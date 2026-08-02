-- ============================================================================
-- CHSS Rejection Letter Generator - MySQL schema
-- ----------------------------------------------------------------------------
-- Tables owned by THIS module only.
--
-- The employee data is NOT created here - the module reads the organisation's
-- pre-existing employee table (see EmployeeDAO.java for the SQL placeholders).
--
-- The rejection_reasons table is auto-seeded on first startup from
-- Rejections.json (see AppDataInitializer.java). You do not need to insert the
-- reasons yourself; this script only guarantees the table exists.
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
