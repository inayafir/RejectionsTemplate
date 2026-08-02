package com.ursc.chss.dao;

import com.ursc.chss.db.DatabaseAdapter;
import com.ursc.chss.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the organisation's EXISTING employee table.
 *
 * <p>The employee table is NOT owned by this module and is NOT created by it.
 * It is the same pre-existing table used by the other Sandesh employee lookup
 * module.
 *
 * <p>=========================================================================
 * EMPLOYEE SQL PLACEHOLDER - EDIT HERE IN THE OFFICE
 * --------------------------------------------------------------------------
 * Change the five constants below to match the actual employee table name and
 * column names. This is the only file that needs changing for employee lookup.
 * =========================================================================
 */
public class EmployeeDAO {

    /** Name of the existing employee table. */
    private static final String EMPLOYEE_TABLE = "EMPLOYEE_TABLE";

    /** Column holding the Staff Number (unique). */
    private static final String COL_STAFF_NUMBER = "STAFF_NUMBER";

    /** Column holding the employee name. */
    private static final String COL_EMPLOYEE_NAME = "EMPLOYEE_NAME";

    private static final String COL_DEPARTMENT = "DEPARTMENT";
    private static final String COL_DESIGNATION = "DESIGNATION";
    private static final String COL_PHONE = "PHONE";
    private static final String COL_EMAIL = "EMAIL";
    private static final String COL_ADDRESS_LINE_1 = "ADDRESS_LINE_1";
    private static final String COL_ADDRESS_LINE_2 = "ADDRESS_LINE_2";
    private static final String COL_LOCALITY = "LOCALITY";
    private static final String COL_CITY = "CITY";
    private static final String COL_PINCODE = "PINCODE";

    private static final String SELECT_COLUMNS =
            COL_STAFF_NUMBER + ", " + COL_EMPLOYEE_NAME + ", " + COL_DEPARTMENT + ", " + COL_DESIGNATION +
            ", " + COL_PHONE + ", " + COL_EMAIL + ", " + COL_ADDRESS_LINE_1 + ", " + COL_ADDRESS_LINE_2 +
            ", " + COL_LOCALITY + ", " + COL_CITY + ", " + COL_PINCODE;

    /**
     * Finds a single employee by Staff Number (exact match).
     *
     * @param staffId the staff number, e.g. "ISO0012"
     * @return the employee, or {@code null} if not found
     */
    public Employee findById(String staffId) {
        String sql = "SELECT " + SELECT_COLUMNS +
                     " FROM " + EMPLOYEE_TABLE +
                     " WHERE " + COL_STAFF_NUMBER + " = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Employee lookup failed for staff id " + staffId, e);
        }
        return null;
    }

    /**
     * Searches employees by Staff Number or Name (partial, case-insensitive).
     * Used by the autocomplete on the generate page.
     *
     * @param query the partial staff number or name
     * @return matching employees (empty list if none)
     */
    public List<Employee> searchEmployees(String query) {
        List<Employee> employees = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return employees;
        }
        String sql = "SELECT " + SELECT_COLUMNS +
                     " FROM " + EMPLOYEE_TABLE +
                     " WHERE " + COL_STAFF_NUMBER + " LIKE ? OR " + COL_EMPLOYEE_NAME + " LIKE ?" +
                     " ORDER BY " + COL_EMPLOYEE_NAME;
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Employee search failed for query: " + query, e);
        }
        return employees;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setStaffId(rs.getString(1));
        emp.setEmployeeName(rs.getString(2));
        emp.setDepartment(rs.getString(3));
        emp.setDesignation(rs.getString(4));
        emp.setPhone(rs.getString(5));
        emp.setEmail(rs.getString(6));
        emp.setAddressLine1(rs.getString(7));
        emp.setAddressLine2(rs.getString(8));
        emp.setLocality(rs.getString(9));
        emp.setCity(rs.getString(10));
        emp.setPincode(rs.getString(11));
        return emp;
    }
}
