package campus_lost_found_web.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import campus_lost_found_web.entity.Student;
import campus_lost_found_web.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class StudentRegistrationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private StudentRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        registrationService = new StudentRegistrationService(studentRepository);
    }

    @Test
    void registersStudentWithNormalizedUsername() {
        when(studentRepository.existsByUsername("alex.student")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register(" Alex.Student ", "campus-password-123");

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).saveAndFlush(studentCaptor.capture());
        assertEquals("alex.student", studentCaptor.getValue().getUsername());
    }

    @Test
    void storesBcryptHashInsteadOfPlainTextPassword() {
        when(studentRepository.existsByUsername("student01")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register("student01", "campus-password-123");

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).saveAndFlush(studentCaptor.capture());
        String storedPassword = studentCaptor.getValue().getPasswordHash();
        assertAll(
                () -> assertFalse(storedPassword.equals("campus-password-123")),
                () -> assertTrue(storedPassword.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")),
                () -> assertTrue(passwordEncoder.matches("campus-password-123", storedPassword)));
    }

    @Test
    void assignsOnlyTheUserRole() {
        when(studentRepository.existsByUsername("student01")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register("student01", "campus-password-123");

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).saveAndFlush(studentCaptor.capture());
        assertEquals("USER", studentCaptor.getValue().getRole());
    }

    @Test
    void rejectsDuplicateUsernameIncludingConcurrentInsertConflict() {
        when(studentRepository.existsByUsername("student01")).thenReturn(true);
        IllegalArgumentException existing = assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("student01", "campus-password-123"));
        assertTrue(existing.getMessage().contains("already registered"));
        verify(studentRepository, never()).saveAndFlush(any(Student.class));

        when(studentRepository.existsByUsername("student02")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenThrow(new DataIntegrityViolationException("unique username constraint"));
        IllegalArgumentException concurrent = assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("student02", "campus-password-123"));
        assertTrue(concurrent.getMessage().contains("already registered"));
    }

    @Test
    void rejectsInvalidRegistrationDetailsBeforeRepositoryAccess() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> registrationService.register("ab", "campus-password-123")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> registrationService.register("bad username", "campus-password-123")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> registrationService.register("student01", "short")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> registrationService.register("student01", "a".repeat(73))));
        verifyNoInteractions(studentRepository);
    }
}