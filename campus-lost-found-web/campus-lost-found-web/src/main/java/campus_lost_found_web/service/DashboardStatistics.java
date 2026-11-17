package campus_lost_found_web.service;

public record DashboardStatistics(
        long totalReports,
        long lostItems,
        long foundItems,
        long recoveredItems) {
}