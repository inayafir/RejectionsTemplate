===============================================================================
LIBRARIES REQUIRED BY THE CHSS REJECTION LETTER MODULE
===============================================================================

The module itself only depends on iText 7 (for PDF generation) and the JDBC
APIs that Tomcat already provides. Copy the JARs below into the folder the
existing Sandesh project uses for libraries (typically lib/ or
web/WEB-INF/lib/).

Versions shown are the ones this module was built and verified against; later
8.x/9.x iText versions should also work.

1) iText 7 html2pdf + core modules
   Used by: src/com/ursc/chss/service/PdfGenerator.java
   Why: PdfGenerator.createPdf() converts the letter HTML to a PDF file using
   com.itextpdf.html2pdf.HtmlConverter on top of the iText 7 kernel.

   Required JARs:
   - html2pdf-5.0.2.jar           (HtmlConverter entry point)
   - kernel-8.0.2.jar             (PdfDocument / PdfWriter)
   - io-8.0.2.jar                 (low-level PDF I/O, required by kernel)
   - layout-8.0.2.jar             (element layout used by html2pdf)
   - forms-8.0.2.jar              (form handling used by html2pdf)
   - pdfa-8.0.2.jar               (transitive requirement of html2pdf)
   - commons-8.0.2.jar            (shared iText utilities)
   - styled-xml-parser-8.0.2.jar  (CSS parsing for the HTML template)
   - svg-8.0.2.jar                (required by html2pdf at runtime, even when
                                   the template has no SVG)

   NOT needed (the letter is English-only):
   - font-asian-8.0.2.jar         (only for CJK/other non-Latin fonts)

2) Logging bridge that iText needs
   Used by: PdfGenerator.java (indirectly, through iText's SLF4J logging)
   Why: iText 7 logs through SLF4J.
   - slf4j-api-2.0.x.jar
     If the Sandesh project already bundles slf4j-api, reuse that and skip
     copying this one.

3) MySQL JDBC driver
   Used by: src/com/ursc/chss/db/DatabaseAdapter.java (once the connection
   mechanism is copied in)
   Why: only required if the connection mechanism copied into
   DatabaseAdapter.java uses DriverManager directly. Sandesh already talks to
   MySQL, so this driver is almost certainly already in the project's lib/ -
   in that case do NOT copy it again.
   - mysql-connector-j-8.1.0.jar

Do NOT add these (already provided by Tomcat):
   - servlet-api.jar, jsp-api.jar, el-api.jar
   - JSTL jars (the JSPs use JSP scriptlets/EL, no JSTL)

Removed during the conversion (must NOT be added back):
   - spring-boot-*, spring-*, spring-data-*
   - hibernate-*, hibernate-community-dialects
   - h2, sqlite-jdbc
   - jackson-*
   - poi / poi-ooxml (Excel import/export was dead code)
   - thymeleaf-*
