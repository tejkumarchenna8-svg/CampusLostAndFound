package campus_lost_found_web.service;

import campus_lost_found_web.entity.ItemReport;
import campus_lost_found_web.entity.ItemReportSummary;
import campus_lost_found_web.entity.ReportStatus;
import campus_lost_found_web.repository.ItemReportRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemReportService {

    private static final Set<String> CATEGORIES = Set.of(
            "Electronics", "Books & stationery", "Clothing & accessories", "Keys & cards", "Other");
    private final ItemReportRepository repository;

    public ItemReportService(ItemReportRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ItemReport save(String reportType, String itemName, String category, String location,
                           String date, String description, String contactInformation) {
        String cleanType = required(reportType, "Report type", 8);
        if (!cleanType.equals("Lost") && !cleanType.equals("Found")) {
            throw new IllegalArgumentException("Choose a valid report type.");
        }
        String cleanName = required(itemName, "Item name", 120);
        String cleanCategory = required(category, "Category", 60);
        if (!CATEGORIES.contains(cleanCategory)) {
            throw new IllegalArgumentException("Choose a valid category.");
        }
        String cleanLocation = required(location, "Location", 160);
        String cleanDescription = required(description, "Description", 1500);
        String cleanContact = required(contactInformation, "Contact information", 160);
        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(required(date, "Date", 10));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Enter a valid date.");
        }
        return repository.save(new ItemReport(cleanType, cleanName, cleanCategory, cleanLocation,
                reportDate, cleanDescription, cleanContact));
    }

    @Transactional
    public ItemReportSummary markRecovered(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Choose a valid report.");
        }
        ItemReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("That report could not be found."));
        ReportStatus currentStatus = report.getStatus();
        if (currentStatus != ReportStatus.LOST && currentStatus != ReportStatus.FOUND) {
            throw new IllegalArgumentException("Only lost or found reports can be marked recovered.");
        }
        report.setStatus(ReportStatus.RECOVERED);
        return ItemReportSummary.from(repository.save(report));
    }

    @Transactional(readOnly = true)
    public DashboardStatistics dashboardStatistics() {
        long lostItems = repository.countByStatusIncludingLegacy(ReportStatus.LOST, "Lost");
        long foundItems = repository.countByStatusIncludingLegacy(ReportStatus.FOUND, "Found");
        long recoveredItems = repository.countByStatusIncludingLegacy(ReportStatus.RECOVERED, "");
        return new DashboardStatistics(repository.count(), lostItems, foundItems, recoveredItems);
    }
    @Transactional(readOnly = true)
    public List<ItemReportSummary> search(String itemName, String category, String reportType) {
        return repository.search(itemName, category, reportType).stream()
                .map(ItemReportSummary::from)
                .toList();
    }

    private String required(String value, String label, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        if (clean.length() > maxLength) {
            throw new IllegalArgumentException(label + " is too long.");
        }
        return clean;
    }
}