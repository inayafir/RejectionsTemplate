# CODE_MAP — CHSS Rejection Letter module

A navigation map of every file in the module, so the module can be debugged
without reading every Java file.

All URLs are scoped under `/chss/...` and prefixed with
`${pageContext.request.contextPath}` (JSP) / `window.CONTEXT_PATH` (JavaScript),
so nothing depends on the deployment context.

---

## 1. URL map (servlet → JSP)

| URL (relative to context)  | Method | Servlet                                | What happens |
|----------------------------|--------|----------------------------------------|--------------|
| `/chss/generate`           | GET    | `LetterServlet.showForm()`             | Forwards to `generate.jsp` |
| `/chss/generate`           | POST   | `LetterServlet.generateLetter()`       | Validates, generates letter, redirects to `/chss/view/{id}`; on error re-forwards to `generate.jsp` with an `error` attribute |
| `/chss/view/{id}`          | GET    | `LetterServlet.viewLetter()`           | Forwards to `view-letter.jsp` |
| `/chss/edit/{id}`          | GET    | `LetterServlet.editLetter()`           | Prefills `generate.jsp` from the stored snapshot (`editMode=true`) |
| `/chss/download/{id}`      | GET    | `LetterServlet.streamPdf(attachment)`  | Streams the PDF file with `Content-Disposition: attachment` |
| `/chss/print/{id}`         | GET    | `LetterServlet.streamPdf(inline)`      | Streams the PDF file inline (used by the `<iframe>` in `view-letter.jsp`) |
| `/chss/regenerate/{id}`    | POST   | `LetterServlet.regenerateLetter()`     | Creates a new letter from stored data, redirects to `/chss/view/{newId}` |
| `/chss/employee-search?q=` | GET    | `EmployeeSearchServlet.doGet()`        | Returns a JSON array of matching employees (autocomplete) |

JSPs forward to **no** servlet; they submit/link to the URLs above. The only
servlet that forwards to a JSP is `LetterServlet`.

---

## 2. Java classes

| Class | Why it exists | Who calls it |
|-------|---------------|--------------|
| `servlet.LetterServlet` | Main workflow: render form, generate/edit/view/regenerate, stream PDFs | `generate.jsp` (form POST + edit link), `view-letter.jsp` (view/download/print/edit/regenerate links); mapped via `@WebServlet` (or web.xml) |
| `servlet.EmployeeSearchServlet` | Backs the "Search Staff" autocomplete | JavaScript `fetch()` in `generate.jsp` |
| `listener.AppContextListener` | Runs at webapp startup: builds DAOs + `LetterService`, seeds reasons, sets the PDF storage dir as a context attribute | Servlet container (`@WebListener` / web.xml) |
| `service.LetterService` | All business logic: resolve reasons, format dates/amount, generate + persist letter | `LetterServlet`, `EmployeeSearchServlet` (via `getEmployeeDAO()`) |
| `service.PdfGenerator` | Renders `src/templates/Template.html` and converts it to an A4 PDF | `LetterService.generateLetter()` |
| `service.AppDataInitializer` | Seeds the `rejection_reasons` table on first startup from `Rejections.json` (built-in 18 defaults as fallback) | `AppContextListener.contextInitialized()` |
| `dao.EmployeeDAO` | SQL against the organisation's existing employee table (placeholder names) | `LetterService` (lookup by staff id), `EmployeeSearchServlet` (search) |
| `dao.RejectionReasonDAO` | SQL against the module's `rejection_reasons` table | `LetterService`, `AppDataInitializer` |
| `dao.GeneratedLetterDAO` | SQL against the module's `generated_letters` table | `LetterService` |
| `db.DatabaseAdapter` | THE single database-connection point; contains the Sandesh connection placeholder | All three DAOs (`DatabaseAdapter.getConnection()`) |
| `dto.LetterFormDto` | Form-backing bean bound from request parameters | `LetterServlet` (show/edit/bind) |
| `model.Employee` | Employee data holder (staff id, name, address, ...) | `EmployeeDAO`, `LetterService`, `PdfGenerator`, `GeneratedLetterDAO`, `EmployeeSearchServlet` |
| `model.GeneratedLetter` | Letter metadata holder, including the employee snapshot | `LetterService`, `GeneratedLetterDAO`, `LetterServlet` |
| `model.RejectionReason` | Rejection reason row holder (id, number, description, active) | `RejectionReasonDAO`, `LetterService`, reasons dropdown in `generate.jsp` |

---

## 3. JSP pages

| JSP | Submits to (servlet) | Forwarded back to it by (servlet) |
|-----|----------------------|-----------------------------------|
| `generate.jsp` | `LetterServlet` `POST /chss/generate` (the form); JS autocomplete calls `EmployeeSearchServlet` `GET /chss/employee-search?q=` | `LetterServlet` — `showForm()`, `editLetter()`, `renderFormError()` |
| `view-letter.jsp` | none (links/buttons only): `LetterServlet` `/chss/view/{id}` (iframe), `/chss/download/{id}`, `/chss/print/{id}`, `/chss/edit/{id}`, `/chss/regenerate/{id}` | `LetterServlet` — `viewLetter()` |

`view-letter.jsp` reads the `letter` request attribute (set by `viewLetter()`);
`generate.jsp` reads `letterForm`, `rejectionReasons`, `editMode`,
`selectedReasonIdsCsv`, `addressPreview`, `success`/`error` request attributes.

---

## 4. JavaScript

