package campus_lost_found_web.repository;

import campus_lost_found_web.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByUsername(String username);

    Optional<Student> findByUsername(String username);
}