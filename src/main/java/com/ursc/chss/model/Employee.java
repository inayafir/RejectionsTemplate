package com.ursc.chss.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing an employee in the CHSS system.
 * Mapped from the SQLite staff_directory.db during initial setup.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "staff_id", length = 20)
    @NotBlank(message = "Staff ID is required")
    private String staffId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "locality", length = 255)
    private String locality;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "pincode", length = 20)
    private String pincode;

    public Employee() {}

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
