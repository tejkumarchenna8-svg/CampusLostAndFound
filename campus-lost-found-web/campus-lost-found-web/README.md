# Campus Lost & Found

A Spring Boot web application that helps a campus community report lost belongings, share found items, and search existing reports. Student accounts can track their own submissions, while administrator access is reserved for managing report recovery status.

## Problem and objective

Lost property is difficult to reunite with its owner when reports are scattered across notice boards, messages, or informal channels. This project provides a central website where campus users can report and search for items, and where students can review reports connected to their accounts.

## Features

### Students and visitors

- Browse the homepage and live report statistics.
- Submit lost-item and found-item reports.
- Search reports by item name, category, and report type (All, Lost, or Found).
- View public report details without exposing contact information in search results.
- Register a student account with a unique username and password.
- Sign in as a student and view a dashboard containing that student’s own linked reports and statistics.
- Sign out of the student account.

Reports submitted while a student is signed in are associated with that account. Older reports without an owner remain unassigned and are not shown as belonging to a student.

### Administrators

- Sign in through the Admin Login page.
- Mark eligible Lost or Found reports as Recovered.
- Recovery actions require the `ADMIN` role. Student accounts have the `USER` role and cannot use administrator recovery actions.

## Technologies

- Java 25
- Spring Boot 4.1.2
- Spring MVC and Spring Security
- Spring Data JPA
- Thymeleaf
- HTML and CSS
- Maven (Maven Wrapper included)
- H2 for local development
- PostgreSQL for the production profile

## Project structure

```text
campus-lost-found-web/
├── data/                              # Local file-based H2 database
├── mvnw, mvnw.cmd                     # Maven Wrapper scripts
├── pom.xml                             # Maven project and dependencies
└── src/
    ├── main/
    │   ├── java/campus_lost_found_web/
    │   │   ├── CampusLostFoundWebApplication.java
    │   │   ├── HomeController.java
    │   │   ├── StudentRegistrationController.java
    │   │   ├── config/                 # Spring Security configuration
    │   │   ├── entity/                 # Student and item report models
    │   │   ├── repository/             # Spring Data repositories
    │   │   └── service/                # Report, registration, and dashboard logic
    │   └── resources/
    │       ├── application.properties
    │       ├── application-prod.properties
    │       ├── static/css/site.css
    │       └── templates/              # Thymeleaf pages and shared fragments
    └── test/java/campus_lost_found_web/ # Application and feature tests
```

## Run locally on Windows

### Prerequisites

- JDK 25 installed and available on `PATH` (or set `JAVA_HOME`).
- A terminal opened in the project root, where `mvnw.cmd` is located.

### Start the application

In PowerShell or Command Prompt, run:

```powershell
.\mvnw.cmd spring-boot:run
```

When startup completes, open [http://localhost:8080](http://localhost:8080) in a browser. The default local configuration uses a file-based H2 database at `data/campus-lost-found`, so report data persists across restarts.

To stop the application, press `Ctrl+C` in the terminal.

### Run tests

```powershell
.\mvnw.cmd clean test
```

The project currently has 15 passing tests covering application startup, student registration, dashboard access and ownership, and security-related flows.

## Student registration and login

1. Open [http://localhost:8080/register](http://localhost:8080/register).
2. Choose a unique username and password, then submit the form.
3. Open [http://localhost:8080/student/login](http://localhost:8080/student/login) and sign in with the registered account.
4. Successful student sign-in redirects to `/student/dashboard`, where the student can view their linked reports and report statistics.
5. Use the student sign-out control to end the student session.

Student passwords are stored as BCrypt hashes. Student accounts receive the `USER` role; registration does not allow users to select an administrator role.

## Administrator access and security

Open [http://localhost:8080/admin](http://localhost:8080/admin) to reach the administrator sign-in page. The local configuration uses Spring Boot’s generated development password unless `SPRING_SECURITY_USER_PASSWORD` is provided through the environment. When a generated password is used, Spring Boot prints it in the application startup log. No administrator password is included in this repository or README.

The admin account has the `ADMIN` role. Administrator-only routes and the Mark as Recovered action require that role. Student login and logout use separate URLs from administrator login and logout. CSRF protection is enabled for form submissions.

For production, activate the `prod` profile and provide the database and administrator password through environment variables. The production configuration expects `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `SPRING_SECURITY_USER_PASSWORD`; it uses PostgreSQL. Keep these values out of source control and do not use local development credentials in production.

## Future enhancements

- Add optional item photos with file type and size validation.
- Add report ownership tools for students, with authorization checks.
- Add notifications when a matching item is reported.
- Add report history and administrative audit records.
- Add pagination and more search filters for larger report collections.
- Add deployment automation and operational monitoring.
