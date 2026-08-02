# OFFICE_CHANGES — what gets edited when integrating into Sandesh

The module is designed so that, after the initial copy, almost **no** file needs
editing. This page lists the few files a developer edits in the office, what to
change in each, and what information IT must supply.

Nothing here is a business-logic change — the letter workflow, UI and PDF
layout are identical to the original application.

---

## Summary

| File | Why it must change | Who supplies the info | Lines |
|------|--------------------|-----------------------|-------|
| `src/com/ursc/chss/db/DatabaseAdapter.java` | Point the module at the real database connection mechanism | IT (how Sandesh opens connections) | 1 method |
| `src/com/ursc/chss/dao/EmployeeDAO.java` | Point at the real employee table and column names | IT (table + column names) | constants at top |
| `src/com/ursc/chss/service/PdfStorage.java` | Only if Sandesh must store PDFs somewhere other than `<webapp>/generated_letters` | IT / your decision | 1 method |
| `web/WEB-INF/sql/chss_schema.sql` | Run once to create the tables + seed the 18 reasons | IT (MySQL credentials, which instance) | run as-is |

---

## 1. `src/com/ursc/chss/db/DatabaseAdapter.java` — database connection

**Why:** the module does not know how the existing Sandesh project obtains
database connections (JNDI DataSource, DriverManager, a shared JDBC helper,
etc.).

**What to change:** copy the connection mechanism from an existing Sandesh
module into `DatabaseAdapter.getConnection()` and return a `java.sql.Connection`.
The placeholder is clearly marked with `SANDSH CONNECTION PLACEHOLDER - EDIT
HERE`.

**IT must supply:** how Sandesh opens connections (e.g. JNDI name in Tomcat, or
the JDBC URL/user/password, or the name of Sandesh's shared JDBC helper class).

**Scope:** every SQL query in the module already goes through this one method —
no other file needs changing for connectivity.

---

## 2. `src/com/ursc/chss/dao/EmployeeDAO.java` — employee lookup SQL

**Why:** the module reads the organisation's **existing** employee table
(read-only). Table and column names are placeholders.

**What to change:** the constants at the top of the class:

```java
EMPLOYEE_TABLE    = "EMPLOYEE_TABLE";   // → real table name
COL_STAFF_NUMBER  = "STAFF_NUMBER";     // → real column holding the Staff Number
COL_EMPLOYEE_NAME = "EMPLOYEE_NAME";
COL_DEPARTMENT    = "DEPARTMENT";
COL_DESIGNATION   = "DESIGNATION";
COL_PHONE         = "PHONE";
COL_EMAIL         = "EMAIL";
COL_ADDRESS_LINE_1 = "ADDRESS_LINE_1";
COL_ADDRESS_LINE_2 = "ADDRESS_LINE_2";
COL_LOCALITY      = "LOCALITY";
COL_CITY          = "CITY";
COL_PINCODE       = "PINCODE";
```

**IT must supply:** the employee table name and the actual column names for
Staff Number, name, department, designation, phone, email and address fields.

**Scope:** both the SQL strings and the `ResultSet` mapping use these constants,
so editing them here is the ONLY change needed for employee lookup. The
autocomplete JSON keys (`staffId`, `employeeName`, `addressLine1`, ...) do not
change, so no JavaScript changes when you remap columns.

**If some columns do not exist** (e.g. no email): the columns are not `NOT
NULL` in the module — only Staff Number and name are required. If a column is
missing entirely, drop it from both the constant and the
`mapEmployee(ResultSet)` lines that read it.

---

## 3. `src/com/ursc/chss/service/PdfStorage.java` — PDF storage directory

**Why:** only if `<webapp>/generated_letters` is not acceptable (e.g. Sandesh
uses a shared/central file area, or the webapp folder is not writable).

**What to change:** return a different directory from
`PdfStorage.resolveStorageDir(ServletContext)`, e.g. `"D:/CHSS_LETTERS"`. The
directory is created automatically. This is the **single** place — no other
file in the module knows where PDFs live.

**IT must supply:** a writable folder path if the default is not usable.

---

## 4. `web/WEB-INF/sql/chss_schema.sql` — run once

**Why:** creates the two MySQL tables the module owns (`rejection_reasons`,
`generated_letters`) and inserts the 18 standard rejection reasons.

**What to change:** nothing — run it as-is against the MySQL instance Sandesh
uses. The seed uses `INSERT IGNORE` and is idempotent, so re-running it is safe.

**IT must supply:** access to run the script (`mysql -u <user> -p <
chss_schema.sql` or equivalent).

**Notes:** if the 18 standard reasons are already present in Sandesh somewhere,
you can replace the seed block with a single statement that points at that
table/data instead — but keep the table/column shape the module expects
(`id`, `reason_number`, `description`, `active`).

---

## What you should NOT need to change

* Servlet URL patterns — they are relative to the context. Only rename them
  (see `web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt`) if they collide with an
  existing Sandesh servlet.
* JSP/JS — all links use `${pageContext.request.contextPath}` /
  `window.CONTEXT_PATH`; nothing depends on the deployment context.
* Business logic, letter text, PDF layout — unchanged from the original
  application.
