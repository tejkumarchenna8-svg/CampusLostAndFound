package campus_lost_found_web.entity;

import java.time.LocalDate;

public record ItemReportSummary(
        Long id,
        String reportType,
        ReportStatus status,
        String itemName,
        String category,
        String location,
        LocalDate reportDate,
        String description) {

    public static ItemReportSummary from(ItemReport report) {
        return new ItemReportSummary(report.getId(), report.getReportType(), report.getStatus(),
                report.getItemName(), report.getCategory(), report.getLocation(),
                report.getReportDate(), report.getDescription());
    }
}