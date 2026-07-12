package com.ursc.chss.repository;

import com.ursc.chss.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Employee entity.
 * Supports staff search and CRUD operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByStaffIdContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(
            String staffId, String employeeName);

    List<Employee> findByStaffIdContainingIgnoreCase(String staffId);

    List<Employee> findByEmployeeNameContainingIgnoreCase(String employeeName);

    boolean existsByStaffId(String staffId);
}
