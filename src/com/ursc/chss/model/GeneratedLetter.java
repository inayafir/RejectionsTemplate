package com.ursc.chss.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Plain-Java representation of a generated rejection letter.
 *
 * <p>Employee information is snapshotted into the {@code generated_letters}
 * row at generation time (employee name and address columns), so a letter can
 * always be viewed / downloaded / edited / regenerated even if the employee
 * lookup table is temporarily unavailable.
 */
public class GeneratedLetter {

    private Long letterId;
    private Employee employee;
    private LocalDate issueDate;
    private String medicalExpenseDates;
    private Double amount;
    private String selectedReasons;
    private String selectedReasonIds;
    private String customReasons;
    private String pdfPath;
    private LocalDateTime createdAt;

    public GeneratedLetter() {
    }

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
