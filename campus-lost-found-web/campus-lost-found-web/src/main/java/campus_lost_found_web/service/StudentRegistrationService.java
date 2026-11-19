package campus_lost_found_web.service;

import campus_lost_found_web.entity.Student;
import campus_lost_found_web.repository.StudentRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentRegistrationService {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final Pattern USERNAME_FORMAT = Pattern.compile("[a-zA-Z0-9._-]+");

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StudentRegistrationService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public void register(String username, String password) {
        String normalizedUsername = validateUsername(username);
        validatePassword(password);

        if (studentRepository.existsByUsername(normalizedUsername)) {
            throw duplicateUsername();
        }

        Student student = Student.registeredStudent(
                normalizedUsername, passwordEncoder.encode(password));
        try {
            // Flush here so concurrent duplicate usernames are reported as a registration error.
            studentRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException exception) {
            throw duplicateUsername();
        }
    }

    private String validateUsername(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < MIN_USERNAME_LENGTH || normalized.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username must be between 3 and 32 characters.");
        }
        if (!USERNAME_FORMAT.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Username may contain only letters, numbers, periods, underscores, and hyphens.");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException("Password is too long for BCrypt (maximum 72 UTF-8 bytes).");
        }
    }

    private IllegalArgumentException duplicateUsername() {
        return new IllegalArgumentException("That username is already registered.");
    }
}