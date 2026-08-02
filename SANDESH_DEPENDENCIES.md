# SANDESH_DEPENDENCIES — every remaining Sandesh assumption

This module was written to plug into the existing Sandesh intranet (Eclipse
Dynamic Web Project, Tomcat, MySQL, JSP/Servlets) with the **minimum** possible
integration effort. Below is an honest list of every assumption the module makes
about Sandesh, why it is made, what happens if it is wrong, and how to fix it.

If an assumption is not listed here, the module does not depend on it.

---

| # | Assumption about Sandesh | Why the module relies on it | If it is wrong | Fix |
|---|--------------------------|------------------------------|----------------|-----|
| 1 | Runs on a Servlet container that provides the **`javax.servlet`** API (Tomcat 8/9, or any pre-Tomcat-10 server) | Both servlets and the JSPs are written against `javax.servlet` / `javax.servlet.jsp` | Container is Tomcat 10+ (or a `jakarta.*` server) — servlet classes fail to load with `ClassNotFoundException`/`NoClassDefFoundError` | Replace `javax.servlet` → `jakarta.servlet` in the imports of `LetterServlet.java` and `EmployeeSearchServlet.java` (2 files) |
| 2 | Uses a **MySQL** database that Sandesh already connects to | The module's two tables (`rejection_reasons`, `generated_letters`) are MySQL schema | Database is not MySQL (e.g. PostgreSQL) | Convert `web/WEB-INF/sql/chss_schema.sql` to the target dialect (the SQL in the DAOs is already plain, parameterised JDBC and portable) |
| 3 | There is a **pre-existing employee table** with a unique Staff Number column | Employee lookup and the autocomplete read it read-only; the module never creates it | No employee table exists | Point `EmployeeDAO` at the real table/columns (see `OFFICE_CHANGES.md`); if there is no employee lookup table at all, that is a business decision, not a code change |
| 4 | **Database connections** are obtained the same way as an existing Sandesh module | All SQL goes through `DatabaseAdapter.getConnection()`, which is a placeholder until the mechanism is copied in | No connection mechanism is copied in yet | Every DB-touching page fails with `UnsupportedOperationException`. Fix: copy the Sandesh connection code into `DatabaseAdapter.getConnection()` (see `OFFICE_CHANGES.md`) |
| 5 | **iText 7 (`html2pdf`) + slf4j JARs** are present in the project's `lib/` (or `web/WEB-INF/lib/`) | `PdfGenerator.createPdf()` converts HTML to PDF | JARs missing | `NoClassDefFoundError` at PDF-generation time. Fix: copy the JARs listed in `lib/README.txt` |
| 6 | The **Servlet 3.0 annotation scanning** default is active (`web.xml` does NOT set `metadata-complete="true"`) | The two servlets are registered with `@WebServlet` annotations | Scanning is disabled | Register the same servlets in `web.xml` using the blocks in `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` |
| 7 | The webapp is **exploded and writable** so `getRealPath("/")` returns a real folder | Default PDF storage is `<webapp>/generated_letters` | Folder not writable, or webapp not exploded (`getRealPath` returns null) | Change `PdfStorage.resolveStorageDir()` to a writable path (see `OFFICE_CHANGES.md`) |
| 8 | The module is reached via its **own URLs**; the existing Sandesh servlets do not collide with the short patterns `/generate`, `/view/*`, `/edit/*`, `/download/*`, `/print/*`, `/regenerate/*`, `/employee-search` | JSP links/actions and the autocomplete `fetch` point at these relative URLs | A pattern collides with an existing Sandesh servlet (both would be registered → startup failure, or the wrong servlet handles requests) | Rename the patterns together — the full list of places is in `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` |
| 9 | **Authentication** is handled by Sandesh before this module is reached | The module has no login of its own by design | Unauthenticated users could reach `/generate` | Add URL filtering / role checks in Sandesh's existing security configuration (web.xml filter or the equivalent) |
| 10 | **Java 8 or newer** is available for the project | The module uses Java 8 APIs only (`java.time`, streams, `try-with-resources`); it compiles with `--release 8` and has no Java 9+ calls | Project is on Java 7 or older | Not supported — no code fix, the project would need Java 8+ |
| 11 | The JSP engine supports **EL 3.0 / JSP 2.3** (standard on Tomcat 8+) | JSPs use `${pageContext.request.contextPath}` EL and JSP scriptlets (no JSTL) | Very old container without these | Same as #1 — this indicates a pre-Tomcat-8 container; upgrade or accept the mismatch |
| 12 | The module's own MySQL tables are **created once** by running `chss_schema.sql` | The DAOs `SELECT`/`INSERT` against `rejection_reasons` and `generated_letters` | Tables not created | `SQLSyntaxErrorException` / table-not-found. Fix: run `web/WEB-INF/sql/chss_schema.sql` once |

---

## Explicitly NOT assumed (deliberately avoided)

* No Spring, Spring Boot, Hibernate, H2, SQLite, JSTL, Thymeleaf or any other
  framework — plain Servlets + JSP + JDBC only.
* No hardcoded context path — everything is relative to the deployed context.
* No startup listener / `web.xml` `<listener>` / runtime seeding — the 18
  reasons come from `chss_schema.sql` (idempotent `INSERT IGNORE`).
* No knowledge of Sandesh's folder layout, module structure, package names, or
  app name. Copy the `com/ursc/chss/**` package as-is.
* No assumption that an employee JSON API already exists — the module ships its
  own small `EmployeeSearchServlet`.
