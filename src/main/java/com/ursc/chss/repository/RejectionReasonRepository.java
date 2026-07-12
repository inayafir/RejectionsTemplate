package com.ursc.chss.repository;

import com.ursc.chss.model.RejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RejectionReason entity.
 */
@Repository
public interface RejectionReasonRepository extends JpaRepository<RejectionReason, Long> {

    List<RejectionReason> findByActiveTrueOrderByReasonNumberAsc();

    List<RejectionReason> findAllByOrderByReasonNumberAsc();
}
