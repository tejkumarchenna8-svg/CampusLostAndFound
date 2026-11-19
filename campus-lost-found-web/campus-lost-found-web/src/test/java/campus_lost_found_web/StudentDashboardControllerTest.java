package campus_lost_found_web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import campus_lost_found_web.entity.ItemReport;
import campus_lost_found_web.entity.ReportStatus;
import campus_lost_found_web.repository.ItemReportRepository;
import campus_lost_found_web.repository.StudentRepository;
import campus_lost_found_web.service.ItemReportService;
import campus_lost_found_web.service.StudentRegistrationService;
import campus_lost_found_web.service.StudentDashboardData;
import campus_lost_found_web.service.StudentDashboardService;
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

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:student-dashboard-flow;DB_CLOSE_DELAY=-1",
        "spring.security.user.name=dashboard-admin",
        "spring.security.user.password=admin-test-password",
        "spring.security.user.roles=ADMIN"
})
@AutoConfigureMockMvc
class StudentDashboardControllerTest {

    private static final Pattern CSRF_INPUT = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");
    private static final String STUDENT_PASSWORD = "campus-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRegistrationService studentRegistrationService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ItemReportRepository itemReportRepository;

    @Autowired
    private ItemReportService itemReportService;

    @Autowired
    private StudentDashboardService studentDashboardService;

