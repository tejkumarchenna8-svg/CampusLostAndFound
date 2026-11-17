package campus_lost_found_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_reports")
public class ItemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String reportType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ReportStatus status;

    @Column(nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 160)
    private String location;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(nullable = false, length = 160)
    private String contactInformation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ItemReport() { }

    public ItemReport(String reportType, String itemName, String category, String location,
                      LocalDate reportDate, String description, String contactInformation) {
        this.reportType = reportType;
        this.status = "Lost".equals(reportType) ? ReportStatus.LOST : ReportStatus.FOUND;
        this.itemName = itemName;
        this.category = category;
        this.location = location;
        this.reportDate = reportDate;
        this.description = description;
        this.contactInformation = contactInformation;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = statusFromReportType();
        }
    }

    private ReportStatus statusFromReportType() {
        return "Found".equalsIgnoreCase(reportType) ? ReportStatus.FOUND : ReportStatus.LOST;
    }

    public Long getId() { return id; }
    public String getReportType() { return reportType; }
    public ReportStatus getStatus() { return status == null ? statusFromReportType() : status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public LocalDate getReportDate() { return reportDate; }
    public String getDescription() { return description; }
    public String getContactInformation() { return contactInformation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}