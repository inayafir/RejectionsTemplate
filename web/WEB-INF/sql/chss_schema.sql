-- ============================================================================
-- CHSS Rejection Letter Generator - MySQL schema
-- ----------------------------------------------------------------------------
-- Tables owned by THIS module only.
--
-- The employee data is NOT created here - the module reads the organisation's
-- pre-existing employee table (see EmployeeDAO.java for the SQL placeholders).
--
-- The 18 standard rejection reasons are seeded below with INSERT IGNORE. They
-- are safe to re-run any number of times: the UNIQUE KEY on reason_number
-- makes every duplicate insert a no-op. There is no runtime auto-seeding and
-- no startup listener in the module.
--
-- Run this script once against the MySQL instance that Sandesh uses:
--     mysql -u <user> -p < chss_schema.sql
-- ============================================================================

CREATE TABLE IF NOT EXISTS rejection_reasons (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reason_number INT          NOT NULL,
    description   TEXT         NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    UNIQUE KEY uk_reason_number (reason_number)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS generated_letters (
    letter_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id               VARCHAR(20)   NOT NULL,
    employee_name          VARCHAR(200)  NULL,
    address_line_1         VARCHAR(255)  NULL,
    address_line_2         VARCHAR(255)  NULL,
    locality               VARCHAR(255)  NULL,
    city                   VARCHAR(100)  NULL,
    pincode                VARCHAR(20)   NULL,
    issue_date             DATE          NOT NULL,
    medical_expense_dates  TEXT          NULL,
    amount                 DECIMAL(12,2) NOT NULL,
    selected_reasons       TEXT          NOT NULL,
    selected_reason_ids    TEXT          NULL,
    custom_reasons         TEXT          NULL,
    pdf_path               VARCHAR(500)  NULL,
    created_at             DATETIME      NOT NULL,
    INDEX idx_staff_id (staff_id),
    INDEX idx_issue_date (issue_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Seed data: the 18 standard CHSS rejection reasons (idempotent).
-- ============================================================================

INSERT IGNORE INTO rejection_reasons (reason_number, description, active) VALUES
(1, 'The claim has not been submitted within the prescribed time limit, i.e., three months from the date of consultation/prescription. Hence approval from Administration is required.', 1),
(2, 'Medicines purchased are inadmissible under CHSS/CSMA rules.', 1),
(3, 'The Medical claim/s is/are not filled properly by the employee.', 1),
(4, 'The prescription is not signed/sealed by AMO.', 1),
(5, 'Kindly fill the attached Declaration Form and forward to CHSS-Accounts along with the claim for processing.', 1),
(6, 'The date of the cash bill is prior to the prescription date.', 1),
(7, 'Direct payment made to the empanelled Lab or Hospital cannot be reimbursed directly by Accounts. Please submit your claim with a justification letter to the Administrative Officer-CHSS for approval by the Competent Authority.', 1),
(8, 'Doctor has not written the Diagnosis/Dosage of the Medicines/Injections in the Prescription form.', 1),
(9, 'Please obtain endorsement of your AMO (in the back-to-back Form and on the back side of the cash bills) for the treatment taken from the Hospital.', 1),
(10, 'Treatment taken from an unauthorised/unempanelled Hospital/Lab is not reimbursable under CHSS. If it is an emergency claim, please submit your claim with a justification letter to the Administrative Officer-CHSS for approval by the Competent Authority.', 1),
(11, 'Annual CHSS Subscription for the calendar year has not been received. Hence unable to process your Medical Claims.', 1),
(12, 'Please enclose the ORIGINAL Prescription along with the Medical claim/s and forward it to CHSS-Accounts for processing the claim.', 1),
(13, 'Doctor has not mentioned the duration for the medicines in the Prescription.', 1),
(14, 'For beneficiaries going on outstation trips, medicines purchased for more than one month require copies of the onward and return journey tickets to be attached along with the medical claim.', 1),
(15, 'For beneficiaries travelling abroad, please attach copies of both onward and return tickets along with a copy of the VISA when submitting the medical claim.', 1),
(16, 'There is a delay in purchasing the medicines. As per the rules, medicines must be purchased within SEVEN DAYS from the date of the prescription.', 1),
(17, 'The beneficiary has attained the age of 25 years. Hence the medical claim cannot be processed if the consultation date is after attaining 25 years of age.', 1),
(18, 'AMO cannot prescribe medicines for more than ONE Month. Please obtain justification for issuing a prescription for more than ONE Month.', 1);
