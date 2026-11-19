package campus_lost_found_web.service;

import campus_lost_found_web.entity.Student;
import campus_lost_found_web.repository.StudentRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Student-only account lookup. This class is deliberately not a Spring bean:
 * registering a global UserDetailsService would disable Spring Boot's existing
 * generated/configured administrator user auto-configuration.
 */
public class StudentUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    public StudentUserDetailsService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        Student student = studentRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Student account not found."));
        if (!"USER".equals(student.getRole())) {
            throw new UsernameNotFoundException("Student account not found.");
        }
        return User.withUsername(student.getUsername())
                .password(student.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}