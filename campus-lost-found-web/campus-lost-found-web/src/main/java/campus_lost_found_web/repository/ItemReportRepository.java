package campus_lost_found_web.repository;

import campus_lost_found_web.entity.ItemReport;
import campus_lost_found_web.entity.ReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {

    @Query("select count(report) from ItemReport report " +
            "where report.status = :status or " +
            "(report.status is null and report.reportType = :legacyReportType)")
    long countByStatusIncludingLegacy(@Param("status") ReportStatus status,
                                      @Param("legacyReportType") String legacyReportType);
    @Query("select report from ItemReport report " +
            "where (:itemName = '' or lower(report.itemName) like lower(concat('%', :itemName, '%'))) " +
            "and (:category = '' or report.category = :category) " +
            "and (:reportType = 'All' or report.reportType = :reportType) " +
            "order by report.createdAt desc")
    List<ItemReport> search(@Param("itemName") String itemName,
                            @Param("category") String category,
                            @Param("reportType") String reportType);
}