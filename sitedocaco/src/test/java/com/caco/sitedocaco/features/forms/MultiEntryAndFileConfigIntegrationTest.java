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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre as duas extensões do motor de formulários que não estavam na entrega original:
 * (1) tipo/tamanho de arquivo configuráveis por pergunta e (2) formulários de múltiplas respostas,
 * com edição por entrada. Roda em contexto próprio (ver {@link FormsIntegrationTest} para o resto).
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
class MultiEntryAndFileConfigIntegrationTest {

    private static final Path DB_FILE = createDbFile();
    private static final AtomicInteger SEQ = new AtomicInteger();

    private static final RequestPostProcessor ADMIN = user("admin@multi.test").roles("ADMIN");
    private static final RequestPostProcessor STUDENT = user("student@multi.test").roles("STUDENT");
    private static final RequestPostProcessor OTHER_STUDENT = user("other@multi.test").roles("STUDENT");

    private static Path createDbFile() {
        try {
            Path file = Files.createTempFile("forms-multi-it", ".db");
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
        ensureUser("admin@multi.test", Role.ADMIN);
        ensureUser("student@multi.test", Role.STUDENT);
        ensureUser("other@multi.test", Role.STUDENT);
        Mockito.reset(fileStorage);
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private String createOpenForm(boolean allowMultiple, boolean allowEdit, Integer maxPerUser) throws Exception {
        int n = SEQ.incrementAndGet();
        String slug = "multi-" + n;
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form %d","slug":"%s","allowEditAfterSubmit":%b,
                 "allowMultipleSubmissions":%b,"maxSubmissionsPerUser":%s}
                """.formatted(n, slug, allowEdit, allowMultiple,
                maxPerUser == null ? "null" : maxPerUser));
        String formId = JsonPath.read(form, "$.id");

        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"note","prompt":"Nota","type":"TEXT"}""");
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form %d","slug":"%s","status":"OPEN","allowEditAfterSubmit":%b,
                 "allowMultipleSubmissions":%b,"maxSubmissionsPerUser":%s}
                """.formatted(n, slug, allowEdit, allowMultiple,
                maxPerUser == null ? "null" : maxPerUser));
        return slug;
    }

    private static String noteAnswers(String text) {
        return "{\"answers\":[{\"question\":\"note\",\"text\":\"" + text + "\"}]}";
    }

    // ── guarda de modo ───────────────────────────────────────────────────────

    @Test
    void singleModeFormRejectsThePluralEndpointsAndViceVersa() throws Exception {
        String single = createOpenForm(false, true, null);
        String multi = createOpenForm(true, true, null);

        assertMessage(doGet("/user/forms/" + single + "/submissions", STUDENT, 400), "múltiplas respostas");
        assertMessage(doPost("/user/forms/" + single + "/submissions", STUDENT, 400, null), "múltiplas respostas");
        assertMessage(doGet("/user/forms/" + multi + "/submission", STUDENT, 400), "use os endpoints de múltiplos envios");
        assertMessage(doPut("/user/forms/" + multi + "/submission", STUDENT, 400, noteAnswers("x")), "use os endpoints de múltiplos envios");
    }

    // ── ciclo de vida de múltiplas entradas ──────────────────────────────────

    @Test
    void eachEntryHasItsOwnLifecycleAndListingIsOrdered() throws Exception {
        String slug = createOpenForm(true, true, null);

        String created1 = doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null);
        assertThat((Boolean) JsonPath.read(created1, "$.submitted")).isFalse();
        String id1 = JsonPath.read(created1, "$.id");

        String filled1 = doPut("/user/forms/" + slug + "/submissions/" + id1, STUDENT, 200, noteAnswers("primeira"));
        assertThat((Boolean) JsonPath.read(filled1, "$.submitted")).isTrue();

        String created2 = doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null);
        String id2 = JsonPath.read(created2, "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + id2, STUDENT, 200, noteAnswers("segunda"));

        String list = doGet("/user/forms/" + slug + "/submissions", STUDENT, 200);
        List<String> ids = JsonPath.read(list, "$[*].id");
        assertThat(ids).containsExactly(id1, id2);
        List<String> notes = JsonPath.read(list, "$[*].answers.note.text");
        assertThat(notes).containsExactly("primeira", "segunda");

        // outro usuário não vê as entradas alheias
        assertThat(JsonPath.<List<Object>>read(doGet("/user/forms/" + slug + "/submissions", OTHER_STUDENT, 200), "$")).isEmpty();
    }

    @Test
    void maxSubmissionsPerUserCapsCreationPerUserIndependently() throws Exception {
        String slug = createOpenForm(true, true, 2);

        doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null);
        doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null);
        assertMessage(doPost("/user/forms/" + slug + "/submissions", STUDENT, 400, null), "número máximo de envios");

        // outro usuário tem sua própria cota
        doPost("/user/forms/" + slug + "/submissions", OTHER_STUDENT, 201, null);
    }

    @Test
    void editingAndDeletingAnEntryAreIndependentFromOtherEntries() throws Exception {
        String slug = createOpenForm(true, true, null);
        String id1 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");
        String id2 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + id1, STUDENT, 200, noteAnswers("um"));
        doPut("/user/forms/" + slug + "/submissions/" + id2, STUDENT, 200, noteAnswers("dois"));

        doDelete("/user/forms/" + slug + "/submissions/" + id1, STUDENT, 204);

        List<String> remaining = JsonPath.read(doGet("/user/forms/" + slug + "/submissions", STUDENT, 200), "$[*].id");
        assertThat(remaining).containsExactly(id2);
        doGet("/user/forms/" + slug + "/submissions/" + id1, STUDENT, 404);
    }

    @Test
    void perEntryEditabilityFollowsAllowEditAfterSubmitJustLikeSingleMode() throws Exception {
        String slug = createOpenForm(true, false, null);
        String id = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + id, STUDENT, 200, noteAnswers("original"));

        assertMessage(doPut("/user/forms/" + slug + "/submissions/" + id, STUDENT, 400, noteAnswers("editado")), "não pode ser editado");
        assertMessage(doDelete("/user/forms/" + slug + "/submissions/" + id, STUDENT, 400), "não pode ser editado");

        // uma entrada NOVA (ainda não enviada) continua criável e editável normalmente
        String id2 = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + id2, STUDENT, 200, noteAnswers("nova"));
    }

    @Test
    void entryOwnershipIsEnforcedAsNotFoundNotForbidden() throws Exception {
        String slug = createOpenForm(true, true, null);
        String id = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");

        doGet("/user/forms/" + slug + "/submissions/" + id, OTHER_STUDENT, 404);
        doPut("/user/forms/" + slug + "/submissions/" + id, OTHER_STUDENT, 404, noteAnswers("invasão"));
        doDelete("/user/forms/" + slug + "/submissions/" + id, OTHER_STUDENT, 404);
    }

    @Test
    void deletingAnEntryRemovesItsFileFromStorage() throws Exception {
        String slug = createOpenForm(true, true, null);
        String formId = first(doGet("/admin/forms", ADMIN, 200), "$[?(@.slug=='" + slug + "')].id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"attachment","prompt":"Anexo","type":"FILE"}""");

        when(fileStorage.store(any())).thenReturn(new StoredFile("https://files.test/entry.pdf", DocumentKind.FORM_ATTACHMENT, 10));
        String id = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");

        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 x".getBytes());
        mvc.perform(multipart("/user/forms/{slug}/submissions/{id}/files/{code}", slug, id, "attachment").file(file).with(STUDENT))
                .andExpect(status().isOk());

        doDelete("/user/forms/" + slug + "/submissions/" + id, STUDENT, 204);
        verify(fileStorage).delete("https://files.test/entry.pdf");
    }

    // ── trava de modo no admin ───────────────────────────────────────────────

    @Test
    void allowMultipleSubmissionsCannotChangeOnceTheFormHasSubmissions() throws Exception {
        String slug = createOpenForm(true, true, null);
        String formId = first(doGet("/admin/forms", ADMIN, 200), "$[?(@.slug=='" + slug + "')].id");
        String id = JsonPath.read(doPost("/user/forms/" + slug + "/submissions", STUDENT, 201, null), "$.id");
        doPut("/user/forms/" + slug + "/submissions/" + id, STUDENT, 200, noteAnswers("x"));

        assertMessage(doPut("/admin/forms/" + formId, ADMIN, 400, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug)), "modo de múltiplas respostas");
    }

    @Test
    void maxSubmissionsPerUserIsValidatedAgainstTheMultipleSubmissionsFlag() throws Exception {
        assertMessage(doPost("/admin/forms", ADMIN, 400, """
                {"name":"X","slug":"single-with-cap-%d","allowMultipleSubmissions":false,"maxSubmissionsPerUser":3}
                """.formatted(SEQ.incrementAndGet())), "maxSubmissionsPerUser só se aplica");
        assertMessage(doPost("/admin/forms", ADMIN, 400, """
                {"name":"X","slug":"multi-zero-cap-%d","allowMultipleSubmissions":true,"maxSubmissionsPerUser":0}
                """.formatted(SEQ.incrementAndGet())), "maior que zero");
    }

    // ── configuração de arquivo por pergunta ─────────────────────────────────

    @Test
    void adminConfiguresAllowedExtensionsAndMaxSizePerQuestion() throws Exception {
        String slug = "file-cfg-" + SEQ.incrementAndGet();
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        String q = "/admin/forms/" + formId + "/questions";

        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"a","prompt":"A","type":"TEXT","allowedExtensions":["pdf"]}"""), "só se aplicam a perguntas de arquivo");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"b","prompt":"B","type":"TEXT","maxFileSizeBytes":100}"""), "só se aplica a perguntas de arquivo");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"c","prompt":"C","type":"FILE","allowedExtensions":["exe"]}"""), "Extensão não suportada");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"d","prompt":"D","type":"FILE","maxFileSizeBytes":0}"""), "entre 1 byte");
        assertMessage(doPost(q, ADMIN, 400, """
                {"code":"e","prompt":"E","type":"FILE","maxFileSizeBytes":999999999}"""), "entre 1 byte");

        String created = doPost(q, ADMIN, 201, """
                {"code":"cv","prompt":"Currículo","type":"FILE","allowedExtensions":["PDF","png"],"maxFileSizeBytes":1000}""");
        List<String> extensions = JsonPath.read(created, "$.allowedExtensions");
        assertThat(extensions).containsExactlyInAnyOrder("pdf", "png");
        assertThat((Integer) JsonPath.read(created, "$.maxFileSizeBytes")).isEqualTo(1000);
    }

    @Test
    void perQuestionFileConfigIsEnforcedEndToEndOnUpload() throws Exception {
        String slug = "file-e2e-" + SEQ.incrementAndGet();
        String form = doPost("/admin/forms", ADMIN, 201, """
                {"name":"Form","slug":"%s"}""".formatted(slug));
        String formId = JsonPath.read(form, "$.id");
        doPost("/admin/forms/" + formId + "/questions", ADMIN, 201, """
                {"code":"cv","prompt":"Currículo","type":"FILE","allowedExtensions":["png"],"maxFileSizeBytes":10}""");
        doPut("/admin/forms/" + formId, ADMIN, 200, """
                {"name":"Form","slug":"%s","status":"OPEN","allowEditAfterSubmit":true,"allowMultipleSubmissions":false}
                """.formatted(slug));

        // a definição pública já expõe a configuração efetiva desta pergunta
        String publicDef = doGet("/public/forms/" + slug, null, 200);
        List<List<String>> extensionsPerMatch = JsonPath.read(publicDef, "$.questions[?(@.code=='cv')].allowedExtensions");
        assertThat(extensionsPerMatch.get(0)).containsExactly("png");
        List<Integer> maxSizePerMatch = JsonPath.read(publicDef, "$.questions[?(@.code=='cv')].maxFileSizeBytes");
        assertThat(maxSizePerMatch.get(0)).isEqualTo(10);

        // extensão fora do subconjunto configurado (mesmo estando no catálogo geral) é rejeitada
        MockMultipartFile pdf = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 x".getBytes());
        mvc.perform(multipart("/user/forms/{slug}/files/{code}", slug, "cv").file(pdf).with(STUDENT))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("extensão não permitida")));

        // tamanho acima do configurado (mesmo abaixo do teto absoluto da FileKind) é rejeitado
        MockMultipartFile bigPng = new MockMultipartFile("file", "cv.png",
                "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0});
        mvc.perform(multipart("/user/forms/{slug}/files/{code}", slug, "cv").file(bigPng).with(STUDENT))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tamanho máximo")));

        // dentro da configuração, passa
        when(fileStorage.store(any())).thenReturn(new StoredFile("https://files.test/cv.png", DocumentKind.FORM_ATTACHMENT, 8));
        MockMultipartFile okPng = new MockMultipartFile("file", "cv.png",
                "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
        mvc.perform(multipart("/user/forms/{slug}/files/{code}", slug, "cv").file(okPng).with(STUDENT))
                .andExpect(status().isOk());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** JsonPath com filtro devolve sempre uma lista; pega o primeiro (e único) elemento. */
    private static String first(String json, String path) {
        return JsonPath.<List<String>>read(json, path).get(0);
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