### `web/js/app.js` (loaded by `generate.jsp`)
| Function | Calls which endpoint | Notes |
|----------|----------------------|-------|
| `dismissToast()` | none | Closes the toast |
| `initLivePreview()` | none | Binds inputs to `updatePreview()` |
| `updatePreview()` | none | Client-side letter preview from form values |
| `getExpenseDates()` / `getReasons()` / `formatDate()` | none | Helpers for `updatePreview()` |
| `previewLetter()` | none | Scrolls to the preview |
| `initFormValidation()` | none | Client-side validation on submit |
| `initEditMode()` | none | Restores selected reasons in edit mode |
| `escapeHtml()` | none | HTML-escape helper |

### `generate.jsp` inline script
| Function | Calls which endpoint | Notes |
|----------|----------------------|-------|
| autocomplete handler | `GET /chss/employee-search?q=<query>` | Debounced `fetch` on staff search input |
| `selectEmployee(emp)` | none | Fills the hidden fields + employee card + preview |
| `addExpenseDate()` | none | Adds an expense-date row |
| `addReason(selectedId)` | none | Adds a reason row (used by edit mode) |
| `handleReasonChange(select)` | none | Shows/hides the custom-reason input |

---

## 5. SQL

Every query obtains its connection from `DatabaseAdapter.getConnection()`.

### Employee table (read-only, pre-existing) — `dao/EmployeeDAO.java`
| Query | Where it runs | Used for |
|-------|---------------|----------|
| `SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER = ?` | `findById()` | Employee lookup when generating/regenerating a letter |
| `SELECT * FROM EMPLOYEE_TABLE WHERE STAFF_NUMBER LIKE ? OR EMPLOYEE_NAME LIKE ? ORDER BY EMPLOYEE_NAME` | `searchEmployees()` | Autocomplete on the generate page |

Table/column names are placeholder constants at the top of `EmployeeDAO`.

### Module tables (MySQL) — `dao/RejectionReasonDAO.java`, `dao/GeneratedLetterDAO.java`
| Query | Where it runs | Used for |
|-------|---------------|----------|
| `SELECT id, reason_number, description, active FROM rejection_reasons WHERE active = 1 ORDER BY reason_number ASC` | `findAllActiveOrderByNumberAsc()` | Reasons dropdown in `generate.jsp` |
| `SELECT id, reason_number, description, active FROM rejection_reasons WHERE id = ?` | `findById()` | Resolving selected reason ids to text when generating |
| `SELECT COUNT(*) FROM rejection_reasons` | `count()` | Seeding check |
| `INSERT INTO rejection_reasons (reason_number, description, active) VALUES (?, ?, 1)` | `insert()` | Seeding |
| `INSERT INTO generated_letters (staff_id, employee_name, address_line_1, address_line_2, locality, city, pincode, issue_date, medical_expense_dates, amount, selected_reasons, selected_reason_ids, custom_reasons, pdf_path, created_at) VALUES (?, ...)` | `insert()` | Persisting a generated letter |
| `SELECT letter_id, staff_id, employee_name, address_line_1, address_line_2, locality, city, pincode, issue_date, medical_expense_dates, amount, selected_reasons, selected_reason_ids, custom_reasons, pdf_path, created_at FROM generated_letters WHERE letter_id = ?` | `findById()` | View/edit/download/print/regenerate |

Schema for these two tables: `web/WEB-INF/sql/chss_schema.sql`.

---

## 6. PDF generation — full request flow

```
generate.jsp
   │  user fills form (employee selected via autocomplete → /chss/employee-search)
   ▼  POST /chss/generate
LetterServlet.generateLetter()
   │  binds LetterFormDto, validates (employee chosen, ≥1 reason)
   ▼
LetterService.generateLetter(dto, storageDir)
   │  ├─ EmployeeDAO.findById(staffId)            ────►  MySQL (EMPLOYEE_TABLE)
   │  └─ RejectionReasonDAO.findById(id) × N      ────►  MySQL (rejection_reasons)
   │  resolves selected/custom reasons to text, formats dates/amount
   ▼
PdfGenerator.generateHtml(employee, issueDate, expenseDates, amount, reasons)
   │  reads src/templates/Template.html from the classpath,
   │  replaces {{ placeholders }} → letter HTML (in memory)
   ▼
PdfGenerator.createPdf(html, storageDir, fileName)
   │  iText 7 html2pdf writes A4 PDF file to <webapp>/generated_letters/
   ▼
GeneratedLetterDAO.insert(letter)                  ────►  MySQL (generated_letters)
   │  row saved, letterId assigned
   ▼
redirect → /chss/view/{letterId}
   ▼
view-letter.jsp  (loads from LetterServlet.viewLetter(), row via GeneratedLetterDAO.findById)
   │  <iframe src="/chss/print/{letterId}">
   ▼
LetterServlet.streamPdf(inline)
   │  GeneratedLetterDAO.findById(letterId), opens pdfPath, streams bytes
   ▼
Browser renders the PDF (Download uses the same stream with attachment disposition)
```

### Debugging tips
* **Employee dropdown empty / "Employee not found"** → check `DatabaseAdapter`
  (connection mechanism) and `EmployeeDAO` (table/column names).
* **Reasons dropdown empty** → `rejection_reasons` table not created/seeded
  (run `chss_schema.sql`; watch for the `[CHSS] Seeded ...` console line).
* **PDF generation error** → iText jars missing from `lib/`, or
  `src/templates/Template.html` not in `WEB-INF/classes/`.
* **404s on links/buttons** → a URL pattern is not registered (check the
  `@WebServlet` annotations vs web.xml — see `CHSS_SERVLET_MAPPINGS.txt`).
* **Any module failure** → the `[CHSS]` lines in the Tomcat console.
