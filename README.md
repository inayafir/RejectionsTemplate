# CHSS Rejection Letter Generator (Sandesh Module)

A JSP + Servlet module for the Sandesh intranet that generates CHSS rejection
letters for URSC Finance & Accounts. Every feature, workflow, UI element,
validation, and PDF layout is preserved; the module is written as plain
Servlets + JSPs + JDBC so it can be manually copied into the existing Sandesh
project (Eclipse project, Tomcat, MySQL).

## What the module does

* Searches the organisation's **existing employee table** by Staff Number /
  name (autocomplete) and auto-fills the employee name and address into the
  form.
* Builds a rejection letter with a live preview, selectable fixed rejection
  reasons (1-18) plus optional custom reasons, expense date(s) and amount.
* Generates an A4 PDF letter (iText 7 `html2pdf`) with the identical layout and
  wording of the original application.
* Lets the user view, download, print, edit, and regenerate a letter.

## Module layout

```
src/            Java classes + PDF template (copy into Sandesh src/)
web/            JSPs, CSS, JS, schema SQL, servlet-mapping reference (copy into Sandesh web/)
lib/            List of JARs that must be copied into the Sandesh lib/ folder
```

## Files to copy into the Sandesh project

| Source (this repo)            | Destination (Sandesh project)                              |
|-------------------------------|------------------------------------------------------------|
| `src/com/ursc/chss/...`       | `<project>/src/com/ursc/chss/...`                          |
| `src/templates/Template.html` | `<project>/src/templates/Template.html` (PDF letter template) |
| `web/generate.jsp`            | `<project>/web/generate.jsp`                               |
| `web/view-letter.jsp`         | `<project>/web/view-letter.jsp`                            |
| `web/css/style.css`           | `<project>/web/css/style.css`                              |
| `web/js/app.js`               | `<project>/web/js/app.js`                                  |
| `web/WEB-INF/sql/chss_schema.sql` | `<project>/web/WEB-INF/sql/chss_schema.sql`            |
| `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` | read it: web.xml entries (or keep the `@WebServlet` annotations) |
| JARs listed in `lib/README.txt` | `<project>/lib/` (or `web/WEB-INF/lib/`)                 |

See **INTEGRATION_GUIDE.md** for step-by-step instructions,
**OFFICE_CHANGES.md** for the files edited in the office, and
**SANDESH_DEPENDENCIES.md** for every remaining Sandesh assumption.

## What you will likely change in the office

Only three files normally need editing to point the module at the real
environment (plus the schema, which is run once):

### 1. Database connection — `src/com/ursc/chss/db/DatabaseAdapter.java`

The single database abstraction. Every SQL query goes through
`DatabaseAdapter.getConnection()`. It currently contains a clearly-marked
placeholder: copy the database connection mechanism from an existing Sandesh
module into this one method (JNDI DataSource, DriverManager, or a shared JDBC
helper - whatever Sandesh uses).

### 2. Employee lookup SQL — `src/com/ursc/chss/dao/EmployeeDAO.java`

The module reads the organisation's pre-existing employee table (read-only).
The table name and column names are placeholder constants at the top of the
class, used by plain SQL of the same shape as the existing Sandesh employee
lookup:

```sql
SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER = ?
```

Change `EMPLOYEE_TABLE` and the column constants to match the real table. The
SQL is written in the same shape as the existing Sandesh employee lookup.

### 3. PDF storage directory — `src/com/ursc/chss/service/PdfStorage.java`

The single place that decides where generated PDF files are written. Defaults
to `<webapp>/generated_letters`. If Sandesh stores files elsewhere, change
`PdfStorage.resolveStorageDir()` - no other file knows about storage.

## Other configuration points

* `web/WEB-INF/sql/chss_schema.sql` — creates the two module-owned MySQL tables
  (`rejection_reasons`, `generated_letters`) and seeds the 18 standard
  rejection reasons (idempotent `INSERT IGNORE`). Run once.
* `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` — web.xml entries, if the Sandesh
  project disables annotation scanning.

## What still depends on the Sandesh environment

* **MySQL schema/database** — the `chss_schema.sql` tables must be created in
  the MySQL instance Sandesh uses.
* **Employee table** — the organisation's existing employee table (read-only).
* **JARs** — iText 7 (`html2pdf`) + slf4j must be present in the project's
  `lib/` (see `lib/README.txt`). The MySQL driver is usually already present
  because Sandesh talks to MySQL.
* **Container servlet API** — compiled against `javax.servlet` (Tomcat 8/9).
  On Tomcat 10+/`jakarta`, the servlet imports must be renamed (`javax.*` →
  `jakarta.*`).
* **Entry point** — the module is reached via `GET <context>/generate`; link
  it from a Sandesh menu as appropriate. The servlet URL patterns are relative
  to the context and the JSPs use `${pageContext.request.contextPath}`
  everywhere, so no context path is hardcoded.

## Authentication

None is implemented. Sandesh authenticates users before this module is
accessed; user/role integration is intentionally out of scope for now.
