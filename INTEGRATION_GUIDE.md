# Integration Guide — CHSS Rejection Letter into Sandesh

This guide explains how to copy the CHSS Rejection Letter Generator module into
an existing Sandesh JSP/Servlet project and point it at the office environment.

The module is deliberately written as plain JSP + Servlet + JDBC so it plugs
into the existing project without any framework or standalone deployment. It has
no startup listener and no runtime seeding.

---

## 1. Which files go into `src/`

Copy the whole Java package and its resources into the project's source folder
(in Eclipse: the folder already configured as a Source Folder, typically
`src/`):

```
src/
├── com/ursc/chss/
│   ├── db/
│   │   └── DatabaseAdapter.java            (connection — edit in office)
│   ├── dao/
│   │   ├── EmployeeDAO.java                (employee SQL — edit in office)
│   │   ├── RejectionReasonDAO.java
│   │   └── GeneratedLetterDAO.java
│   ├── dto/
│   │   └── LetterFormDto.java
│   ├── model/
│   │   ├── Employee.java
│   │   ├── GeneratedLetter.java
│   │   └── RejectionReason.java
│   ├── servlet/
│   │   ├── EmployeeSearchServlet.java      (autocomplete JSON — /employee-search)
│   │   └── LetterServlet.java              (/generate + view/edit/download/print/regenerate)
│   └── service/
│       ├── PdfStorage.java                 (PDF storage dir — edit in office if needed)
│       ├── LetterService.java
│       └── PdfGenerator.java               (PDF generation, isolated)
└── templates/
    └── Template.html                       (PDF letter template — goes to WEB-INF/classes/templates/)
```

`templates/Template.html` is the only classpath resource. Because it sits
inside a source folder, Eclipse copies it to `web/WEB-INF/classes/templates/`,
which is exactly where `PdfGenerator` loads it from. There is no
`Rejections.json` anymore — the 18 reasons are seeded by the schema script.

## 2. Which files go into `web/`

```
web/
├── generate.jsp                            (main form + live preview)
├── view-letter.jsp                         (PDF viewer + Download/Print/Edit/Regenerate)
├── css/
│   └── style.css
├── js/
│   └── app.js
└── WEB-INF/
    ├── sql/
    │   └── chss_schema.sql                 (run once against the MySQL instance — also seeds the 18 reasons)
    └── CHSS_SERVLET_MAPPINGS.txt           (web.xml entries — see section 6)
```

## 3. Which libraries may need to be copied

The JARs are **not** shipped in this repository. Copy the ones listed in
`lib/README.txt` into the project's `lib/` folder (or `web/WEB-INF/lib/`,
whichever the existing Sandesh project uses):

* iText 7: `html2pdf-5.0.2.jar` + `kernel`, `io`, `layout`, `forms`, `pdfa`,
  `commons`, `styled-xml-parser`, `svg` (all 8.0.2) — required by
  `PdfGenerator.java`. `font-asian` is NOT needed (the letter is English-only).
