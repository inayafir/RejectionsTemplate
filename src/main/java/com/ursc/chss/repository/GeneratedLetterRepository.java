package com.ursc.chss.repository;

import com.ursc.chss.model.GeneratedLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for GeneratedLetter entity.
 * Supports search and retrieval operations.
 */
@Repository
public interface GeneratedLetterRepository extends JpaRepository<GeneratedLetter, Long> {

    List<GeneratedLetter> findAllByOrderByCreatedAtDesc();

    List<GeneratedLetter> findByEmployeeStaffIdContainingIgnoreCase(String staffId);

    List<GeneratedLetter> findByIssueDate(LocalDate issueDate);

    @Query("SELECT l FROM GeneratedLetter l WHERE " +
           "LOWER(l.employee.staffId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(l.letterId AS string) LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(l.employee.employeeName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<GeneratedLetter> searchByQuery(String query);

    List<GeneratedLetter> findByIssueDateBetweenOrderByCreatedAtDesc(LocalDate from, LocalDate to);
}
