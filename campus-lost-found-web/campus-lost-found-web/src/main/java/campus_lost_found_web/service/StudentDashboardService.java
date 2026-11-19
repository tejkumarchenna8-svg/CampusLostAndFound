package campus_lost_found_web.service;

import campus_lost_found_web.entity.ItemReportSummary;
import campus_lost_found_web.entity.ReportStatus;
import campus_lost_found_web.entity.Student;
import campus_lost_found_web.repository.ItemReportRepository;
import campus_lost_found_web.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final ItemReportRepository itemReportRepository;

    public StudentDashboardService(StudentRepository studentRepository, ItemReportRepository itemReportRepository) {
        this.studentRepository = studentRepository;
        this.itemReportRepository = itemReportRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardData dashboardFor(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student account not found."));
        Long studentId = student.getId();
        List<ItemReportSummary> reports = itemReportRepository
                .findByReportedBy_IdOrderByCreatedAtDesc(studentId).stream()
                .map(ItemReportSummary::from)
                .toList();
        return new StudentDashboardData(student.getUsername(),
                itemReportRepository.countByReportedBy_Id(studentId),
                itemReportRepository.countByReportedBy_IdAndStatus(studentId, ReportStatus.LOST),
                itemReportRepository.countByReportedBy_IdAndStatus(studentId, ReportStatus.FOUND),
                itemReportRepository.countByReportedBy_IdAndStatus(studentId, ReportStatus.RECOVERED),
                reports);
    }
}