package com.caco.sitedocaco.features.forms;

import com.caco.sitedocaco.features.users.entity.User;
import com.caco.sitedocaco.features.users.entity.UserProfile;
import com.caco.sitedocaco.features.users.repository.UserProfileRepository;
import com.caco.sitedocaco.features.users.repository.UserRepository;
import com.caco.sitedocaco.shared.entity.CourseType;
import com.caco.sitedocaco.shared.entity.Role;
import com.caco.sitedocaco.shared.storage.FileStorage;
import com.caco.sitedocaco.shared.storage.StoredFile;
import com.caco.sitedocaco.shared.storage.kind.DocumentKind;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo (admin monta o formulário, usuário responde) sobre um SQLite em arquivo.
 * Atenção: o dialeto SQLite do Hibernate não gera chaves estrangeiras, então este teste cobre lógica e
 * mapeamento JPA, mas NÃO exercita restrições de FK (o MySQL de produção as impõe).
 */
@SpringBootTest(properties = {
        "app.frontend.url=http://localhost:3000",
        "jwt.secret=test-secret-test-secret-test-secret",
        "jwt.expiration=3600000",
        "imgbb.api.key=test",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.security.oauth2.client.registration.google.client-id=dummy",
        "spring.security.oauth2.client.registration.google.client-secret=dummy",
        "spring.security.oauth2.client.registration.google.scope=profile,email",
        "spring.security.oauth2.client.provider.google.authorization-uri=http://localhost:9/oauth2/authorize",
        "spring.security.oauth2.client.provider.google.token-uri=http://localhost:9/oauth2/token",
        "spring.security.oauth2.client.provider.google.user-info-uri=http://localhost:9/userinfo",
        "spring.security.oauth2.client.provider.google.user-name-attribute=sub"
})
@AutoConfigureMockMvc
class FormsIntegrationTest {

    private static final Path DB_FILE = createDbFile();
    private static final AtomicInteger SEQ = new AtomicInteger();

    private static final RequestPostProcessor ADMIN = user("admin@forms.test").roles("ADMIN");
    private static final RequestPostProcessor STUDENT = user("student@forms.test").roles("STUDENT");
    private static final RequestPostProcessor OTHER_STUDENT = user("other@forms.test").roles("STUDENT");

    private static Path createDbFile() {
        try {
            Path file = Files.createTempFile("forms-it", ".db");
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DB_FILE);
    }

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @MockitoBean FileStorage fileStorage;

    @BeforeEach
    void setUp() {
        ensureUser("admin@forms.test", Role.ADMIN);
        ensureUser("student@forms.test", Role.STUDENT);
        ensureUser("other@forms.test", Role.STUDENT);
        Mockito.reset(fileStorage);
    }

    // ── fixture ───────────────────────────────────────────────────────────────

    private record Fixture(String slug, String formId, String courseSet, String yesNoSet, String interestsSet) {
    }

