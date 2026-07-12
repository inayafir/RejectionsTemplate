package com.ursc.chss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * DTO for the rejection letter generation form.
 */
public class LetterFormDto {

    private Long letterId;

    @NotBlank(message = "Staff ID is required")
    private String staffId;

    private String employeeName;
    private String addressLine1;
    private String addressLine2;
    private String locality;
    private String city;
    private String pincode;

    @NotBlank(message = "Issue date is required")
    private String issueDate;

    private List<String> medicalExpenseDates;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private List<String> selectedReasonIds;

    private List<String> customReasons;

    private String action;

    public LetterFormDto() {}

    public Long getLetterId() { return letterId; }
    public void setLetterId(Long letterId) { this.letterId = letterId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public List<String> getMedicalExpenseDates() { return medicalExpenseDates; }
    public void setMedicalExpenseDates(List<String> medicalExpenseDates) { this.medicalExpenseDates = medicalExpenseDates; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public List<String> getSelectedReasonIds() { return selectedReasonIds; }
    public void setSelectedReasonIds(List<String> selectedReasonIds) { this.selectedReasonIds = selectedReasonIds; }

    public List<String> getCustomReasons() { return customReasons; }
    public void setCustomReasons(List<String> customReasons) { this.customReasons = customReasons; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
