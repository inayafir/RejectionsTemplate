================================================================================
LIBRARIES REQUIRED BY THE CHSS REJECTION LETTER MODULE
================================================================================

Copy the following JAR files into the Sandesh web/WEB-INF/lib/ folder (or the
Sandesh lib/ folder, whichever the existing project uses). Versions shown are
the ones the module was built and verified against; later 8.x/9.x versions of
iText should also work.

1) MySQL JDBC driver (required for DatabaseAdapter)
   - mysql-connector-j-8.1.0.jar
     (also works: mysql-connector-java-8.0.x)

2) iText 7 html2pdf + core (required for PdfGenerator)
   - html2pdf-5.0.2.jar
   - kernel-8.0.2.jar
   - io-8.0.2.jar
   - layout-8.0.2.jar
   - forms-8.0.2.jar
   - pdfa-8.0.2.jar
   - commons-8.0.2.jar
   - styled-xml-parser-8.0.2.jar
   - svg-8.0.2.jar
   - font-asian-8.0.2.jar          (CJK font support used by the template)

3) Logging bridge iText depends on
   - slf4j-api-2.0.x.jar
     (if the Sandesh project already bundles slf4j, reuse it)

4) JSTL (used by generate.jsp / view-letter.jsp)
   - javax.servlet.jsp.jstl / jstl-1.2.jar   (Tomcat 8/9, javax namespace)
   - OR the jakarta JSTL jars                 (Tomcat 10+, jakarta namespace)

5) Servlet API (already provided by Tomcat - do NOT bundle)
   - servlet-api.jar is supplied by the container.

NOT required anymore (removed during the Sandesh conversion):
   - spring-boot-*, spring-*, spring-data-*
   - hibernate-*, hibernate-community-dialects
   - h2
   - sqlite-jdbc
   - jackson-*
   - poi / poi-ooxml (Excel import/export was dead code and was removed)
   - thymeleaf-*
