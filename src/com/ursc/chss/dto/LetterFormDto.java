package com.ursc.chss.dto;

import java.util.List;

/**
 * Form-backing object for the "Generate Rejection Letter" page.
 * Plain Java bean - bound manually from request parameters in the servlet.
 */
public class LetterFormDto {

    private String staffId;
    private String employeeName;
    private String addressLine1;
    private String addressLine2;
    private String locality;
    private String city;
    private String pincode;
    private String issueDate;
    private List<String> medicalExpenseDates;
    private Double amount;
    private List<String> selectedReasonIds;
    private List<String> customReasons;

    public LetterFormDto() {}

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
}