* `slf4j-api-2.0.x.jar` — iText logs through SLF4J (reuse Sandesh's if present).
* `mysql-connector-j-8.1.0.jar` — only if the connection mechanism copied into
  `DatabaseAdapter.java` uses `DriverManager`. Sandesh already talks to MySQL,
  so this is usually already present.

The Servlet/JSP/EL APIs are provided by the container — do not bundle them.
No JSTL is needed; the JSPs use JSP scriptlets and EL.

## 4. How to update the employee SQL query

Open `src/com/ursc/chss/dao/EmployeeDAO.java`. The SQL is built from the
constants at the top of the class:

```java
private static final String EMPLOYEE_TABLE   = "EMPLOYEE_TABLE";
private static final String COL_STAFF_NUMBER  = "STAFF_NUMBER";
private static final String COL_EMPLOYEE_NAME = "EMPLOYEE_NAME";
// ... address/contact columns
```

Change these to the real employee table name and column names. The SQL is plain
and has the same shape as the existing Sandesh employee lookup:

```sql
SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER = ?   -- findById (exact)

... WHERE STAFF_NUMBER LIKE ? OR EMPLOYEE_NAME LIKE ? -- autocomplete
```

The JSON keys returned by `EmployeeSearchServlet` stay `staffId`,
`employeeName`, `addressLine1`, ... regardless of the SQL column names, so the
JavaScript needs no changes when you remap columns.

## 5. How to replace `DatabaseAdapter`

`src/com/ursc/chss/db/DatabaseAdapter.java` is the only file involved in
database connectivity. It currently throws
`UnsupportedOperationException` from a clearly-marked placeholder: copy the
database connection mechanism from an existing Sandesh module into
`getConnection()` (JNDI DataSource, DriverManager, or a shared JDBC helper —
whatever Sandesh uses), for example:

```java
Context ctx = new InitialContext();
DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/chss");
return ds.getConnection();
```

No other file reads connection settings.

## 6. How to compare against an existing Sandesh servlet

* Look at an existing Sandesh servlet to see which servlet API namespace the
  project uses: `javax.servlet.*` (Tomcat 8/9) or `jakarta.servlet.*`
  (Tomcat 10+). This module is written for `javax.servlet`; if the project uses
  `jakarta`, replace `javax.servlet` → `jakarta.servlet` in the imports of the
  two servlet classes.
* Check whether the project registers servlets in `web.xml`. If yes, use the
  mappings in `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` instead of (or in
  addition to) the `@WebServlet` annotations — never double-register the same
  URL pattern.
* If the project disables annotation scanning (`metadata-complete="true"` in
  `web.xml`), the web.xml registrations are **required**.
* Match the module's DB access style to the project (e.g. shared JDBC helper).
  The module isolates all JDBC inside the three `dao` classes, so adapting is
  localised.

There is no `web.xml` listener to register — the module has no startup
component.

## 7. How to verify the employee lookup

1. Run `web/WEB-INF/sql/chss_schema.sql` once (creates both tables and seeds
   the 18 reasons).
2. Open `GET <context>/generate`.
3. Type a known Staff Number (or partial name) in **Search Staff**. The
   autocomplete list should appear from the organisation's employee table.
4. Select an employee. Name and address must auto-fill, and the live preview
   must update.
5. Confirm the autocomplete endpoint directly:
   * `GET <context>/employee-search?q=ISO` → JSON array

If nothing appears, the usual cause is a wrong table/column name in
`EmployeeDAO` (section 4) or the connection mechanism still missing from
`DatabaseAdapter` (section 5).

## 8. How to verify PDF generation

1. On the generate page, select an employee, set dates and amount, choose at
   least one reason (or a custom reason), and click **Generate PDF**.
2. You are taken to `/view/<id>`, which streams the PDF inline (printed via
   `/print/<id>`).
3. Check the PDF layout and wording — it is produced from
   `src/templates/Template.html` and must look exactly like the original
   application's output.
4. Verify **Download** (attachment), **Print** (inline), **Edit** (form
   prefilled from the stored snapshot), and **Regenerate** (new PDF created).
5. The PDF file itself is written to `<webapp>/generated_letters/` (override in
   `PdfStorage.resolveStorageDir()`).

## 9. Common integration mistakes

* **Wrong servlet namespace** — mixing `javax`/`jakarta`. Match the container.
* **Forgetting `Template.html` in a source folder** — it must land in
  `WEB-INF/classes/templates/`; otherwise PDF generation throws "Template
  resource ... not found".
* **Not creating the MySQL tables / not seeding** — run
  `web/WEB-INF/sql/chss_schema.sql` once. Without `rejection_reasons`, the
  dropdown is empty; without `generated_letters`, generation fails. The 18
  reasons are seeded by the same script (`INSERT IGNORE` — re-running is safe).
* **Not adding the JARs** — iText 7 (+ svg) and slf4j must be in the project's
  `lib/`. Missing iText causes `NoClassDefFoundError` at PDF time.
* **DatabaseAdapter still a placeholder** — `getConnection()` throws
  `UnsupportedOperationException` until the Sandesh connection mechanism is
  copied in; all module pages that touch the database will fail until then.
* **Double-registering servlets** — same URL pattern in both `@WebServlet` and
  `web.xml` fails at startup. Use one mechanism.
* **Hardcoded context path in JavaScript** — keep `window.CONTEXT_PATH`
  (already wired in `generate.jsp`); do not change `fetch` URLs to absolute
  paths.
* **Employee table assumptions** — verify the real table/column names in
  `EmployeeDAO` before testing the autocomplete.
* **PDF storage dir not writable** — Tomcat user must be able to write to
  `<webapp>/generated_letters` (or change the directory in
  `PdfStorage`).
