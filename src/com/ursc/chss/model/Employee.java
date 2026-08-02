package com.ursc.chss.model;

/**
 * Plain-Java representation of an employee returned from the organisation's
 * existing employee table. No JPA/Hibernate annotations - populated manually
 * from JDBC {@code ResultSet}s in {@code com.ursc.chss.dao.EmployeeDAO}.
 */
public class Employee {

    private String staffId;
    private String employeeName;
    private String department;
    private String designation;
    private String phone;
    private String email;
    private String addressLine1;
    private String addressLine2;
    private String locality;
    private String city;
    private String pincode;

    public Employee() {
    }

    public Employee(String staffId, String employeeName, String department, String designation,
                    String phone, String email, String addressLine1, String addressLine2,
                    String locality, String city, String pincode) {
        this.staffId = staffId;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
        this.phone = phone;
        this.email = email;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.pincode = pincode;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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
}
