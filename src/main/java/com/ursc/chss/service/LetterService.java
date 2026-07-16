package com.ursc.chss.service;

import com.ursc.chss.dto.LetterFormDto;
import com.ursc.chss.model.Employee;
import com.ursc.chss.model.GeneratedLetter;
import com.ursc.chss.model.RejectionReason;
import com.ursc.chss.repository.EmployeeRepository;
import com.ursc.chss.repository.GeneratedLetterRepository;
import com.ursc.chss.repository.RejectionReasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LetterService {

    private static final Logger log = LoggerFactory.getLogger(LetterService.class);

    private final EmployeeRepository employeeRepository;
    private final RejectionReasonRepository rejectionReasonRepository;
    private final GeneratedLetterRepository generatedLetterRepository;

    @Value("${app.letters.storage-path:./generated_letters}")
    private String storagePath;

    public LetterService(EmployeeRepository employeeRepository,
                         RejectionReasonRepository rejectionReasonRepository,
                         GeneratedLetterRepository generatedLetterRepository) {
        this.employeeRepository = employeeRepository;
        this.rejectionReasonRepository = rejectionReasonRepository;
        this.generatedLetterRepository = generatedLetterRepository;
    }

    public List<RejectionReason> getAllRejectionReasons() {
        return rejectionReasonRepository.findByActiveTrueOrderByReasonNumberAsc();
    }

    public List<GeneratedLetter> getAllLetters() {
        return generatedLetterRepository.findAllByOrderByCreatedAtDesc();
    }

    public GeneratedLetter getLetterById(Long id) {
        return generatedLetterRepository.findById(id).orElse(null);
    }

    public List<GeneratedLetter> searchLetters(String query) {
        return generatedLetterRepository.searchByQuery(query);
    }

    public List<GeneratedLetter> searchLettersByDateRange(LocalDate from, LocalDate to) {
        return generatedLetterRepository.findByIssueDateBetweenOrderByCreatedAtDesc(from, to);
    }

    @Transactional
    public GeneratedLetter generateLetter(LetterFormDto dto) throws Exception {
        Employee employee = employeeRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + dto.getStaffId()));

        List<String> selectedReasonTexts = new ArrayList<>();
        if (dto.getSelectedReasonIds() != null) {
            for (String id : dto.getSelectedReasonIds()) {
                if (id == null || id.trim().isEmpty() || "custom".equalsIgnoreCase(id.trim())) continue;
                try {
                    Long reasonId = Long.parseLong(id.trim());
                    RejectionReason reason = rejectionReasonRepository.findById(reasonId).orElse(null);
                    if (reason != null) {
                        selectedReasonTexts.add(reason.getDescription());
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid reason ID: {}", id);
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
        Path lettersDir = Paths.get(storagePath);
        Files.createDirectories(lettersDir);
        Path pdfPath = lettersDir.resolve(fileName);

        String htmlContent = generateHtml(employee, issueDate, expenseDates, dto.getAmount(), selectedReasonTexts);

        try (OutputStream os = new FileOutputStream(pdfPath.toFile())) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(pdfPath.toFile());
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
            com.itextpdf.html2pdf.ConverterProperties props = new com.itextpdf.html2pdf.ConverterProperties();
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(htmlContent, pdfDoc, props);
            pdfDoc.close();
        }

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
        letter.setPdfPath(pdfPath.toAbsolutePath().toString());

        GeneratedLetter saved = generatedLetterRepository.save(letter);
        log.info("Generated rejection letter {} for employee {}", saved.getLetterId(), employee.getStaffId());
        return saved;
    }

    private String generateHtml(Employee employee, LocalDate issueDate,
                                 String expenseDates, Double amount,
                                 List<String> reasons) {
        try {
            String template = new String(
                    getClass().getClassLoader().getResourceAsStream("templates/Template.html").readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String address = buildAddress(employee);
            String[] addressParts = splitAddress(address);

            String reasonsHtml = reasons.stream()
                    .map(r -> "<li>" + escapeHtml(r) + "</li>")
                    .collect(Collectors.joining("\n        "));

            String formattedDate = issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String formattedAmount = String.format("%.0f", amount);

            template = template.replace("{{ org_name }}", "U R Rao Satellite Centre");
            template = template.replace("{{ dept_name }}", "Finance and Accounts");
            template = template.replace("{{ staff_label }}", "Staff No:");
            template = template.replace("{{ date_label }}", "Date:");
            template = template.replace("{{ subject }}", "SUBJECT: Return of CHSS Claim for Reimbursement of Medical Expenses Under CHSS / CSMA Rule");

            String bodyText = "Your claim(s) towards reimbursement of medical expenses dated " +
                escapeHtml(expenseDates.isEmpty() ? formattedDate : expenseDates) +
                " for Rs. " + formattedAmount + "/- is/are returned unpassed on account of reason(s) mentioned below:";
            template = template.replace("{{ body_text }}", bodyText);

            template = template.replace("{{ officer_title }}", "Senior Accounts Officer");
            template = template.replace("{{ to_label }}", "To,");
            template = template.replace("{{ staff_id }}", escapeHtml(employee.getStaffId()));
            template = template.replace("{{ issue_date }}", formattedDate);
            template = template.replace("{{ medical_expense_dates }}", escapeHtml(expenseDates));
            template = template.replace("{{ amount }}", formattedAmount);
            template = template.replace("{{ employee_name }}", escapeHtml(employee.getEmployeeName()));
            template = template.replace("{{ address_line1 }}", escapeHtml(addressParts[0]));
            template = template.replace("{{ address_line2 }}", escapeHtml(addressParts[1]));
            template = template.replace("{{ address_line3 }}", escapeHtml(addressParts[2]));

            String reasonsBlock = "    <ol>\n" +
                "        {% for reason in reasons %}\n" +
                "        <li>{{ reason }}</li>\n" +
                "        {% endfor %}\n" +
                "    </ol>";
            String reasonsReplacement = "    <ol>\n" + reasonsHtml + "\n    </ol>";
            template = template.replace(reasonsBlock, reasonsReplacement);

            return template;
        } catch (IOException e) {
            throw new RuntimeException("Error reading template file", e);
        }
    }

    private String[] splitAddress(String address) {
        if (address == null || address.isEmpty()) {
            return new String[]{"", "", ""};
        }
        String[] parts = address.split(",\\s*");
        String line1 = parts.length > 0 ? parts[0].trim() : "";
        String line2 = parts.length > 1 ? parts[1].trim() : "";
        String line3 = parts.length > 2 ? String.join(", ", Arrays.copyOfRange(parts, 2, parts.length)).trim() : "";
        return new String[]{line1, line2, line3};
    }

    private String buildAddress(Employee emp) {
        StringBuilder sb = new StringBuilder();
        if (emp.getAddressLine1() != null && !emp.getAddressLine1().isEmpty())
            sb.append(emp.getAddressLine1()).append(", ");
        if (emp.getAddressLine2() != null && !emp.getAddressLine2().isEmpty())
            sb.append(emp.getAddressLine2()).append(", ");
        if (emp.getLocality() != null && !emp.getLocality().isEmpty())
            sb.append(emp.getLocality()).append(", ");
        if (emp.getCity() != null && !emp.getCity().isEmpty())
            sb.append(emp.getCity()).append(" - ");
        if (emp.getPincode() != null && !emp.getPincode().isEmpty())
            sb.append(emp.getPincode());

        String result = sb.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    public void deleteLetter(Long id) {
        GeneratedLetter letter = generatedLetterRepository.findById(id).orElse(null);
        if (letter != null) {
            try {
                Files.deleteIfExists(Paths.get(letter.getPdfPath()));
            } catch (IOException e) {
                log.warn("Could not delete PDF file: {}", letter.getPdfPath());
            }
            generatedLetterRepository.deleteById(id);
        }
    }
}
