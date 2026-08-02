package com.ursc.chss.servlet;

import com.ursc.chss.dto.LetterFormDto;
import com.ursc.chss.listener.AppContextListener;
import com.ursc.chss.model.GeneratedLetter;
import com.ursc.chss.service.LetterService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main workflow servlet of the CHSS Rejection Letter module. Replaces the
 * former Spring MVC controller; every mapping below existed in the original
 * application.
 *
 * <ul>
 *   <li>{@code GET  /generate}            - show the generate form</li>
 *   <li>{@code POST /generate}            - create a letter, redirect to /view/{id}</li>
 *   <li>{@code GET  /view/{id}}           - view a letter (PDF viewer)</li>
 *   <li>{@code GET  /edit/{id}}           - edit a letter (generate form, prefilled)</li>
 *   <li>{@code GET  /download/{id}}       - download the PDF</li>
 *   <li>{@code GET  /print/{id}}          - stream the PDF inline for printing</li>
 *   <li>{@code POST /regenerate/{id}}     - regenerate from the stored data</li>
 * </ul>
 *
 * <p>URL patterns are declared relative to the context root and the JSPs use
 * {@code ${pageContext.request.contextPath}} for every link/action, so the
 * module works under any Sandesh context path without hardcoding.
 */
@WebServlet(urlPatterns = {
        "/generate",
        "/view/*",
        "/edit/*",
        "/download/*",
        "/print/*",
        "/regenerate/*"
})
public class LetterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/generate".equals(path)) {
            showForm(request, response);
            return;
        }

        Long id = parseId(request, response);
        if (id == null) return;

        switch (path) {
            case "/view":
                viewLetter(request, response, id);
                break;
            case "/edit":
                editLetter(request, response, id);
                break;
            case "/download":
                streamPdf(request, response, id, "attachment");
                break;
            case "/print":
                streamPdf(request, response, id, "inline");
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/generate".equals(path)) {
            generateLetter(request, response);
            return;
        }

        if ("/regenerate".equals(path)) {
            Long id = parseId(request, response);
            if (id == null) return;
            regenerateLetter(request, response, id);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // ------------------------------------------------------------------
    // Form rendering
    // ------------------------------------------------------------------

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LetterService service = letterService(request);
        request.setAttribute("letterForm", new LetterFormDto());
        request.setAttribute("rejectionReasons", service.getAllRejectionReasons());
        request.getRequestDispatcher("/generate.jsp").forward(request, response);
    }

    private void editLetter(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        LetterService service = letterService(request);
        GeneratedLetter letter = service.getLetterById(id);
        if (letter == null) {
            redirectError(request, response, "Letter not found");
            return;
        }

        LetterFormDto dto = new LetterFormDto();
        dto.setLetterId(letter.getLetterId());
        dto.setStaffId(letter.getEmployee().getStaffId());
        dto.setEmployeeName(letter.getEmployee().getEmployeeName());
        dto.setAddressLine1(letter.getEmployee().getAddressLine1());
        dto.setAddressLine2(letter.getEmployee().getAddressLine2());
        dto.setLocality(letter.getEmployee().getLocality());
        dto.setCity(letter.getEmployee().getCity());
        dto.setPincode(letter.getEmployee().getPincode());
        dto.setIssueDate(letter.getIssueDate().toString());
        dto.setAmount(letter.getAmount());

        if (letter.getMedicalExpenseDates() != null && !letter.getMedicalExpenseDates().isEmpty()) {
            dto.setMedicalExpenseDates(Arrays.asList(letter.getMedicalExpenseDates().split(",\\s*")));
        }
        if (letter.getSelectedReasonIds() != null && !letter.getSelectedReasonIds().isEmpty()) {
            dto.setSelectedReasonIds(Arrays.stream(letter.getSelectedReasonIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (letter.getCustomReasons() != null && !letter.getCustomReasons().isEmpty()) {
            dto.setCustomReasons(Arrays.asList(letter.getCustomReasons().split("\\|\\|")));
        }

        request.setAttribute("letterForm", dto);
        request.setAttribute("rejectionReasons", service.getAllRejectionReasons());
        request.setAttribute("editMode", true);
        request.setAttribute("selectedReasonIdsCsv",
                dto.getSelectedReasonIds() != null ? String.join(",", dto.getSelectedReasonIds()) : "");
        request.getRequestDispatcher("/generate.jsp").forward(request, response);
    }

    private void viewLetter(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        LetterService service = letterService(request);
        GeneratedLetter letter = service.getLetterById(id);
        if (letter == null) {
            redirectError(request, response, "Letter not found");
            return;
        }
        request.setAttribute("letter", letter);
        request.setAttribute("reasons",
                letter.getSelectedReasons() != null ? Arrays.asList(letter.getSelectedReasons().split("\\|\\|"))
                        : new ArrayList<String>());
        request.getRequestDispatcher("/view-letter.jsp").forward(request, response);
    }

    // ------------------------------------------------------------------
    // Letter creation / regeneration
    // ------------------------------------------------------------------

    private void generateLetter(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LetterService service = letterService(request);
        LetterFormDto dto = bindForm(request);

        if (dto.getStaffId() == null || dto.getStaffId().trim().isEmpty()) {
            renderFormError(request, response, service, dto, "Please search and select an employee.");
            return;
        }

        boolean hasReason = hasSelectedReasons(dto) || hasCustomReasons(dto);
        if (!hasReason) {
            renderFormError(request, response, service, dto,
                    "Please select or enter at least one rejection reason.");
            return;
        }

        try {
            GeneratedLetter letter = service.generateLetter(dto, storageDir(request));
            response.sendRedirect(request.getContextPath() + "/view/" + letter.getLetterId());
        } catch (Exception e) {
            renderFormError(request, response, service, dto, "Error generating letter: " + e.getMessage());
        }
    }

    private void regenerateLetter(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        LetterService service = letterService(request);
        GeneratedLetter existing = service.getLetterById(id);
        if (existing == null) {
            redirectError(request, response, "Letter not found");
            return;
        }

        LetterFormDto dto = new LetterFormDto();
        dto.setStaffId(existing.getEmployee().getStaffId());
        dto.setIssueDate(existing.getIssueDate().toString());
        dto.setAmount(existing.getAmount());
        if (existing.getMedicalExpenseDates() != null && !existing.getMedicalExpenseDates().isEmpty()) {
            dto.setMedicalExpenseDates(Arrays.asList(existing.getMedicalExpenseDates().split(",\\s*")));
        }
        if (existing.getSelectedReasonIds() != null && !existing.getSelectedReasonIds().isEmpty()) {
            dto.setSelectedReasonIds(Arrays.stream(existing.getSelectedReasonIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (existing.getCustomReasons() != null && !existing.getCustomReasons().isEmpty()) {
            dto.setCustomReasons(Arrays.asList(existing.getCustomReasons().split("\\|\\|")));
        }

        try {
            GeneratedLetter letter = service.generateLetter(dto, storageDir(request));
            response.sendRedirect(request.getContextPath() + "/view/" + letter.getLetterId());
        } catch (Exception e) {
            redirectError(request, response, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // PDF streaming
    // ------------------------------------------------------------------

    private void streamPdf(HttpServletRequest request, HttpServletResponse response, Long id,
                           String disposition) throws IOException {
        LetterService service = letterService(request);
        GeneratedLetter letter = service.getLetterById(id);
        if (letter == null || letter.getPdfPath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(letter.getPdfPath());
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        if ("attachment".equals(disposition)) {
            String filename = "CHSS_REJECTION_" + letter.getEmployee().getStaffId() + "_" + letter.getLetterId() + ".pdf";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        } else {
            response.setHeader("Content-Disposition", "inline; filename=\"letter.pdf\"");
        }

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private LetterService letterService(HttpServletRequest request) {
        LetterService service = (LetterService) request.getServletContext()
                .getAttribute(AppContextListener.KEY_LETTER_SERVICE);
        if (service == null) {
            throw new IllegalStateException("CHSS module not initialised");
        }
        return service;
    }

    private String storageDir(HttpServletRequest request) {
        return (String) request.getServletContext().getAttribute(AppContextListener.KEY_STORAGE_DIR);
    }

    private Long parseId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing letter id");
            return null;
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid letter id");
            return null;
        }
    }

    private LetterFormDto bindForm(HttpServletRequest request) {
        LetterFormDto dto = new LetterFormDto();

        String letterId = request.getParameter("letterId");
        if (letterId != null && !letterId.trim().isEmpty()) {
            try {
                dto.setLetterId(Long.parseLong(letterId.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        dto.setStaffId(trimToNull(request.getParameter("staffId")));
        dto.setEmployeeName(trimToNull(request.getParameter("employeeName")));
        dto.setAddressLine1(trimToNull(request.getParameter("addressLine1")));
        dto.setAddressLine2(trimToNull(request.getParameter("addressLine2")));
        dto.setLocality(trimToNull(request.getParameter("locality")));
        dto.setCity(trimToNull(request.getParameter("city")));
        dto.setPincode(trimToNull(request.getParameter("pincode")));
        dto.setIssueDate(trimToNull(request.getParameter("issueDate")));

        String amount = request.getParameter("amount");
        if (amount != null && !amount.trim().isEmpty()) {
            try {
                dto.setAmount(Double.parseDouble(amount.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        dto.setMedicalExpenseDates(listParam(request, "medicalExpenseDates"));
        dto.setSelectedReasonIds(listParam(request, "selectedReasonIds"));
        dto.setCustomReasons(listParam(request, "customReasons"));
        dto.setAction(trimToNull(request.getParameter("action")));
        return dto;
    }

    private List<String> listParam(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if (values == null) return new ArrayList<>();
        return Arrays.asList(values);
    }

    private boolean hasSelectedReasons(LetterFormDto dto) {
        if (dto.getSelectedReasonIds() == null) return false;
        for (String id : dto.getSelectedReasonIds()) {
            if (id != null && !id.trim().isEmpty() && !"custom".equalsIgnoreCase(id.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCustomReasons(LetterFormDto dto) {
        if (dto.getCustomReasons() == null) return false;
        for (String custom : dto.getCustomReasons()) {
            if (custom != null && !custom.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void renderFormError(HttpServletRequest request, HttpServletResponse response,
                                 LetterService service, LetterFormDto dto, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("letterForm", dto);
        request.setAttribute("rejectionReasons", service.getAllRejectionReasons());
        request.getRequestDispatcher("/generate.jsp").forward(request, response);
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String enc = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        response.sendRedirect(request.getContextPath() + "/generate?error=" + enc);
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
