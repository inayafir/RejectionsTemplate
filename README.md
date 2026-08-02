# CHSS Rejection Letter Generator (Sandesh Module)

A JSP + Servlet module for the Sandesh intranet that generates CHSS rejection
letters for URSC Finance & Accounts. This is the Sandesh-compatible version of
the original application: every feature, workflow, UI element, validation, and
PDF layout is preserved. Only the backend has been reworked from a Spring Boot
standalone application into plain Servlets + JSPs + JDBC so it can be dropped
into the existing Sandesh project.

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
src/            Java classes + PDF template + Rejections.json (copy into Sandesh src/)
web/            JSPs, CSS, JS, schema SQL, servlet-mapping reference (copy into Sandesh web/)
lib/            List of JARs that must be copied into the Sandesh lib/ folder
Rejections.json Canonical list of the 18 rejection reasons (config)
```

## Files to copy into the Sandesh project

| Source (this repo)            | Destination (Sandesh project)                              |
|-------------------------------|------------------------------------------------------------|
| `src/com/ursc/chss/...`       | `<project>/src/com/ursc/chss/...`                          |
| `src/templates/Template.html` | `<project>/src/templates/Template.html` (PDF letter template) |
| `src/Rejections.json`         | `<project>/src/Rejections.json`                            |
| `web/generate.jsp`            | `<project>/web/generate.jsp`                               |
| `web/view-letter.jsp`         | `<project>/web/view-letter.jsp`                            |
| `web/css/style.css`           | `<project>/web/css/style.css`                              |
| `web/js/app.js`               | `<project>/web/js/app.js`                                  |
| `web/WEB-INF/sql/chss_schema.sql` | `<project>/web/WEB-INF/sql/chss_schema.sql`            |
| `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt` | read it: web.xml entries (or keep the `@WebServlet` annotations) |
| JARs listed in `lib/README.txt` | `<project>/lib/` (or `web/WEB-INF/lib/`)                 |

See **INTEGRATION_GUIDE.md** for step-by-step instructions.

## What you will likely change in the office

Only two files normally need editing to point the module at the real
environment:

### 1. Database connection — `src/com/ursc/chss/db/DatabaseAdapter.java`

The single database abstraction. Every SQL query goes through
`DatabaseAdapter.getConnection()`. Edit the constants (or swap the
`DriverManager` call for the Sandesh JNDI DataSource) in this one file.

### 2. Employee lookup SQL — `src/com/ursc/chss/dao/EmployeeDAO.java`

The module reads the organisation's pre-existing employee table. The table name
and column names are constants at the top of the class, e.g.:

```sql
SELECT STAFF_NUMBER, EMPLOYEE_NAME, ... FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER = ?
```

Change `EMPLOYEE_TABLE` and the column constants to match the real table. The
SQL is written in the same shape as the existing Sandesh employee lookup.

## Other configuration points

* `src/com/ursc/chss/listener/AppContextListener.java` — `resolveStorageDir()`
  chooses where generated PDF files are written (defaults to
  `<webapp>/generated_letters`).
* `web/WEB-INF/sql/chss_schema.sql` — creates the two module-owned MySQL tables
  (`rejection_reasons`, `generated_letters`). Run once.
* `Rejections.json` — the 18 rejection reasons. Seeded into
  `rejection_reasons` automatically on first startup (with built-in defaults as
  a fallback if the file is missing).

## What still depends on the Sandesh environment

* **MySQL schema/database** — the `chss_schema.sql` tables must be created in
  the MySQL instance Sandesh uses.
* **Employee table** — the organisation's existing employee table (read-only).
* **JARs** — MySQL driver, iText 7 (`html2pdf`), JSTL must be present in the
  project's `lib/` (see `lib/README.txt`).
* **Container servlet API** — compiled against `javax.servlet` (Tomcat 8/9).
  On Tomcat 10+/`jakarta`, the servlet imports must be renamed (`javax.*` →
  `jakarta.*`) and the jakarta JSTL jars used.
* **Entry point** — the module is reached via `GET <context>/generate`; link it
  from a Sandesh menu as appropriate. The JSPs use
  `${pageContext.request.contextPath}` everywhere, so no context path is
  hardcoded.

## Authentication

None is implemented. Sandesh authenticates users before this module is
accessed; user/role integration is intentionally out of scope for now.
