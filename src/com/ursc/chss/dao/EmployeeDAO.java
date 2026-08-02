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
 * The table name and column names below are placeholders. After speaking with
 * the IT team, change the constants to the real employee table name and column
 * names. Both the SQL strings and the ResultSet mapping use these constants,
 * so editing them here is the ONLY change needed for employee lookup.
 * =========================================================================
 */
public class EmployeeDAO {

    // ----------------------------------------------------------------------
    // PLACEHOLDER table and column names - replace after talking to IT.
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Plain SQL - same shape as the existing Sandesh employee lookup module.
    // ----------------------------------------------------------------------

    /** SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER = ? */
    private static final String SQL_FIND_BY_STAFF_ID =
            "SELECT * FROM " + EMPLOYEE_TABLE +
            " WHERE " + COL_STAFF_NUMBER + " = ?";

    /** SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER LIKE ? OR EMPLOYEE_NAME LIKE ? ORDER BY EMPLOYEE_NAME */
    private static final String SQL_SEARCH =
            "SELECT * FROM " + EMPLOYEE_TABLE +
            " WHERE " + COL_STAFF_NUMBER + " LIKE ? OR " + COL_EMPLOYEE_NAME + " LIKE ?" +
            " ORDER BY " + COL_EMPLOYEE_NAME;

    /**
     * Finds a single employee by Staff Number (exact match).
     *
     * @param staffId the staff number, e.g. "ISO0012"
     * @return the employee, or {@code null} if not found
     */
    public Employee findById(String staffId) {
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STAFF_ID)) {
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
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
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
        emp.setStaffId(rs.getString(COL_STAFF_NUMBER));
        emp.setEmployeeName(rs.getString(COL_EMPLOYEE_NAME));
        emp.setDepartment(rs.getString(COL_DEPARTMENT));
        emp.setDesignation(rs.getString(COL_DESIGNATION));
        emp.setPhone(rs.getString(COL_PHONE));
        emp.setEmail(rs.getString(COL_EMAIL));
        emp.setAddressLine1(rs.getString(COL_ADDRESS_LINE_1));
        emp.setAddressLine2(rs.getString(COL_ADDRESS_LINE_2));
        emp.setLocality(rs.getString(COL_LOCALITY));
        emp.setCity(rs.getString(COL_CITY));
        emp.setPincode(rs.getString(COL_PINCODE));
        return emp;
    }
}
