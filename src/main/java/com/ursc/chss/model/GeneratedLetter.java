package com.ursc.chss.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a generated rejection letter.
 * Stores all metadata and the PDF file path.
 */
@Entity
@Table(name = "generated_letters")
public class GeneratedLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "letter_id")
    private Long letterId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = false)
    private Employee employee;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "medical_expense_dates", columnDefinition = "TEXT")
    private String medicalExpenseDates;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "selected_reasons", columnDefinition = "TEXT", nullable = false)
    private String selectedReasons;

    @Column(name = "selected_reason_ids", columnDefinition = "TEXT")
    private String selectedReasonIds;

    @Column(name = "custom_reasons", columnDefinition = "TEXT")
    private String customReasons;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public GeneratedLetter() {}

    public Long getLetterId() { return letterId; }
    public void setLetterId(Long letterId) { this.letterId = letterId; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getMedicalExpenseDates() { return medicalExpenseDates; }
    public void setMedicalExpenseDates(String medicalExpenseDates) { this.medicalExpenseDates = medicalExpenseDates; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getSelectedReasons() { return selectedReasons; }
    public void setSelectedReasons(String selectedReasons) { this.selectedReasons = selectedReasons; }

    public String getSelectedReasonIds() { return selectedReasonIds; }
    public void setSelectedReasonIds(String selectedReasonIds) { this.selectedReasonIds = selectedReasonIds; }

    public String getCustomReasons() { return customReasons; }
    public void setCustomReasons(String customReasons) { this.customReasons = customReasons; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
