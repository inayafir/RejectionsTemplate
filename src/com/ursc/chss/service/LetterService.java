package com.ursc.chss.service;

import com.ursc.chss.dao.EmployeeDAO;
import com.ursc.chss.dao.GeneratedLetterDAO;
import com.ursc.chss.dao.RejectionReasonDAO;
import com.ursc.chss.dto.LetterFormDto;
import com.ursc.chss.model.Employee;
import com.ursc.chss.model.GeneratedLetter;
import com.ursc.chss.model.RejectionReason;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for generating CHSS rejection letters. A plain Java service
 * class (no framework). Instantiated per request by the servlets; the DAOs are
 * stateless and every query opens/closes its own connection, so no shared
 * instance or startup wiring is needed.
 */
public class LetterService {

    private final EmployeeDAO employeeDAO;
    private final RejectionReasonDAO rejectionReasonDAO;
    private final GeneratedLetterDAO generatedLetterDAO;

    public LetterService(EmployeeDAO employeeDAO,
                         RejectionReasonDAO rejectionReasonDAO,
                         GeneratedLetterDAO generatedLetterDAO) {
        this.employeeDAO = employeeDAO;
        this.rejectionReasonDAO = rejectionReasonDAO;
        this.generatedLetterDAO = generatedLetterDAO;
    }

    public EmployeeDAO getEmployeeDAO() {
        return employeeDAO;
    }

    /** All active rejection reasons, ordered by reason number (1..18). */
    public List<RejectionReason> getAllRejectionReasons() {
        return rejectionReasonDAO.findAllActiveOrderByNumberAsc();
    }

    /** Loads a generated letter by id (employee reconstructed from snapshot). */
    public GeneratedLetter getLetterById(Long id) {
        return generatedLetterDAO.findById(id);
    }

    /**
     * Generates and persists a rejection letter, exactly as the original
     * application did: employee is looked up by Staff Number, the selected
     * reasons and custom reasons are resolved to text, the PDF is produced via
     * {@link PdfGenerator}, and the letter metadata is stored in MySQL.
     *
     * @param dto        form data
     * @param storageDir directory where the PDF file will be stored
     * @return the persisted letter (with generated letterId)
     */
    public GeneratedLetter generateLetter(LetterFormDto dto, String storageDir) throws Exception {
        Employee employee = employeeDAO.findById(dto.getStaffId());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found: " + dto.getStaffId());
        }

        List<String> selectedReasonTexts = new ArrayList<>();
        if (dto.getSelectedReasonIds() != null) {
            for (String id : dto.getSelectedReasonIds()) {
                if (id == null || id.trim().isEmpty() || "custom".equalsIgnoreCase(id.trim())) continue;
                try {
                    Long reasonId = Long.parseLong(id.trim());
                    RejectionReason reason = rejectionReasonDAO.findById(reasonId);
                    if (reason != null) {
                        selectedReasonTexts.add(reason.getDescription());
                    }
                } catch (NumberFormatException e) {
                    // ignore malformed reason ids, same as the original application
                }
            }
        }

        if (dto.getCustomReasons() != null) {
            selectedReasonTexts.addAll(
                    dto.getCustomReasons().stream()
                            .filter(s -> s != null && !s.trim().isEmpty())
                            .collect(Collectors.toList())
            );
        }

        if (selectedReasonTexts.isEmpty()) {
            throw new IllegalArgumentException("At least one rejection reason must be selected.");
        }

        LocalDate issueDate = LocalDate.parse(dto.getIssueDate());
        String expenseDates = "";
        if (dto.getMedicalExpenseDates() != null && !dto.getMedicalExpenseDates().isEmpty()) {
            expenseDates = dto.getMedicalExpenseDates().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> {
                        try {
                            return LocalDate.parse(s).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } catch (Exception e) {
                            return s;
                        }
                    })
                    .collect(Collectors.joining(", "));
        }

        String fileName = "CHSS_REJECTION_" + employee.getStaffId() + "_" +
                          System.currentTimeMillis() + ".pdf";

        String htmlContent = PdfGenerator.generateHtml(employee, issueDate, expenseDates,
                dto.getAmount(), selectedReasonTexts);
        String pdfPath = PdfGenerator.createPdf(htmlContent, storageDir, fileName);

        GeneratedLetter letter = new GeneratedLetter();
        letter.setEmployee(employee);
        letter.setIssueDate(issueDate);
        letter.setMedicalExpenseDates(expenseDates);
        letter.setAmount(dto.getAmount());
        letter.setSelectedReasons(String.join("||", selectedReasonTexts));
        letter.setSelectedReasonIds(dto.getSelectedReasonIds() != null
                ? dto.getSelectedReasonIds().stream().map(String::valueOf).collect(Collectors.joining(","))
                : "");
        letter.setCustomReasons(dto.getCustomReasons() != null
                ? dto.getCustomReasons().stream()
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .collect(Collectors.joining("||"))
                : null);
        letter.setPdfPath(pdfPath);
        letter.setCreatedAt(LocalDateTime.now());

        generatedLetterDAO.insert(letter);
        return letter;
    }
}
