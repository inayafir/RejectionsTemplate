# CHSS Rejection Letter Generator

Automated generation of CHSS rejection letters for URSC Finance & Accounts.

## Prerequisites

- **Java 17+** (tested on Eclipse Adoptium JDK 17)
- **Maven 3.6+** (tested on Apache Maven 3.9.6)

No external database is required — the application uses an embedded H2 database by default (dev profile).

## Quick Start

```bash
# 1. Build the project (first run downloads dependencies)
mvn package -DskipTests

# 2. Run the application
java -jar target/chss-rejection-generator-1.0.0.jar
```

The application starts at **http://localhost:8080/**.

## Detailed Setup

### 1. Install Java 17

Download from [Eclipse Adoptium](https://adoptium.net/) and set `JAVA_HOME`:

```cmd
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
```

### 2. Install Maven

Download from [Apache Maven](https://maven.apache.org/download.cgi) (e.g. 3.9.6).

Extract to a folder and add `bin\` to your `PATH`.

### 3. Build

```cmd
mvn package -DskipTests
```

This creates `target/chss-rejection-generator-1.0.0.jar`.

### 4. Run

```cmd
java -jar target/chss-rejection-generator-1.0.0.jar
```

The app is ready when you see:
```
Tomcat started on port 8080 (http) with context path ''
Started ChssApplication in 10.298 seconds
```

### 5. Open

Navigate to **http://localhost:8080/** in your browser.

## Quick Updates (After Code Changes)

If you only changed static files (CSS, HTML templates, JS), you can avoid a full rebuild:

```cmd
mvn compile
jar uf target/chss-rejection-generator-1.0.0.jar -C target\classes static\css\style.css
jar uf target/chss-rejection-generator-1.0.0.jar -C target\classes templates\generate.html
java -jar target/chss-rejection-generator-1.0.0.jar
```

## Pages

| Page | Route | Description |
|---|---|---|
| Generate Letter | `/` or `/generate` | Create rejection letters with employee search and live preview |
| View Letter | `/view/{id}` | View generated PDF with Download, Print, Edit, Regenerate |

## Configuration

All configuration is in `src/main/resources/application.properties`.

| Property | Description | Default |
|---|---|---|
| `server.port` | Application port | 8080 |
| `app.letters.storage-path` | Directory for generated PDFs | `./generated_letters` |
| `app.sqlite.db-path` | Path to SQLite staff directory | `./staff_directory.db` |
| `app.rejections.json-path` | Path to rejections JSON file | `./Rejections.json` |

## Data

On first startup, the application automatically:
1. Creates database tables via Hibernate
2. Migrates employee records from `staff_directory.db` (SQLite) to H2
3. Loads standard rejection reasons from `Rejections.json` into H2