    private String username;
    private String otherUsername;
    private ItemReport ownLostReport;
    private ItemReport ownFoundReport;
    private ItemReport otherReport;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        username = "dash-user-" + suffix;
        otherUsername = "other-user-" + suffix;
        studentRegistrationService.register(username, STUDENT_PASSWORD);
        studentRegistrationService.register(otherUsername, STUDENT_PASSWORD);
        ownLostReport = createReport("Lost", "My missing notebook", username, "private-student-contact@example.test");
        ownFoundReport = createReport("Found", "My found umbrella", username, "private-student-contact@example.test");
        otherReport = createReport("Lost", "Another student's headphones", otherUsername, "other-private-contact@example.test");
        itemReportService.markRecovered(ownLostReport.getId());
    }

    @AfterEach
    void cleanUp() {
        if (ownLostReport != null) itemReportRepository.deleteById(ownLostReport.getId());
        if (ownFoundReport != null) itemReportRepository.deleteById(ownFoundReport.getId());
        if (otherReport != null) itemReportRepository.deleteById(otherReport.getId());
        if (username != null) studentRepository.findByUsername(username).ifPresent(studentRepository::delete);
        if (otherUsername != null) studentRepository.findByUsername(otherUsername).ifPresent(studentRepository::delete);
    }

    @Test
    void studentLoginOpensDashboardWithOnlyThatStudentsReports() throws Exception {
        MockHttpSession session = login("/student/login", "/student/login", username, STUDENT_PASSWORD,
                "/student/dashboard");
        MvcResult dashboardResult = mockMvc.perform(get("/student/dashboard").session(session))
                .andExpect(status().isOk())
                .andReturn();
        String page = dashboardResult.getResponse().getContentAsString();

        assertTrue(page.contains("Welcome,"));
        assertTrue(page.contains(username));
        StudentDashboardData data = studentDashboardService.dashboardFor(username);
        assertEquals(2, data.totalReports());
        assertEquals(0, data.lostItems());
        assertEquals(1, data.foundItems());
        assertEquals(1, data.recoveredItems());
        assertEquals(2, data.reports().size());
        assertTrue(page.contains("My missing notebook"));
        assertTrue(page.contains("My found umbrella"));
        assertFalse(page.contains("Another student's headphones"));
        assertFalse(page.contains("private-student-contact@example.test"));
        assertFalse(page.contains("other-private-contact@example.test"));
        assertFalse(page.contains("Mark as Recovered"));
        assertTrue(page.contains("/student/logout"));
        assertTrue(page.contains("student-dashboard"));
    }

    @Test
    void userCannotRecoverReportsButAdminLoginAndRecoveryStillWork() throws Exception {
        MockHttpSession studentSession = login("/student/login", "/student/login", username,
                STUDENT_PASSWORD, "/student/dashboard");
        MvcResult studentPage = mockMvc.perform(get("/student/dashboard").session(studentSession))
                .andExpect(status().isOk()).andReturn();
        String studentCsrf = csrfFrom(studentPage);
        mockMvc.perform(post("/admin/reports/{id}/recover", ownFoundReport.getId())
                        .session(studentSession).param("_csrf", studentCsrf))
                .andExpect(status().isForbidden());
        assertTrue(itemReportRepository.findById(ownFoundReport.getId()).orElseThrow().getStatus() == ReportStatus.FOUND);

        MockHttpSession adminSession = login("/admin", "/login", "dashboard-admin", "admin-test-password", "/search");
        MvcResult searchPage = mockMvc.perform(get("/search").session(adminSession)).andExpect(status().isOk()).andReturn();
        mockMvc.perform(post("/admin/reports/{id}/recover", ownFoundReport.getId())
                        .session(adminSession).param("_csrf", csrfFrom(searchPage)))
                .andExpect(status().is3xxRedirection())
                .andExpect(status().is3xxRedirection());
        assertTrue(itemReportRepository.findById(ownFoundReport.getId()).orElseThrow().getStatus() == ReportStatus.RECOVERED);
    }

    @Test
    void adminCannotOpenStudentDashboardAndAnonymousVisitorCannotOpenIt() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/login"));

        MockHttpSession adminSession = login("/admin", "/login", "dashboard-admin", "admin-test-password", "/search");
        mockMvc.perform(get("/student/dashboard").session(adminSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentAndAdminLogoutFlowsUseSeparateSessions() throws Exception {
        MockHttpSession studentSession = login("/student/login", "/student/login", username,
                STUDENT_PASSWORD, "/student/dashboard");
        MockHttpSession adminSession = login("/admin", "/login", "dashboard-admin", "admin-test-password", "/search");

        MvcResult studentDashboard = mockMvc.perform(get("/student/dashboard").session(studentSession))
                .andExpect(status().isOk()).andReturn();
        mockMvc.perform(post("/student/logout").accept("text/html").session(studentSession)
                        .param("_csrf", csrfFrom(studentDashboard)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/login?logout"));
        mockMvc.perform(get("/student/dashboard").session(studentSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/login"));

        MvcResult adminSearch = mockMvc.perform(get("/search").session(adminSession))
                .andExpect(status().isOk()).andReturn();
        mockMvc.perform(post("/admin/reports/{id}/recover", ownFoundReport.getId())
                        .session(adminSession).param("_csrf", csrfFrom(adminSearch)))
                .andExpect(status().is3xxRedirection());
        assertEquals(ReportStatus.RECOVERED,
                itemReportRepository.findById(ownFoundReport.getId()).orElseThrow().getStatus());

        MvcResult refreshedSearch = mockMvc.perform(get("/search").session(adminSession))
                .andExpect(status().isOk()).andReturn();
        mockMvc.perform(post("/logout").accept("text/html").session(adminSession)
                        .param("_csrf", csrfFrom(refreshedSearch)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?logout"));
        MvcResult publicAdminPage = mockMvc.perform(get("/admin"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession anonymousSession = (MockHttpSession) publicAdminPage.getRequest().getSession(false);
        mockMvc.perform(post("/admin/reports/{id}/recover", ownLostReport.getId())
                        .session(anonymousSession).param("_csrf", csrfFrom(publicAdminPage)))
                .andExpect(status().isUnauthorized());
        assertEquals(ReportStatus.RECOVERED,
                itemReportRepository.findById(ownLostReport.getId()).orElseThrow().getStatus());
    }
    private ItemReport createReport(String type, String itemName, String owner, String contact) {
        return itemReportService.save(type, itemName, "Books & stationery", "Main library", "2025-01-15",
                "Test item description", contact, owner);
    }

    private MockHttpSession login(String pageUrl, String processingUrl, String loginName, String password,
                                  String expectedRedirect) throws Exception {
        MvcResult loginPage = mockMvc.perform(get(pageUrl)).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) loginPage.getRequest().getSession(false);
        mockMvc.perform(post(processingUrl).session(session)
                        .param("_csrf", csrfFrom(loginPage))
                        .param("username", loginName)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirect));
        return session;
    }

    private String csrfFrom(MvcResult page) throws Exception {
        Matcher matcher = CSRF_INPUT.matcher(page.getResponse().getContentAsString());
        assertTrue(matcher.find(), "Rendered POST form should include a CSRF token");
        return matcher.group(1);
    }
}