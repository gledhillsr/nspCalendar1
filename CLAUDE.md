# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NSP Online Calendar is a multi-tenant web application for managing ski patrol shift scheduling across multiple ski patrol organizations. Built with Java 7/8, Servlet API 2.5, and MySQL, it provides volunteer sign-up, shift management, and administrative tools.

## Build & Development Commands

### Build
```bash
mvn clean package
```
Produces `target/calendar-1.war`

### Testing
```bash
# Unit tests only
mvn test

# Run specific test
mvn test -Dtest=ClassName#testMethod

# Integration tests (starts Jetty on port 7070)
mvn clean package -Dintegration.test
```

**Important**: Integration tests use the `integration.test` property to activate the integration-test profile, which starts an embedded Jetty server on port 7070. The application context is accessible at `http://localhost:7070/`

## Architecture

### Servlet-Based Multi-Tenant Design

The application uses a servlet-based architecture where **all requests must include a `resort` parameter** to identify which ski patrol organization's data to access. The `resort` parameter maps to database table prefixes and configuration in `PatrolData.resortMap`.

### Resource (Servlet) Organization

Servlets in `src/main/java/org/nsponline/calendar/resources/` follow a naming convention:

- **`Outer*.java`**: Public pages requiring no authentication (e.g., `OuterVolunteerForm`, `OuterMemberLogin`)
- **`Inner*.java`**: Authenticated pages requiring valid session credentials (e.g., `InnerChangeShiftAssignments`, `InnerEditShiftTemplates`)
- **`ApiResources.java`**: REST API endpoints that return JSON and require `Authorization` header with session token

All servlets extend `ResourceBase` which provides:
- `initBase()`: Validates resort parameter and initializes database connection
- `initBaseAndRequireValidSession()`: Additionally validates session token (for API endpoints)
- `initBaseAndAskForValidCredentials()`: Redirects to login if credentials invalid (for Inner pages)

### Data Layer

**Store classes** (`src/main/java/org/nsponline/calendar/store/`):
- `Roster`: Patrol member information
- `Assignments`: Shift assignments
- `ShiftDefinitions`: Shift templates and schedules
- `NspSession`: Session management
- `DirectorSettings`: Per-resort configuration

**PatrolData** (`src/main/java/org/nsponline/calendar/utils/PatrolData.java`):
- Central database connection manager
- Contains `resortMap` with all configured ski patrols
- Manages MySQL connection at `MYSQL_ADDRESS` (currently 127.0.0.1)
- Each resort has separate database tables using resort name as prefix

## Key Configuration Files

### Database Configuration
Update MySQL address in `PatrolData.java`:
```java
private final static String MYSQL_ADDRESS = "127.0.0.1";
```

### Adding New Resorts
Add entries to `PatrolData.resortMap` static initializer with resort metadata (name, URL, logo, etc.). Each resort requires corresponding database tables.

### Servlet Mappings
Defined in `src/main/webapp/WEB-INF/web.xml` - maps URL patterns to servlet classes.

## Multi-Tenant Database Pattern

Each ski resort has its own set of database tables prefixed with the resort name. When a request comes in with `?resort=Sample`, the application queries tables like `Sample_roster`, `Sample_assignments`, etc.

## Session Management

- Web sessions: Cookie-based via servlet container
- API sessions: Token-based via `Authorization` header
- Session validation: Through `NspSession.read(connection, sessionId)`
- Invalid sessions: API endpoints return 401, web pages redirect to MemberLogin

## Dependency Management

When updating versions in `pom.xml` properties section, also update `src/main/resources/doReminder.sh` classpath to match the JAR versions for the daily reminder cron job.

## Testing Notes

- Integration tests in `example/selenium/` package run against embedded Jetty
- Unit tests exclude `example/selenium/**` via surefire configuration
- All test requests must include valid `resort` parameter
- Selenium tests use HtmlUnit driver (headless browser)
