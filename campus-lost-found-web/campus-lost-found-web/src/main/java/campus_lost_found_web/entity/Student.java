package campus_lost_found_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.regex.Pattern;

@Entity
@Table(name = "students", uniqueConstraints =
        @UniqueConstraint(name = "uk_students_username", columnNames = "username"))
public class Student {

    private static final Pattern BCRYPT_HASH = Pattern.compile("\\A\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}\\z");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, length = 16, updatable = false)
    private String role;

    protected Student() { }

    /** Creates a student only when the supplied credential is already BCrypt encoded. */
    public static Student registeredStudent(String username, String bcryptPasswordHash) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (bcryptPasswordHash == null || !BCRYPT_HASH.matcher(bcryptPasswordHash).matches()) {
            throw new IllegalArgumentException("Student password must be BCrypt hashed.");
        }
        Student student = new Student();
        student.username = username;
        student.passwordHash = bcryptPasswordHash;
        student.role = "USER";
        return student;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}