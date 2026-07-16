package com.ursc.chss.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ursc.chss.model.Employee;
import com.ursc.chss.model.RejectionReason;
import com.ursc.chss.repository.EmployeeRepository;
import com.ursc.chss.repository.RejectionReasonRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Service
public class DataInitializationService {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    private final EmployeeRepository employeeRepository;
    private final RejectionReasonRepository rejectionReasonRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.sqlite.db-path:./staff_directory.db}")
    private String sqliteDbPath;

    @Value("${app.rejections.json-path:./Rejections.json}")
    private String rejectionsJsonPath;

    public DataInitializationService(EmployeeRepository employeeRepository,
                                     RejectionReasonRepository rejectionReasonRepository,
                                     ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.rejectionReasonRepository = rejectionReasonRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    @Transactional
    public void initializeData() {
        migrateRejectionReasons();
        migrateEmployees();
    }

    private void migrateRejectionReasons() {
        if (rejectionReasonRepository.count() > 0) {
            log.info("Rejection reasons already exist in database. Skipping migration.");
            return;
        }

        try {
            File jsonFile = new File(rejectionsJsonPath);
            if (!jsonFile.exists()) {
                log.warn("Rejections.json not found at: {}", jsonFile.getAbsolutePath());
                return;
            }

            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(
                    jsonFile, new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            List<Map<String, Object>> reasons = data.get("objection_points");
            if (reasons != null) {
                for (Map<String, Object> reason : reasons) {
                    Integer number = (Integer) reason.get("number");
                    String description = (String) reason.get("description");
                    rejectionReasonRepository.save(new RejectionReason(number, description));
                }
                log.info("Migrated {} rejection reasons from Rejections.json", reasons.size());
            }
        } catch (Exception e) {
            log.error("Error migrating rejection reasons: {}", e.getMessage());
        }
    }

    private void migrateEmployees() {
        if (employeeRepository.count() > 0) {
            log.info("Employees already exist in database. Skipping migration.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteDbPath)) {
            String sql = "SELECT staff_id, first_name, last_name, department, designation, " +
                         "phone, email, address_line1, address_line2, locality, city, pincode FROM staff";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                int count = 0;
                while (rs.next()) {
                    String staffId = rs.getString("staff_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String employeeName = (firstName != null ? firstName : "") +
                                          (lastName != null ? " " + lastName : "");

                    Employee emp = new Employee(
                            staffId,
                            employeeName.trim(),
                            rs.getString("department"),
                            rs.getString("designation"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address_line1"),
                            rs.getString("address_line2"),
                            rs.getString("locality"),
                            rs.getString("city"),
                            rs.getString("pincode")
                    );
                    employeeRepository.save(emp);
                    count++;
                }
                log.info("Migrated {} employees from SQLite to MySQL", count);
            }
        } catch (Exception e) {
            log.error("Error migrating employees from SQLite: {}", e.getMessage());
        }
    }
}
