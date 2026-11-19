package campus_lost_found_web.service;

import campus_lost_found_web.entity.ItemReportSummary;
import java.util.List;

public record StudentDashboardData(
        String username,
        long totalReports,
        long lostItems,
        long foundItems,
        long recoveredItems,
        List<ItemReportSummary> reports) {
}