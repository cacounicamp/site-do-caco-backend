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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cobre GET /admin/forms/{id}/statistics: distribuição por pergunta, sem cruzamento entre elas. */
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
class FormStatisticsIntegrationTest {

    private static final Path DB_FILE = createDbFile();
    private static final AtomicInteger SEQ = new AtomicInteger();

    private static final RequestPostProcessor ADMIN = user("admin@stats.test").roles("ADMIN");
    private static final RequestPostProcessor ANA = user("ana@stats.test").roles("STUDENT");
    private static final RequestPostProcessor BRUNO = user("bruno@stats.test").roles("STUDENT");
    private static final RequestPostProcessor CARLA = user("carla@stats.test").roles("STUDENT");

    private static Path createDbFile() {
        try {
            Path file = Files.createTempFile("forms-stats-it", ".db");
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
        ensureUser("admin@stats.test", Role.ADMIN);
        ensureUser("ana@stats.test", Role.STUDENT);
        ensureUser("bruno@stats.test", Role.STUDENT);
        ensureUser("carla@stats.test", Role.STUDENT);
        Mockito.reset(fileStorage);
    }

    @Test
    void adminEndpointRejectsNonAdminAndUnknownForm() throws Exception {
        doGet("/admin/forms/" + java.util.UUID.randomUUID() + "/statistics", ANA, 403);
        doGet("/admin/forms/" + java.util.UUID.randomUUID() + "/statistics", ADMIN, 404);
    }

    @Test
    void distributionsCoverChoiceIntegerTextAndFileQuestionsOverSubmittedEntriesOnly() throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "stats-" + n;

        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"course-%d","name":"Curso","options":[
                  {"code":"cc","label":"CC"},{"code":"ec","label":"EC"},{"code":"si","label":"SI"}]}
                """.formatted(n));
        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"interests-%d","name":"Interesses","options":[
                  {"code":"ia","label":"IA"},{"code":"web","label":"Web"},{"code":"jogos","label":"Jogos"}]}
                """.formatted(n));

        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form %d","slug":"%s"}""".formatted(n, slug));
        String formId = JsonPath.read(form, "$.id");
        String q = "/admin/forms/" + formId + "/questions";

        doPost(q, ADMIN, 201, """
                {"code":"course","prompt":"Curso","type":"SINGLE_CHOICE","required":true,"optionSetSlug":"course-%d"}""".formatted(n));
        doPost(q, ADMIN, 201, """
                {"code":"interests","prompt":"Interesses","type":"MULTIPLE_CHOICE","optionSetSlug":"interests-%d"}""".formatted(n));
        doPost(q, ADMIN, 201, """
                {"code":"year","prompt":"Ano","type":"TEXT","textFormat":"INTEGER","minValue":1960,"maxValue":2100}""");
        doPost(q, ADMIN, 201, """
                {"code":"bio","prompt":"Bio","type":"LONG_TEXT"}""");
        doPost(q, ADMIN, 201, """
                {"code":"cv","prompt":"Currículo","type":"FILE"}""");
        doPost(q, ADMIN, 201, """
                {"code":"unused","prompt":"Nunca respondida","type":"TEXT"}""");
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug));

        // Ana: escolhas completas + arquivo enviado com sucesso.
        when(fileStorage.store(any())).thenReturn(new StoredFile("https://files.test/ana.pdf", DocumentKind.FORM_ATTACHMENT, 10));
        submit(slug, ANA, """
                {"answers":[
                  {"question":"course","options":[{"option":"cc"}]},
                  {"question":"interests","options":[{"option":"ia"},{"option":"web"}]},
                  {"question":"year","text":"2019"},
                  {"question":"bio","text":"oi"}]}""");
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 x".getBytes());
        mvc.perform(multipart("/user/forms/{slug}/files/cv", slug).file(file).with(ANA)).andExpect(status().isOk());

        // Bruno: escolhas parciais, sem arquivo, sem bio (opcionais).
        submit(slug, BRUNO, """
                {"answers":[
                  {"question":"course","options":[{"option":"cc"}]},
                  {"question":"interests","options":[{"option":"web"}]},
                  {"question":"year","text":"2020"}]}""");

        // Carla: sobe um arquivo (cria rascunho) mas NUNCA chama o PUT — não deve contar em nada.
        mvc.perform(multipart("/user/forms/{slug}/files/cv", slug).file(file).with(CARLA)).andExpect(status().isOk());

        String stats = doGet("/admin/forms/" + formId + "/statistics", ADMIN, 200);

        assertThat((Integer) JsonPath.read(stats, "$.totalSubmissions")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(stats, "$.uniqueRespondents")).isEqualTo(2);

        Map<String, Object> byCode = indexQuestionsByCode(stats);

        // Escolha única: cc=2, ec=0, si=0 (zero-fill), na ordem de definição.
        assertThat(distributionOf(byCode, "course")).containsExactly(
                Map.entry("cc", 2L), Map.entry("ec", 0L), Map.entry("si", 0L));
        assertThat(answeredOf(byCode, "course")).isEqualTo(2);

        // Múltipla escolha: ia=1 (só Ana), web=2 (Ana e Bruno), jogos=0.
        assertThat(distributionOf(byCode, "interests")).containsExactly(
                Map.entry("ia", 1L), Map.entry("web", 2L), Map.entry("jogos", 0L));

        // Inteiro: dois valores distintos, um balde cada.
        assertThat(distributionOf(byCode, "year")).containsExactly(
                Map.entry("2019", 1L), Map.entry("2020", 1L));

        // Texto livre: sem distribuição, só contagem (só Ana respondeu; Bruno pulou o opcional).
        assertThat((List<?>) ((Map<?, ?>) byCode.get("bio")).get("distribution")).isEmpty();
        assertThat(answeredOf(byCode, "bio")).isEqualTo(1);

        // Arquivo: só Ana "respondeu" de verdade — o rascunho da Carla não vira submissão.
        assertThat(answeredOf(byCode, "cv")).isEqualTo(1);

        // Pergunta nunca respondida: aparece com contagem zero, não some da lista.
        assertThat(answeredOf(byCode, "unused")).isEqualTo(0);
        assertThat((List<?>) ((Map<?, ?>) byCode.get("unused")).get("distribution")).isEmpty();
    }

    @Test
    void yearDistributionSortsNumericallyNotLexicographically() throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "stats-year-" + n;
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"year","prompt":"Ano","type":"TEXT","textFormat":"INTEGER","minValue":1,"maxValue":10000}""");
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug));

        submit(slug, ANA, "{\"answers\":[{\"question\":\"year\",\"text\":\"9\"}]}");
        submit(slug, BRUNO, "{\"answers\":[{\"question\":\"year\",\"text\":\"10\"}]}");
        submit(slug, CARLA, "{\"answers\":[{\"question\":\"year\",\"text\":\"2\"}]}");

        String stats = doGet("/admin/forms/" + formId + "/statistics", ADMIN, 200);
        List<String> values = JsonPath.read(stats, "$.questions[0].distribution[*].value");
        assertThat(values).containsExactly("2", "9", "10");
    }

    @Test
    void deactivatedOptionKeepsHistoricalCountButUnusedDeactivatedOptionDisappears() throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "stats-deact-" + n;
        String setSlug = "shirt-" + n;
        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"%s","name":"Camiseta","options":[
                  {"code":"p","label":"P"},{"code":"m","label":"M"},{"code":"g","label":"G"}]}
                """.formatted(setSlug));
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"shirt","prompt":"Camiseta","type":"SINGLE_CHOICE","optionSetSlug":"%s"}""".formatted(setSlug));
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug));

        submit(slug, ANA, "{\"answers\":[{\"question\":\"shirt\",\"options\":[{\"option\":\"m\"}]}]}");

        // Desativa "m" (tem histórico) e "g" (nunca foi escolhida).
        String setId = first(doGet("/admin/form-option-sets", ADMIN, 200), "$[?(@.slug=='" + setSlug + "')].id");
        doPut("/admin/form-option-sets/" + setId, ADMIN, 200, """
                {"name":"Camiseta","options":[
                  {"code":"p","label":"P"},
                  {"code":"m","label":"M","active":false},
                  {"code":"g","label":"G","active":false}]}""");

        String stats = doGet("/admin/forms/" + formId + "/statistics", ADMIN, 200);
        List<String> codes = JsonPath.read(stats, "$.questions[0].distribution[*].value");
        // "m" permanece (tem resposta histórica), "g" some (desativada e nunca usada), "p" continua (ativa).
        assertThat(codes).containsExactlyInAnyOrder("p", "m");
    }

    @Test
    void inactiveQuestionKeepsItsHistoryInStatistics() throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "stats-inactive-q-" + n;
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"note","prompt":"Nota","type":"TEXT"}""");
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug));
        submit(slug, ANA, "{\"answers\":[{\"question\":\"note\",\"text\":\"x\"}]}");

        String noteId = first(doGet("/admin/forms/" + formId, ADMIN, 200), "$.questions[?(@.code=='note')].id");
        doPut("/admin/forms/" + formId + "/questions/" + noteId, ADMIN, 200, """
                {"prompt":"Nota","type":"TEXT","active":false}""");

        // some da definição pública...
        assertThat((List<?>) JsonPath.read(doGet("/public/forms/" + slug, null, 200), "$.questions")).isEmpty();
        // ...mas não some da estatística.
        String stats = doGet("/admin/forms/" + formId + "/statistics", ADMIN, 200);
        assertThat((List<String>) JsonPath.read(stats, "$.questions[*].code")).containsExactly("note");
        assertThat((Integer) JsonPath.read(stats, "$.questions[0].answeredCount")).isEqualTo(1);
    }

    @Test
    void multipleSubmissionsFormCountsEachEntrySeparatelyInDistributionButUsersOnceInUniqueRespondents() throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "stats-multi-" + n;
        String setSlug = "yes-no-" + n;
        doPost("/admin/form-option-sets", ADMIN, 201, """
                {"slug":"%s","name":"Sim/Não","options":[{"code":"yes","label":"Sim"},{"code":"no","label":"Não"}]}
                """.formatted(setSlug));
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s","allowMultipleSubmissions":true}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"like","prompt":"Gostou?","type":"SINGLE_CHOICE","optionSetSlug":"%s"}""".formatted(setSlug));
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":true}
                """.formatted(slug));

        // Ana envia DUAS entradas; Bruno envia uma.
        String entry1 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", ANA, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + entry1, ANA, 200,
                "{\"answers\":[{\"question\":\"like\",\"options\":[{\"option\":\"yes\"}]}]}");
        String entry2 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", ANA, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + entry2, ANA, 200,
                "{\"answers\":[{\"question\":\"like\",\"options\":[{\"option\":\"no\"}]}]}");
        String entry3 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", BRUNO, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + entry3, BRUNO, 200,
                "{\"answers\":[{\"question\":\"like\",\"options\":[{\"option\":\"yes\"}]}]}");

        String stats = doGet("/admin/forms/" + formId + "/statistics", ADMIN, 200);
        assertThat((Integer) JsonPath.read(stats, "$.totalSubmissions")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(stats, "$.uniqueRespondents")).isEqualTo(2);

        Map<String, Long> counts = JsonPath.<List<Map<String, Object>>>read(stats, "$.questions[0].distribution").stream()
                .collect(java.util.stream.Collectors.toMap(m -> (String) m.get("value"), m -> ((Number) m.get("count")).longValue()));
        assertThat(counts).containsEntry("yes", 2L).containsEntry("no", 1L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void submit(String slug, RequestPostProcessor as, String body) throws Exception {
        doPut("/user/forms/" + slug + "/submission", as, 200, body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> indexQuestionsByCode(String stats) {
        List<Map<String, Object>> questions = JsonPath.read(stats, "$.questions");
        return questions.stream().collect(java.util.stream.Collectors.toMap(q -> (String) q.get("code"), q -> q));
    }

    @SuppressWarnings("unchecked")
    private List<Map.Entry<String, Long>> distributionOf(Map<String, Object> byCode, String code) {
        List<Map<String, Object>> distribution = (List<Map<String, Object>>) ((Map<String, Object>) byCode.get(code)).get("distribution");
        return distribution.stream()
                .map(d -> Map.entry((String) d.get("value"), ((Number) d.get("count")).longValue()))
                .toList();
    }

    private long answeredOf(Map<String, Object> byCode, String code) {
        return ((Number) ((Map<?, ?>) byCode.get(code)).get("answeredCount")).longValue();
    }

    /** JsonPath com filtro devolve sempre uma lista; pega o primeiro (e único) elemento. */
    private static String first(String json, String path) {
        return JsonPath.<List<String>>read(json, path).get(0);
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

    private String doPost(String url, RequestPostProcessor as, int expected, String json) throws Exception {
        MockHttpServletRequestBuilder request = post(url);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return run(request, as, expected);
    }

    private String doPut(String url, RequestPostProcessor as, int expected, String json) throws Exception {
        return run(put(url).contentType(MediaType.APPLICATION_JSON).content(json), as, expected);
    }

    private String run(MockHttpServletRequestBuilder request, RequestPostProcessor as, int expected) throws Exception {
        if (as != null) {
            request.with(as);
        }
        return mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
