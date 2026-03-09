# NSP Online Calendar

National Ski Patrol Online Scheduling System - A web-based application for managing ski patrol shift scheduling and assignments across multiple ski patrol organizations.

## Overview

This application provides ski patrols with a centralized platform to:
- Manage patrol member rosters
- Define shift templates and schedules
- Allow members to view and sign up for shifts
- Track assignments and patrol coverage
- Send automated reminder emails
- Provide director-level administrative tools

The system is multi-tenant, supporting multiple ski patrol organizations with separate rosters, schedules, and configurations.

## Technology Stack

- **Backend**: Java 7/8 with Servlet API 2.5
- **Build Tool**: Maven
- **Web Server**: Apache Tomcat 7
- **Database**: MySQL 8.0
- **Email**: AWS SES (Simple Email Service)
- **Frontend**: Server-side rendered HTML
- **Testing**: JUnit 4, Selenium with HtmlUnit

## Prerequisites

- Java JDK 8 or higher
- Maven 3.x
- MySQL 8.0
- AWS account with SES configured (for email functionality)
- Apache Tomcat 7 (for production deployment)

## Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd calendar
```

### 2. Configure Database

Update the MySQL address in `src/main/java/org/nsponline/calendar/utils/PatrolData.java`:

```java
private final static String MYSQL_ADDRESS = "127.0.0.1";
```

Ensure your MySQL instance is running and accessible.

### 3. Build the Project

```bash
mvn clean package
```

This will create `target/calendar-1.war`.

### 4. Run Tests

```bash
# Run unit tests
mvn test

# Run integration tests (starts embedded Jetty on port 7070)
mvn clean package -Dintegration.test
```

## Deployment

### Local Development with Jetty

Integration tests start an embedded Jetty server:

```bash
mvn clean package -Dintegration.test
```

Access the application at `http://localhost:7070/`

### Production Deployment (Tomcat)

1. Copy the WAR file to your Tomcat webapps directory:
   ```bash
   cp target/calendar-1.war /var/lib/tomcat7/webapps/
   ```

2. Restart Tomcat:
   ```bash
   sudo systemctl restart tomcat7
   ```

3. Access the application at `http://your-server:8080/calendar-1/`

### Daily Reminder Emails

Configure a cron job to send daily reminder emails:

```bash
# Edit crontab
crontab -e

# Add this line (runs daily at 6 AM)
0 6 * * * /path/to/calendar/src/main/resources/doReminder.sh
```

Note: If you update dependency versions in `pom.xml`, update the classpath in `doReminder.sh` accordingly.

## Project Structure

```
calendar/
├── src/
│   ├── main/
│   │   ├── java/org/nsponline/calendar/
│   │   │   ├── resources/        # Servlet endpoints
│   │   │   │   ├── Outer*.java   # Public pages (no auth)
│   │   │   │   ├── Inner*.java   # Authenticated pages
│   │   │   │   └── Api*.java     # REST API endpoints
│   │   │   ├── store/            # Data models
│   │   │   ├── utils/            # Utilities and helpers
│   │   │   └── DailyReminder.java # Email reminder job
│   │   ├── webapp/WEB-INF/
│   │   │   └── web.xml           # Servlet mappings
│   │   └── resources/
│   │       └── doReminder.sh     # Cron job script
│   └── test/
│       └── java/                 # Unit and integration tests
├── pom.xml                       # Maven configuration
└── CLAUDE.md                     # AI assistant guidance
```

## Usage

### Accessing the Application

Navigate to the volunteer form for a specific resort:
```
http://localhost:8080/calendar-1/VolunteerForm?resort=<ResortName>
```

Valid resort names are configured in `PatrolData.resortMap`.

### Key Endpoints

**Web UI:**
- `/VolunteerForm` - Public volunteer signup form
- `/MemberLogin` - Member login page
- `/MonthCalendar` - Monthly shift calendar
- `/Directors` - Director administration panel

**REST API:**
- `/login` - Authenticate and receive session token
- `/user/assignments` - Get user's shift assignments
- `/patrol/assignments` - Get all patrol assignments
- `/resort/list` - List all configured resorts

API endpoints require an `Authorization` header with a valid session token.

## Adding a New Resort

To add a new ski patrol to the system:

1. Open `src/main/java/org/nsponline/calendar/utils/PatrolData.java`
2. Add a new entry to the `resortMap` in the static initializer block:
   ```java
   resortMap.put("ResortName", new ResortData(
       "ResortName",           // URL parameter
       "Resort Display Name",  // Full name
       null,                   // Optional alternate name
       "http://website.com",   // Resort website
       "/images/logo.jpg",     // Logo path
       80,                     // Image height
       90                      // Image width
   ));
   ```
3. Set up the database tables for the new resort
4. Rebuild and redeploy

## Development

### Running a Single Test

```bash
mvn test -Dtest=SimpleTest#testMethod
```

### Debugging

For local debugging, configure your IDE to:
1. Run Maven with the `integration.test` property
2. Attach debugger to port 7070
3. Set breakpoints in servlet classes

## License

[Add license information]

## Contact

For questions or support, contact [contact information]
