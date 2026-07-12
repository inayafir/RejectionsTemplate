package com.ursc.chss.model;

import jakarta.persistence.*;

/**
 * Entity representing a standard rejection reason.
 * Loaded initially from Rejections.json, then stored in MySQL for easy maintenance.
 */
@Entity
@Table(name = "rejection_reasons")
public class RejectionReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_number", nullable = false)
    private Integer reasonNumber;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public RejectionReason() {}

    public RejectionReason(Integer reasonNumber, String description) {
        this.reasonNumber = reasonNumber;
        this.description = description;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getReasonNumber() { return reasonNumber; }
    public void setReasonNumber(Integer reasonNumber) { this.reasonNumber = reasonNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
