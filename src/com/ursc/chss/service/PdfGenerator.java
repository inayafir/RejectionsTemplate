package com.ursc.chss.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.ursc.chss.model.Employee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PDF generation helper for CHSS rejection letters.
 *
 * <p>Produces exactly the same layout and content as the original application:
 * the HTML comes from {@code templates/Template.html} (classpath) with the same
 * placeholders, and is converted to A4 PDF using iText7 {@code html2pdf}.
 *
 * <p>Requires the iText7 core + html2pdf jars in the Sandesh {@code lib/}
 * folder. See {@code lib/README.txt}.
 */
public final class PdfGenerator {

    private PdfGenerator() {
    }

    /**
     * Renders the rejection letter HTML (single language - English) from the
     * shared {@code Template.html} resource. This is byte-for-byte the same
     * output the original application produced.
     */
    public static String generateHtml(Employee employee, LocalDate issueDate,
                                      String expenseDates, Double amount,
                                      List<String> reasons) {
        try (InputStream in = PdfGenerator.class.getClassLoader()
                .getResourceAsStream("templates/Template.html")) {
            if (in == null) {
                throw new IllegalStateException("Template resource 'templates/Template.html' not found on classpath. "
                        + "Copy src/templates/Template.html into the Sandesh src/ folder.");
            }
            String template = new String(readAllBytes(in), StandardCharsets.UTF_8);

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
            template = template.replace("{{ subject }}",
                    "SUBJECT: Return of CHSS Claim for Reimbursement of Medical Expenses Under CHSS / CSMA Rule");

            String bodyText = "Your claim(s) towards reimbursement of medical expenses dated " +
                    escapeHtml(expenseDates.isEmpty() ? formattedDate : expenseDates) +
                    " for Rs. " + formattedAmount +
                    "/- is/are returned unpassed on account of reason(s) mentioned below:";
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
            throw new RuntimeException("Error reading PDF template", e);
        }
    }

    /**
     * Converts the given HTML to an A4 PDF file under {@code storageDir}.
     *
     * @param html       the letter HTML
     * @param storageDir directory in which the PDF will be created
     * @param fileName   the PDF file name
     * @return absolute path of the created PDF file
     */
    public static String createPdf(String html, String storageDir, String fileName) {
        try {
            Path dir = Paths.get(storageDir);
            Files.createDirectories(dir);
            Path pdfPath = dir.resolve(fileName);

            PdfWriter writer = new PdfWriter(pdfPath.toFile());
            try (PdfDocument pdfDoc = new PdfDocument(writer)) {
                pdfDoc.setDefaultPageSize(PageSize.A4);
                ConverterProperties props = new ConverterProperties();
                HtmlConverter.convertToPdf(html, pdfDoc, props);
            }
            return pdfPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    /**
     * Reads an entire stream into a byte array (Java 8 compatible - avoids
     * {@code InputStream.readAllBytes()}, which requires Java 9+).
     */
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static String[] splitAddress(String address) {
        if (address == null || address.isEmpty()) {
            return new String[]{"", "", ""};
        }
        String[] parts = address.split(",\\s*");
        String line1 = parts.length > 0 ? parts[0].trim() : "";
        String line2 = parts.length > 1 ? parts[1].trim() : "";
        String line3 = parts.length > 2 ? String.join(", ", Arrays.copyOfRange(parts, 2, parts.length)).trim() : "";
        return new String[]{line1, line2, line3};
    }

    private static String buildAddress(Employee emp) {
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

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
