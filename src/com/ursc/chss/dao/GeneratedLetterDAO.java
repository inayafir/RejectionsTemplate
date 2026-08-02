package com.ursc.chss.dao;

import com.ursc.chss.db.DatabaseAdapter;
import com.ursc.chss.model.Employee;
import com.ursc.chss.model.GeneratedLetter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data access for the module's own {@code generated_letters} table (MySQL).
 *
 * <p>Employee name and address are snapshotted into the row at generation time
 * so the module never depends on the organisation's employee table for
 * viewing, downloading, editing or regenerating a letter.
 */
public class GeneratedLetterDAO {

    private static final String TABLE = "generated_letters";

    private static final String INSERT_SQL =
            "INSERT INTO " + TABLE + " (" +
            " staff_id, employee_name, address_line_1, address_line_2, locality, city, pincode," +
            " issue_date, medical_expense_dates, amount, selected_reasons, selected_reason_ids," +
            " custom_reasons, pdf_path, created_at" +
            " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Inserts a generated letter. The generated {@code letterId} is set on the
     * passed object, which is also returned.
     */
    public GeneratedLetter insert(GeneratedLetter letter) {
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

            Employee emp = letter.getEmployee();
            ps.setString(1, letter.getEmployee().getStaffId());
            ps.setString(2, emp != null ? emp.getEmployeeName() : null);
            ps.setString(3, emp != null ? emp.getAddressLine1() : null);
            ps.setString(4, emp != null ? emp.getAddressLine2() : null);
            ps.setString(5, emp != null ? emp.getLocality() : null);
            ps.setString(6, emp != null ? emp.getCity() : null);
            ps.setString(7, emp != null ? emp.getPincode() : null);
            ps.setDate(8, java.sql.Date.valueOf(letter.getIssueDate()));
            ps.setString(9, letter.getMedicalExpenseDates());
            ps.setDouble(10, letter.getAmount());
            ps.setString(11, letter.getSelectedReasons());
            ps.setString(12, letter.getSelectedReasonIds());
            ps.setString(13, letter.getCustomReasons());
            ps.setString(14, letter.getPdfPath());
            ps.setTimestamp(15, Timestamp.valueOf(letter.getCreatedAt() != null
                    ? letter.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    letter.setLetterId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save generated letter", e);
        }
        return letter;
    }

    /**
     * Loads a letter by primary key. The {@link Employee} is reconstructed from
     * the snapshot columns stored in the row.
     *
     * @return the letter, or {@code null} if not found
     */
    public GeneratedLetter findById(Long letterId) {
        String sql = "SELECT letter_id, staff_id, employee_name, address_line_1, address_line_2," +
                     " locality, city, pincode, issue_date, medical_expense_dates, amount," +
                     " selected_reasons, selected_reason_ids, custom_reasons, pdf_path, created_at" +
                     " FROM " + TABLE + " WHERE letter_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, letterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapLetter(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load generated letter id " + letterId, e);
        }
        return null;
    }

    private GeneratedLetter mapLetter(ResultSet rs) throws SQLException {
        GeneratedLetter letter = new GeneratedLetter();
        letter.setLetterId(rs.getLong("letter_id"));

        Employee emp = new Employee();
        emp.setStaffId(rs.getString("staff_id"));
        emp.setEmployeeName(rs.getString("employee_name"));
        emp.setAddressLine1(rs.getString("address_line_1"));
        emp.setAddressLine2(rs.getString("address_line_2"));
        emp.setLocality(rs.getString("locality"));
        emp.setCity(rs.getString("city"));
        emp.setPincode(rs.getString("pincode"));
        letter.setEmployee(emp);

        LocalDate issueDate = rs.getDate("issue_date") != null
                ? rs.getDate("issue_date").toLocalDate() : null;
        letter.setIssueDate(issueDate);
        letter.setMedicalExpenseDates(rs.getString("medical_expense_dates"));
        letter.setAmount(rs.getDouble("amount"));
        letter.setSelectedReasons(rs.getString("selected_reasons"));
        letter.setSelectedReasonIds(rs.getString("selected_reason_ids"));
        letter.setCustomReasons(rs.getString("custom_reasons"));
        letter.setPdfPath(rs.getString("pdf_path"));
        Timestamp created = rs.getTimestamp("created_at");
        letter.setCreatedAt(created != null ? created.toLocalDateTime() : null);
        return letter;
    }
}