    /**
     * course (escolha única obrigatória, com "Outro"), year (inteiro obrigatório), has-cv (sim/não),
     * cv (arquivo obrigatório, só se has-cv = yes), interests (múltipla) e bio (texto longo até 20).
     */
    private Fixture buildForm(boolean allowEdit, boolean open) throws Exception {
        int n = SEQ.incrementAndGet();
        String courseSet = "course-" + n, yesNoSet = "yes-no-" + n, interestsSet = "interests-" + n, slug = "enroll-" + n;

        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"%s","name":"Curso","options":[
                  {"code":"cc","label":"CC"},{"code":"ec","label":"EC"},{"code":"other","label":"Outro","allowsFreeText":true}]}
                """.formatted(courseSet));
        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"%s","name":"Sim/Não","options":[{"code":"yes","label":"Sim"},{"code":"no","label":"Não"}]}
                """.formatted(yesNoSet));
        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"%s","name":"Interesses","options":[
                  {"code":"a","label":"A"},{"code":"b","label":"B"},{"code":"c","label":"C"}]}
                """.formatted(interestsSet));

        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Inscrição %d","slug":"%s","description":"desc","allowEditAfterSubmit":%b}
                """.formatted(n, slug, allowEdit));
        String formId = JsonPath.read(form, "$.id");

        String q = "/admin/forms/" + formId + "/questions";
        doPost(q, ADMIN, 201, """
                {"code":"course","prompt":"Curso","type":"SINGLE_CHOICE","required":true,"optionSetSlug":"%s"}""".formatted(courseSet));
        doPost(q, ADMIN, 201, """
                {"code":"year","prompt":"Ano de ingresso","type":"TEXT","required":true,"textFormat":"INTEGER","minValue":1960,"maxValue":2100}""");
        doPost(q, ADMIN, 201, """
                {"code":"has-cv","prompt":"Tem currículo?","type":"SINGLE_CHOICE","optionSetSlug":"%s"}""".formatted(yesNoSet));
        doPost(q, ADMIN, 201, """
                {"code":"cv","prompt":"Currículo","type":"FILE","required":true,"showIfQuestion":"has-cv","showIfOption":"yes"}""");
        doPost(q, ADMIN, 201, """
                {"code":"interests","prompt":"Interesses","type":"MULTIPLE_CHOICE","optionSetSlug":"%s"}""".formatted(interestsSet));
        doPost(q, ADMIN, 201, """
                {"code":"bio","prompt":"Bio","type":"LONG_TEXT","maxValue":20}""");

        if (open) {
            updateForm(formId, slug, "OPEN", allowEdit, 200);
        }
        return new Fixture(slug, formId, courseSet, yesNoSet, interestsSet);
    }

    private String updateForm(String formId, String slug, String status, boolean allowEdit, int expected) throws Exception {
        return doPut("/admin/forms/" + formId, ADMIN, expected, """
                {"name":"Inscrição","slug":"%s","description":"desc","status":"%s","allowEditAfterSubmit":%b}
                """.formatted(slug, status, allowEdit));
    }

    private String questionId(String formId, String code) throws Exception {
        String detail = doGet("/admin/forms/" + formId, ADMIN, 200);
        List<String> ids = JsonPath.read(detail, "$.questions[?(@.code=='" + code + "')].id");
        return ids.get(0);
    }

    private static final String VALID_ANSWERS = """
            {"answers":[
              {"question":"course","options":[{"option":"cc"}]},
              {"question":"year","text":"2020"},
              {"question":"has-cv","options":[{"option":"no"}]}]}""";

    private String submit(Fixture f, RequestPostProcessor as, String body, int expected) throws Exception {
        return doPut("/user/forms/" + f.slug() + "/submission", as, expected, body);
    }

    // ── público ───────────────────────────────────────────────────────────────

    @Test
    void draftIsHiddenAndOpeningRequiresAnActiveQuestion() throws Exception {
        String slug = "empty-" + SEQ.incrementAndGet();
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Vazio","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        assertThat((String) JsonPath.read(form, "$.status")).isEqualTo("DRAFT");

        doGet("/public/forms/" + slug, null, 404);
        assertThat((String) JsonPath.read(updateForm(formId, slug, "OPEN", true, 400), "$.message"))
                .contains("ao menos uma pergunta ativa");

        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"name","prompt":"Nome","type":"TEXT"}""");
        updateForm(formId, slug, "OPEN", true, 200);
        doGet("/public/forms/" + slug, null, 200);
    }

    @Test
    void publicDefinitionListsOnlyActiveQuestionsOptionsAndTheirConditions() throws Exception {
        Fixture f = buildForm(true, true);

        String bioId = questionId(f.formId(), "bio");
        doPut("/admin/forms/" + f.formId() + "/questions/" + bioId, ADMIN, 200, """
                {"prompt":"Bio","type":"LONG_TEXT","maxValue":20,"active":false}""");

        String def = doGet("/public/forms/" + f.slug(), null, 200);
        List<String> codes = JsonPath.read(def, "$.questions[*].code");
        assertThat(codes).containsExactly("course", "year", "has-cv", "cv", "interests");
        assertThat(first(def, "$.questions[?(@.code=='cv')].showIf.question")).isEqualTo("has-cv");
        assertThat(first(def, "$.questions[?(@.code=='cv')].showIf.option")).isEqualTo("yes");
        assertThat(publicOptionCodes(def, "course")).containsExactly("cc", "ec", "other");
    }

    // ── responder ─────────────────────────────────────────────────────────────

    @Test
    void submitValidatesRequiredFieldsFormatsAndOptions() throws Exception {
        Fixture f = buildForm(true, true);

        assertThat((String) JsonPath.read(submit(f, STUDENT, "{\"answers\":[]}", 400), "$.message"))
                .contains("Curso: resposta obrigatória");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"course","options":[{"option":"cc"}]}]}""", 400), "$.message"))
                .contains("Ano de ingresso: resposta obrigatória");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"course","options":[{"option":"cc"}]},{"question":"year","text":"1900"}]}""", 400), "$.message"))
                .contains("valor mínimo é 1960");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"course","options":[{"option":"zzz"}]},{"question":"year","text":"2020"}]}""", 400), "$.message"))
                .contains("opção inválida");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"course","options":[{"option":"other"}]},{"question":"year","text":"2020"}]}""", 400), "$.message"))
                .contains("especifique");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"nope","text":"x"}]}""", 400), "$.message"))
                .contains("Perguntas inexistentes: nope");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[{"question":"cv","text":"x"}]}""", 400), "$.message"))
                .contains("endpoint de arquivos");
        assertThat((String) JsonPath.read(submit(f, STUDENT, """
                {"answers":[
                  {"question":"course","options":[{"option":"cc"}]},{"question":"year","text":"2020"},
                  {"question":"bio","text":"%s"}]}""".formatted("x".repeat(21)), 400), "$.message"))
                .contains("máximo de 20");

        // nada foi persistido pelas tentativas inválidas
        String mine = doGet("/user/forms/" + f.slug() + "/submission", STUDENT, 200);
        assertThat((Boolean) JsonPath.read(mine, "$.submitted")).isFalse();
    }

    @Test
    void validSubmissionIsPersistedPerUserAndReadBack() throws Exception {
        Fixture f = buildForm(true, true);

        String out = submit(f, STUDENT, """
                {"answers":[
                  {"question":"course","options":[{"option":"other","freeText":"  Física  "}]},
                  {"question":"year","text":" 2020 "},
                  {"question":"has-cv","options":[{"option":"no"}]},
                  {"question":"interests","options":[{"option":"a"},{"option":"b"}]},
                  {"question":"bio","text":"oi"}]}""", 200);

        assertThat((Boolean) JsonPath.read(out, "$.submitted")).isTrue();
        assertThat((String) JsonPath.read(out, "$.answers.course.options[0].code")).isEqualTo("other");
        assertThat((String) JsonPath.read(out, "$.answers.course.options[0].freeText")).isEqualTo("Física");
        assertThat((String) JsonPath.read(out, "$.answers.year.text")).isEqualTo("2020");
        assertThat((List<?>) JsonPath.read(out, "$.answers.interests.options")).hasSize(2);
        assertThat((String) JsonPath.read(out, "$.answers.bio.text")).isEqualTo("oi");

        String again = doGet("/user/forms/" + f.slug() + "/submission", STUDENT, 200);
        assertThat((Boolean) JsonPath.read(again, "$.submitted")).isTrue();
        assertThat((String) JsonPath.read(again, "$.answers.year.text")).isEqualTo("2020");

        String other = doGet("/user/forms/" + f.slug() + "/submission", OTHER_STUDENT, 200);
        assertThat((Boolean) JsonPath.read(other, "$.submitted")).isFalse();
        assertThat((java.util.Map<?, ?>) JsonPath.read(other, "$.answers")).isEmpty();
    }

    @Test
    void resubmittingReplacesTheWholeStateIncludingOverlappingChoices() throws Exception {
        Fixture f = buildForm(true, true);
        submit(f, STUDENT, """
                {"answers":[
                  {"question":"course","options":[{"option":"cc"}]},{"question":"year","text":"2020"},
                  {"question":"interests","options":[{"option":"a"},{"option":"b"}]},
                  {"question":"bio","text":"oi"}]}""", 200);

        // b permanece, a sai, c entra; bio omitida = removida
        String out = submit(f, STUDENT, """
                {"answers":[
                  {"question":"course","options":[{"option":"ec"}]},{"question":"year","text":"2021"},
                  {"question":"interests","options":[{"option":"b"},{"option":"c"}]}]}""", 200);

        List<String> interests = JsonPath.read(out, "$.answers.interests.options[*].code");
        assertThat(interests).containsExactlyInAnyOrder("b", "c");
        assertThat((String) JsonPath.read(out, "$.answers.course.options[0].code")).isEqualTo("ec");
        assertThat(JsonPath.<java.util.Map<String, Object>>read(out, "$.answers")).doesNotContainKey("bio");
    }

    @Test
    void conditionalFileQuestionIsRequiredOnlyWhileVisibleAndItsFileIsDiscardedWhenHidden() throws Exception {
        Fixture f = buildForm(true, true);
        Deque<String> urls = new ArrayDeque<>(List.of("https://files.test/one.pdf", "https://files.test/two.pdf"));
        when(fileStorage.store(any())).thenAnswer(inv ->
                new StoredFile(urls.poll(), DocumentKind.FORM_ATTACHMENT, 10));

        String withCv = """
                {"answers":[
                  {"question":"course","options":[{"option":"cc"}]},{"question":"year","text":"2020"},
                  {"question":"has-cv","options":[{"option":"yes"}]}]}""";

        assertThat((String) JsonPath.read(submit(f, STUDENT, withCv, 400), "$.message")).contains("Currículo: resposta obrigatória");

        String uploaded = doUpload(f, "cv", "cv.pdf", STUDENT, 200);
        assertThat((String) JsonPath.read(uploaded, "$.fileName")).isEqualTo("cv.pdf");
        assertThat((String) JsonPath.read(uploaded, "$.fileUrl")).isEqualTo("https://files.test/one.pdf");
        // um rascunho com arquivo ainda não conta como respondido
        assertThat((Boolean) JsonPath.read(doGet("/user/forms/" + f.slug() + "/submission", STUDENT, 200), "$.submitted")).isFalse();

        String out = submit(f, STUDENT, withCv, 200);
        assertThat((Boolean) JsonPath.read(out, "$.submitted")).isTrue();
        assertThat((String) JsonPath.read(out, "$.answers.cv.fileUrl")).isEqualTo("https://files.test/one.pdf");

        // substituir o arquivo apaga o anterior do storage
        doUpload(f, "cv", "novo.pdf", STUDENT, 200);
        verify(fileStorage).delete("https://files.test/one.pdf");

        // obrigatório e já enviado: não pode ser removido, só substituído
        assertThat((String) JsonPath.read(doDelete("/user/forms/" + f.slug() + "/files/cv", STUDENT, 400), "$.message"))
                .contains("obrigatória");

        // ocultar a pergunta (has-cv = no) descarta a resposta e o arquivo
        String hidden = submit(f, STUDENT, VALID_ANSWERS, 200);
        assertThat(JsonPath.<java.util.Map<String, Object>>read(hidden, "$.answers")).doesNotContainKey("cv");
        verify(fileStorage).delete("https://files.test/two.pdf");
        doDelete("/user/forms/" + f.slug() + "/files/cv", STUDENT, 204);
    }

    @Test
    void uploadRejectsWrongQuestionOrEmptyFile() throws Exception {
        Fixture f = buildForm(true, true);

        doUpload(f, "course", "cv.pdf", STUDENT, 404);
        doUpload(f, "inexistente", "cv.pdf", STUDENT, 404);
        mvc.perform(multipart("/user/forms/{slug}/files/{code}", f.slug(), "cv")
                        .file(new MockMultipartFile("file", "vazio.pdf", "application/pdf", new byte[0])).with(STUDENT))
                .andExpect(status().isBadRequest());
        verify(fileStorage, never()).store(any());
    }

    // ── ciclo de vida ─────────────────────────────────────────────────────────

    @Test
    void submissionIsLockedWhenTheFormDisallowsEditsAfterSubmit() throws Exception {
        Fixture f = buildForm(false, true);

        submit(f, STUDENT, VALID_ANSWERS, 200);
        assertThat((String) JsonPath.read(submit(f, STUDENT, VALID_ANSWERS, 400), "$.message")).contains("não pode ser editado");
        doUpload(f, "cv", "cv.pdf", STUDENT, 400);
        // outro usuário não é afetado
        submit(f, OTHER_STUDENT, VALID_ANSWERS, 200);
    }

    @Test
    void closedFormStopsAcceptingAnswersButKeepsBeingReadable() throws Exception {
        Fixture f = buildForm(true, true);
        submit(f, STUDENT, VALID_ANSWERS, 200);

        updateForm(f.formId(), f.slug(), "CLOSED", true, 200);

        assertThat((String) JsonPath.read(submit(f, STUDENT, VALID_ANSWERS, 400), "$.message")).contains("não está aceitando respostas");
        assertThat((Boolean) JsonPath.read(doGet("/user/forms/" + f.slug() + "/submission", STUDENT, 200), "$.submitted")).isTrue();
        assertThat((String) JsonPath.read(doGet("/public/forms/" + f.slug(), null, 200), "$.status")).isEqualTo("CLOSED");
    }

    @Test
    void answeredStructureIsLockedButUnusedPartsStayEditable() throws Exception {
        Fixture f = buildForm(true, true);
        submit(f, STUDENT, VALID_ANSWERS, 200);
        String base = "/admin/forms/" + f.formId();

        // formulário com respostas
        assertThat((String) JsonPath.read(doPut(base, ADMIN, 400, """
                {"name":"X","slug":"outro-slug","status":"OPEN","allowEditAfterSubmit":true}"""), "$.message")).contains("slug");
        assertThat((String) JsonPath.read(updateForm(f.formId(), f.slug(), "DRAFT", true, 400), "$.message")).contains("rascunho");
        assertThat((String) JsonPath.read(doDelete(base, ADMIN, 400), "$.message")).contains("não pode ser excluído");

        // pergunta com respostas: sem excluir nem trocar tipo; mas o enunciado pode mudar
        String courseId = questionId(f.formId(), "course");
        assertThat((String) JsonPath.read(doDelete(base + "/questions/" + courseId, ADMIN, 400), "$.message")).contains("respostas");
        assertThat((String) JsonPath.read(doPut(base + "/questions/" + courseId, ADMIN, 400, """
                {"prompt":"Curso","type":"TEXT"}"""), "$.message")).contains("já tem respostas");
        String renamed = doPut(base + "/questions/" + courseId, ADMIN, 200, """
                {"prompt":"Qual seu curso?","type":"SINGLE_CHOICE","required":true,"optionSetSlug":"%s"}""".formatted(f.courseSet()));
        assertThat((Boolean) JsonPath.read(renamed, "$.hasAnswers")).isTrue();

        // pergunta sem respostas pode ser excluída
        doDelete(base + "/questions/" + questionId(f.formId(), "bio"), ADMIN, 204);

        // opções: ec nunca foi escolhida (exclui); cc foi (só desativa); other permanece
        String setId = first(doGet("/admin/form-option-sets", ADMIN, 200), "$[?(@.slug=='" + f.courseSet() + "')].id");
        String updated = doPut("/admin/form-option-sets/" + setId, ADMIN, 200, """
                {"name":"Curso","options":[{"code":"other","label":"Outro","allowsFreeText":true}]}""");
        List<String> codes = JsonPath.read(updated, "$.options[*].code");
        assertThat(codes).containsExactly("other", "cc");
        assertThat(JsonPath.<List<Boolean>>read(updated, "$.options[?(@.code=='cc')].active").get(0)).isFalse();

        // a opção desativada some da definição pública e não é mais aceita
        assertThat(publicOptionCodes(doGet("/public/forms/" + f.slug(), null, 200), "course")).containsExactly("other");
        assertThat((String) JsonPath.read(submit(f, STUDENT, VALID_ANSWERS, 400), "$.message")).contains("opção inválida");

        // conjunto em uso por pergunta não pode ser excluído
        assertThat((String) JsonPath.read(doDelete("/admin/form-option-sets/" + setId, ADMIN, 400), "$.message")).contains("usado por perguntas");
    }

    @Test
    void optionThatTriggersAnActiveConditionalCannotBeRemovedOrDeactivated() throws Exception {
        Fixture f = buildForm(true, false);
        String setId = first(doGet("/admin/form-option-sets", ADMIN, 200), "$[?(@.slug=='" + f.yesNoSet() + "')].id");

        assertThat((String) JsonPath.read(doPut("/admin/form-option-sets/" + setId, ADMIN, 400, """
                {"name":"Sim/Não","options":[{"code":"no","label":"Não"}]}"""), "$.message")).contains("gatilho");
        assertThat((String) JsonPath.read(doPut("/admin/form-option-sets/" + setId, ADMIN, 400, """
                {"name":"Sim/Não","options":[{"code":"yes","label":"Sim","active":false},{"code":"no","label":"Não"}]}"""), "$.message"))
                .contains("gatilho");
        // renomear o rótulo é livre
        doPut("/admin/form-option-sets/" + setId, ADMIN, 200, """
                {"name":"Sim/Não","options":[{"code":"yes","label":"Com certeza"},{"code":"no","label":"Não"}]}""");
    }

    // ── administração ─────────────────────────────────────────────────────────

    @Test
    void reorderKeepsConditionalsAfterTheirParents() throws Exception {
        Fixture f = buildForm(true, false);
        String base = "/admin/forms/" + f.formId() + "/questions/reorder";
        String course = questionId(f.formId(), "course"), year = questionId(f.formId(), "year"),
                hasCv = questionId(f.formId(), "has-cv"), cv = questionId(f.formId(), "cv"),
                interests = questionId(f.formId(), "interests"), bio = questionId(f.formId(), "bio");

        assertThat((String) JsonPath.read(doPut(base, ADMIN, 400, ids(course, year, cv, hasCv, interests, bio)), "$.message"))
                .contains("depende de 'has-cv'");
        assertThat((String) JsonPath.read(doPut(base, ADMIN, 400, ids(course, year, hasCv, cv, interests)), "$.message"))
                .contains("exatamente todas");
        assertThat((String) JsonPath.read(doPut(base, ADMIN, 400, ids(course, year, hasCv, cv, interests, interests)), "$.message"))
                .contains("exatamente todas");

        String ok = doPut(base, ADMIN, 200, ids(bio, course, year, hasCv, cv, interests));
        List<String> codes = JsonPath.read(ok, "$[*].code");
        assertThat(codes).containsExactly("bio", "course", "year", "has-cv", "cv", "interests");
        List<String> stored = JsonPath.read(doGet("/admin/forms/" + f.formId(), ADMIN, 200), "$.questions[*].code");
        assertThat(stored).containsExactly("bio", "course", "year", "has-cv", "cv", "interests");
    }

    @Test
    void questionConfigurationIsValidated() throws Exception {
        Fixture f = buildForm(true, false);
        String q = "/admin/forms/" + f.formId() + "/questions";

        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"course","prompt":"Dup","type":"TEXT"}"""), "Já existe uma pergunta com o código");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x1","prompt":"X","type":"SINGLE_CHOICE"}"""), "exigem um conjunto de opções");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x2","prompt":"X","type":"TEXT","optionSetSlug":"%s"}""".formatted(f.yesNoSet())), "Só perguntas de escolha");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x3","prompt":"X","type":"SINGLE_CHOICE","optionSetSlug":"nao-existe"}"""), "Conjunto de opções inexistente");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x4","prompt":"X","type":"LONG_TEXT","textFormat":"EMAIL"}"""), "texto curto");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x5","prompt":"X","type":"TEXT","minValue":10,"maxValue":5,"textFormat":"INTEGER"}"""), "mínimo não pode ser maior");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x6","prompt":"X","type":"TEXT","maxValue":9999}"""), "entre 1 e 255");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x7","prompt":"X","type":"SINGLE_CHOICE","optionSetSlug":"%s","minValue":1}""".formatted(f.yesNoSet())), "Mínimo e máximo");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x8","prompt":"X","type":"TEXT","showIfQuestion":"year"}"""), "precisa ser de escolha");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x9","prompt":"X","type":"TEXT","showIfQuestion":"has-cv","showIfOption":"a"}"""), "não pertence às opções");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x10","prompt":"X","type":"TEXT","showIfOption":"yes"}"""), "Informe a pergunta da condicional");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x11","prompt":"X","type":"TEXT","showIfQuestion":"ghost"}"""), "não encontrada");
        // validação de bean (400 com detalhamento dos campos) e corpo malformado (antes virava 500)
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"Código Inválido","prompt":"","type":"TEXT"}"""), "Erros de validação");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"x12","prompt":"X","type":"NOPE"}"""), "Corpo da requisição inválido");
        assertMessage(doPost("/admin/forms", ADMIN, 400, """
                {"name":"X","slug":"Slug Inválido"}"""), "Erros de validação");
        assertMessage(doPost("/admin/forms", ADMIN, 400, """
                {"name":"Outro","slug":"%s"}""".formatted(f.slug())), "Já existe um formulário com esse slug");
    }

    @Test
    void conditionalDependenciesAreProtected() throws Exception {
        Fixture f = buildForm(true, false);
        String base = "/admin/forms/" + f.formId() + "/questions/";
        String hasCv = questionId(f.formId(), "has-cv");

        assertMessage(doDelete(base + hasCv, ADMIN, 400), "dependem desta");
        assertMessage(doPut(base + hasCv, ADMIN, 400, """
                {"prompt":"Tem?","type":"SINGLE_CHOICE","optionSetSlug":"%s","active":false}""".formatted(f.yesNoSet())), "dependem desta");
        assertMessage(doPut(base + hasCv, ADMIN, 400, """
                {"prompt":"Tem?","type":"SINGLE_CHOICE","optionSetSlug":"%s"}""".formatted(f.interestsSet())), "condicionais");

        // uma pergunta que vem depois não pode ser a mãe de uma anterior
        String year = questionId(f.formId(), "year");
        assertMessage(doPut(base + year, ADMIN, 400, """
                {"prompt":"Ano","type":"TEXT","showIfQuestion":"has-cv"}"""), "vir antes");
        // ...mas a que vem antes pode
        String cv = questionId(f.formId(), "cv");
        doPut(base + cv, ADMIN, 200, """
                {"prompt":"Currículo","type":"FILE","required":true,"showIfQuestion":"course"}""");
    }

    @Test
    void deletingAnUnansweredFormRemovesItsInterlinkedQuestions() throws Exception {
        Fixture f = buildForm(true, false);

        doDelete("/admin/forms/" + f.formId(), ADMIN, 204);
        doGet("/admin/forms/" + f.formId(), ADMIN, 404);
    }

    @Test
    void adminEndpointsRejectNonAdminUsers() throws Exception {
        doPost("/admin/forms", STUDENT, 403, """
                {"name":"X","slug":"x-1"}""");
        doGet("/admin/forms", STUDENT, 403);
        doGet("/admin/form-option-sets", STUDENT, 403);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String ids(String... ids) {
        return "{\"questionIds\":[" + String.join(",", java.util.Arrays.stream(ids).map(i -> "\"" + i + "\"").toList()) + "]}";
    }

    /** JsonPath com filtro devolve sempre uma lista; pega o primeiro (e único) elemento. */
    private static String first(String json, String path) {
        return JsonPath.<List<String>>read(json, path).get(0);
    }

    private static List<String> publicOptionCodes(String definition, String questionCode) {
        List<List<java.util.Map<String, Object>>> options =
                JsonPath.read(definition, "$.questions[?(@.code=='" + questionCode + "')].options");
        return options.get(0).stream().map(o -> (String) o.get("code")).toList();
    }

    private static void assertMessage(String body, String expectedFragment) {
        assertThat((String) JsonPath.read(body, "$.message")).contains(expectedFragment);
    }

    private void ensureUser(String email, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        User u = new User();
        u.setEmail(email);
        u.setUsername(email.substring(0, email.indexOf('@')));
        u.setRole(role);
        u = userRepository.save(u);

        UserProfile profile = new UserProfile();
        profile.setUser(u);
        profile.setCourse(CourseType.CIENCIAS_DA_COMPUTACAO);
        profile.setEntryYear(2020);
        userProfileRepository.save(profile);
    }

    private String doGet(String url, RequestPostProcessor as, int expected) throws Exception {
        return run(get(url), as, expected);
    }

    private String doDelete(String url, RequestPostProcessor as, int expected) throws Exception {
        return run(delete(url), as, expected);
    }

    private String doPost(String url, RequestPostProcessor as, int expected, String json) throws Exception {
        return run(post(url).contentType(MediaType.APPLICATION_JSON).content(json), as, expected);
    }

    private String doPut(String url, RequestPostProcessor as, int expected, String json) throws Exception {
        return run(put(url).contentType(MediaType.APPLICATION_JSON).content(json), as, expected);
    }

    private String doUpload(Fixture f, String questionCode, String filename, RequestPostProcessor as, int expected) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, "application/pdf", "%PDF-1.4 conteudo".getBytes());
        return mvc.perform(multipart("/user/forms/{slug}/files/{code}", f.slug(), questionCode).file(file).with(as))
                .andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String run(MockHttpServletRequestBuilder request, RequestPostProcessor as, int expected) throws Exception {
        if (as != null) {
            request.with(as);
        }
        return mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
