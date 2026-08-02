package com.ursc.chss.dao;

import com.ursc.chss.db.DatabaseAdapter;
import com.ursc.chss.model.RejectionReason;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the {@code rejection_reasons} configuration table.
 *
 * <p>The 18 standard reasons are seeded by {@code web/WEB-INF/sql/chss_schema.sql}
 * (INSERT IGNORE statements - safe to re-run), so there is no runtime seeding.
 * This class only reads.
 */
public class RejectionReasonDAO {

    private static final String TABLE = "rejection_reasons";

    /** Returns all active reasons ordered by reason number (1..18). */
    public List<RejectionReason> findAllActiveOrderByNumberAsc() {
        List<RejectionReason> reasons = new ArrayList<>();
        String sql = "SELECT id, reason_number, description, active FROM " + TABLE +
                     " WHERE active = 1 ORDER BY reason_number ASC";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reasons.add(mapReason(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rejection reasons", e);
        }
        return reasons;
    }

    /** Returns a single reason by primary key, or {@code null}. */
    public RejectionReason findById(Long id) {
        String sql = "SELECT id, reason_number, description, active FROM " + TABLE +
                     " WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReason(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rejection reason id " + id, e);
        }
        return null;
    }

    private RejectionReason mapReason(ResultSet rs) throws SQLException {
        RejectionReason reason = new RejectionReason();
        reason.setId(rs.getLong("id"));
        reason.setReasonNumber(rs.getInt("reason_number"));
        reason.setDescription(rs.getString("description"));
        reason.setActive(rs.getBoolean("active"));
        return reason;
    }
}
