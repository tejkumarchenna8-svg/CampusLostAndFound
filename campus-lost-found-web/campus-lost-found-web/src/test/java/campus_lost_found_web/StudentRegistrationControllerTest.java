package campus_lost_found_web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import campus_lost_found_web.repository.StudentRepository;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:registration-page-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class StudentRegistrationControllerTest {

    private static final Pattern CSRF_INPUT = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    private String username;

    @BeforeEach
    void setUp() {
        username = "page-test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void cleanUp() {
        studentRepository.findByUsername(username).ifPresent(studentRepository::delete);
    }

    @Test
    void displaysRegistrationPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create your account")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"username\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"password\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"confirmPassword\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Return to Homepage")));
    }

    @Test
    void acceptsValidRegistrationAndDoesNotRenderPasswordHash() throws Exception {
        MvcResult result = submitRegistration("campus-pass-123", "campus-pass-123");

        assertTrue(result.getResponse().getContentAsString().contains("Your student account has been created"));
        assertTrue(studentRepository.existsByUsername(username));
        assertTrue(!result.getResponse().getContentAsString().contains("$2a$"));
    }

    @Test
    void rejectsMismatchedPasswordsWithoutSavingAccount() throws Exception {
        MvcResult result = submitRegistration("campus-pass-123", "campus-pass-456");

        assertTrue(result.getResponse().getContentAsString().contains("The passwords do not match."));
        assertTrue(!studentRepository.existsByUsername(username));
    }

    @Test
    void displaysDuplicateUsernameError() throws Exception {
        MvcResult first = submitRegistration("campus-pass-123", "campus-pass-123");
        assertTrue(first.getResponse().getContentAsString().contains("Your student account has been created"));

        MvcResult duplicate = submitRegistration("another-pass-123", "another-pass-123");
        assertTrue(duplicate.getResponse().getContentAsString().contains("That username is already registered."));
    }

    @Test
    void displaysValidationErrorForInvalidPassword() throws Exception {
        MvcResult result = submitRegistration("short", "short");

        assertTrue(result.getResponse().getContentAsString().contains("Password must be at least 8 characters long."));
        assertTrue(!studentRepository.existsByUsername(username));
    }

    private MvcResult submitRegistration(String password, String confirmPassword) throws Exception {
        MvcResult page = mockMvc.perform(get("/register")).andExpect(status().isOk()).andReturn();
        Matcher matcher = CSRF_INPUT.matcher(page.getResponse().getContentAsString());
        assertTrue(matcher.find(), "Registration form should include a CSRF token");
        MockHttpSession session = (MockHttpSession) page.getRequest().getSession(false);

        return mockMvc.perform(post("/register")
                        .session(session)
                        .param("_csrf", matcher.group(1))
                        .param("username", username)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andExpect(status().isOk())
                .andReturn();
    }
}