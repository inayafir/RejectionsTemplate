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
 * <p>Reasons are seeded automatically by {@code AppDataInitializer} on first
 * startup (from {@code Rejections.json} on the classpath, with built-in
 * defaults as fallback).
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

    /** Returns the number of rows in the table (used by the seeder). */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count rejection reasons", e);
        }
        return 0;
    }

    /** Inserts a new reason. Returns the generated id, or -1 on failure. */
    public long insert(int reasonNumber, String description) {
        String sql = "INSERT INTO " + TABLE + " (reason_number, description, active) VALUES (?, ?, 1)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reasonNumber);
            ps.setString(2, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert rejection reason", e);
        }
        return -1;
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
