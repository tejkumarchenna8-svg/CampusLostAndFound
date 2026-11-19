package campus_lost_found_web;

import campus_lost_found_web.service.StudentRegistrationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentRegistrationController {

    private final StudentRegistrationService registrationService;

    public StudentRegistrationController(StudentRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/student/login")
    public String studentLoginPage() {
        return "student-login";
    }

    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("username", "");
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String password,
                           @RequestParam(defaultValue = "") String confirmPassword,
                           Model model) {
        model.addAttribute("username", username);
        List<String> errors = new ArrayList<>();
        if (username.isBlank()) {
            errors.add("Enter a username.");
        }
        if (password.isBlank()) {
            errors.add("Enter a password.");
        }
        if (confirmPassword.isBlank()) {
            errors.add("Confirm your password.");
        } else if (!password.isBlank() && !Objects.equals(password, confirmPassword)) {
            errors.add("The passwords do not match.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errorMessage", String.join(" ", errors));
            return "register";
        }

        try {
            registrationService.register(username, password);
            model.addAttribute("successMessage", "Your student account has been created. You can now sign in.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (DataAccessException exception) {
            model.addAttribute("errorMessage", "We could not create your account right now. Please try again.");
        }
        return "register";
    }
}