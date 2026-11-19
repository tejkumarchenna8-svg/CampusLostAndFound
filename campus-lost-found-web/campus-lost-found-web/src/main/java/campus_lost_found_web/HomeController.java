package campus_lost_found_web;

import campus_lost_found_web.entity.ItemReportSummary;
import campus_lost_found_web.service.ItemReportService;
import campus_lost_found_web.service.StudentDashboardData;
import campus_lost_found_web.service.StudentDashboardService;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private static final Set<String> CATEGORIES = Set.of(
            "Electronics", "Books & stationery", "Clothing & accessories", "Keys & cards", "Other");
    private final ItemReportService itemReportService;
    private final StudentDashboardService studentDashboardService;

    public HomeController(ItemReportService itemReportService, StudentDashboardService studentDashboardService) {
        this.itemReportService = itemReportService;
        this.studentDashboardService = studentDashboardService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("statistics", itemReportService.dashboardStatistics());
        return "index";
    }

    @GetMapping("/report-lost")
    public String reportLost(Model model) {
        addFormDefaults(model, "lastSeenLocation", "dateLost");
        return "report-lost";
    }

    @PostMapping("/report-lost")
    public String submitLostReport(@RequestParam(defaultValue = "") String itemName,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String lastSeenLocation,
            @RequestParam(defaultValue = "") String dateLost,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String contactInformation, Authentication authentication, Model model) {
        populateForm(model, itemName, category, lastSeenLocation, dateLost, description, contactInformation,
                "lastSeenLocation", "dateLost");
        return saveReport("Lost", itemName, category, lastSeenLocation, dateLost, description,
                contactInformation, authentication, model, "report-lost");
    }

    @GetMapping("/report-found")
    public String reportFound(Model model) {
        addFormDefaults(model, "foundLocation", "dateFound");
        return "report-found";
    }

    @PostMapping("/report-found")
    public String submitFoundReport(@RequestParam(defaultValue = "") String itemName,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String foundLocation,
            @RequestParam(defaultValue = "") String dateFound,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String contactInformation, Authentication authentication, Model model) {
        populateForm(model, itemName, category, foundLocation, dateFound, description, contactInformation,
                "foundLocation", "dateFound");
        return saveReport("Found", itemName, category, foundLocation, dateFound, description,
                contactInformation, authentication, model, "report-found");
    }

    private String saveReport(String type, String name, String category, String location, String date,
                              String description, String contact, Authentication authentication, Model model, String view) {
        try {
            String reporterUsername = hasRole(authentication, "ROLE_USER") ? authentication.getName() : null;
            itemReportService.save(type, name, category, location, date, description, contact, reporterUsername);
            model.addAttribute("successMessage", "Your " + type.toLowerCase()
                    + " item report has been saved and is now available in search.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (DataAccessException exception) {
            model.addAttribute("errorMessage", "We could not save your report right now. Please try again.");
        }
        return view;
    }

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String studentDashboard(Authentication authentication, Model model) {
        StudentDashboardData dashboard = studentDashboardService.dashboardFor(authentication.getName());
        model.addAttribute("dashboard", dashboard);
        return "student-dashboard";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String itemName,
                         @RequestParam(defaultValue = "") String category,
                         @RequestParam(defaultValue = "All") String reportType,
                         Authentication authentication, Model model) {
        String cleanName = itemName.trim();
        String cleanCategory = category.trim();
        String cleanType = reportType.trim();
        model.addAttribute("itemName", cleanName);
        model.addAttribute("category", cleanCategory);
        model.addAttribute("reportType", cleanType);
        model.addAttribute("isAdmin", hasAdminRole(authentication));
        model.addAttribute("isStudent", hasRole(authentication, "ROLE_USER"));
        if (cleanName.length() > 120 || (!cleanCategory.isEmpty() && !CATEGORIES.contains(cleanCategory))
                || !Set.of("All", "Lost", "Found").contains(cleanType)) {
            model.addAttribute("errorMessage", "Choose valid search filters and keep the item name under 120 characters.");
            model.addAttribute("reports", List.of());
            return "search";
        }
        try {
            List<ItemReportSummary> reports = itemReportService.search(cleanName, cleanCategory, cleanType);
            model.addAttribute("reports", reports);
        } catch (DataAccessException exception) {
            model.addAttribute("errorMessage", "Search is temporarily unavailable. Please try again.");
            model.addAttribute("reports", List.of());
        }
        return "search";
    }
    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    private boolean hasAdminRole(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    @GetMapping("/admin")
    public String adminAccess(Authentication authentication) {
        return hasAdminRole(authentication) ? "redirect:/search" : "admin";
    }

    @PostMapping("/admin/reports/{id}/recover")
    @PreAuthorize("hasRole('ADMIN')")
    public String markRecovered(@PathVariable Long id,
            @RequestParam(defaultValue = "") String itemName,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "All") String reportType,
            RedirectAttributes redirectAttributes) {
        try {
            ItemReportSummary recovered = itemReportService.markRecovered(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Report marked as recovered: " + recovered.itemName() + ".");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "We could not update that report. Please try again.");
        }
        redirectAttributes.addAttribute("itemName", itemName);
        redirectAttributes.addAttribute("category", category);
        redirectAttributes.addAttribute("reportType", reportType);
        return "redirect:/search";
    }

    private void addFormDefaults(Model model, String locationField, String dateField) {
        model.addAttribute("itemName", "");
        model.addAttribute("category", "");
        model.addAttribute(locationField, "");
        model.addAttribute(dateField, "");
        model.addAttribute("description", "");
        model.addAttribute("contactInformation", "");
    }

    private void populateForm(Model model, String itemName, String category, String location, String date,
                              String description, String contact, String locationField, String dateField) {
        model.addAttribute("itemName", itemName);
        model.addAttribute("category", category);
        model.addAttribute(locationField, location);
        model.addAttribute(dateField, date);
        model.addAttribute("description", description);
        model.addAttribute("contactInformation", contact);
    }
}