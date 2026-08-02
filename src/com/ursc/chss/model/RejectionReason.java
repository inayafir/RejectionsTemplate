package com.ursc.chss.model;

/**
 * Plain-Java representation of a rejection reason row from the
 * {@code rejection_reasons} table.
 */
public class RejectionReason {

    private Long id;
    private Integer reasonNumber;
    private String description;
    private Boolean active;

    public RejectionReason() {
    }

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
