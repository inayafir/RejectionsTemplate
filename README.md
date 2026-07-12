# CHSS Rejection Letter Generator

Automated generation of CHSS rejection letters for URSC Finance & Accounts.

## Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+

## Database Setup

1. Create a MySQL database:
   ```sql
   CREATE DATABASE chss_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Update database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## Configuration

| Property | Description | Default |
|---|---|---|
| `server.port` | Application port | 8080 |
| `app.letters.storage-path` | Directory for generated PDFs | `./generated_letters` |
| `app.sqlite.db-path` | Path to SQLite staff directory | `./staff_directory.db` |
| `app.rejections.json-path` | Path to rejections JSON file | `./Rejections.json` |

## Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Or run the `ChssApplication` main class from your IDE.

## Access

- Dashboard: `http://localhost:8080/`
- Generate Letter: `http://localhost:8080/generate`
- Letter History: `http://localhost:8080/history`
- Employee Management: `http://localhost:8080/employees`
- Rejection Reason Management: `http://localhost:8080/reasons`

## Data Migration

On first startup, the application automatically:
1. Creates all required database tables via Hibernate `ddl-auto=update`
2. Migrates employee records from `staff_directory.db` (SQLite) to MySQL
3. Loads standard rejection reasons from `Rejections.json` into MySQL

After migration, all data is served from MySQL. The SQLite file and JSON file are not read again.

## Features

- **Dashboard** - Clean overview with quick access to all functions
- **Employee Search** - Real-time autocomplete search by Staff ID or name
- **Live Preview** - Instant preview of the rejection letter as you fill the form
- **PDF Generation** - Generates professionally formatted PDFs from the official template
- **Letter History** - Searchable history with date range filtering
- **Employee Management** - Add, edit, delete employees with Excel import/export
- **Rejection Reason Management** - Add, edit, enable/disable rejection reasons
- **Responsive UI** - Professional government-style interface for all devices

## Pages

| Page | Route | Description |
|---|---|---|
| Dashboard | `/` | Home page with summary stats and navigation cards |
| Generate Letter | `/generate` | Form + live preview for creating rejection letters |
| View Letter | `/view/{id}` | View generated letter PDF with Download/Print/Edit/Regenerate |
| Letter History | `/history` | Search by Staff ID, Letter ID, name, or date range |
| Employees | `/employees` | Manage employee records |
| Add/Edit Employee | `/employees/add`, `/employees/edit/{id}` | Employee form |
| Rejection Reasons | `/reasons` | Manage rejection reasons |
| Add/Edit Reason | `/reasons/add`, `/reasons/edit/{id}` | Reason form |
