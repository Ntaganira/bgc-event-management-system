# 🎯 BGC Event Management System

**Author:** NTAGANIRA Heritier  
**Date:** 2026-02-27  
**Stack:** Spring Boot 3.2 · Thymeleaf · Spring Security · JPA · Flyway · H2 / PostgreSQL

---

## ✅ Features

| Feature | Status |
|---|---|
| Public Registration | ✅ |
| Session-based Login | ✅ |
| Dynamic RBAC (Roles & Permissions) | ✅ |
| Method-level `@PreAuthorize` security | ✅ |
| Event CRUD | ✅ |
| FullCalendar Integration | ✅ |
| QR / Manual Code Attendance | ✅ |
| User Management (enable/disable, roles) | ✅ |
| Audit Logs (with pagination & search) | ✅ |
| Flyway DB Migrations (V1–V8) | ✅ |
| H2 Dev Mode (zero setup) | ✅ |
| PostgreSQL Production Mode | ✅ |
| Docker + Docker Compose | ✅ |
| Fully Responsive UI | ✅ |

---

## 🚀 Quick Start (Dev Mode — No DB Setup Needed)

```bash
# 1. Clone / extract the project
cd bgc-event

# 2. Run with Maven (Java 17+ required)
./mvnw spring-boot:run

# OR with Maven installed:
mvn spring-boot:run
```

**App URL:** http://localhost:8080  
**H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:bgceventdb`)

### Default Admin Account
| Email | Password |
|---|---|
| admin@bgc.com | password123 |

---

## 🐳 Docker (Production with PostgreSQL)

```bash
# Build and run everything
docker-compose up --build

# App available at:
http://localhost:8080
```

---

## 🗂️ Project Structure

```
bgc-event/
├── src/main/java/com/bgc/event/
│   ├── BgcEventApplication.java          # Main class
│   ├── config/
│   │   └── SecurityConfig.java           # Spring Security + RBAC
│   ├── controller/
│   │   ├── AuthController.java           # /login /register
│   │   ├── DashboardController.java      # /dashboard
│   │   ├── EventController.java          # /events + /api/events/calendar
│   │   ├── AttendanceController.java     # /attendance
│   │   ├── UserController.java           # /users
│   │   └── AuditLogController.java       # /audit
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── Event.java
│   │   ├── Attendance.java
│   │   └── AuditLog.java
│   ├── repository/                        # Spring Data JPA repos
│   ├── service/                          # Service interfaces
│   │   └── impl/                        # Implementations
│   ├── dto/                             # RegisterDto, EventDto, CalendarEventDto...
│   └── security/
│       └── CustomUserDetailsService.java # Loads user + roles + permissions
│
├── src/main/resources/
│   ├── application.properties            # Base config
│   ├── application-dev.properties        # H2 in-memory
│   ├── application-prod.properties       # PostgreSQL
│   ├── db/migration/                     # Flyway V1–V8
│   │   ├── V1__init_schema.sql
│   │   ├── V2__add_roles_table.sql
│   │   ├── V3__add_permissions_table.sql
│   │   ├── V4__role_permission_mapping.sql
│   │   ├── V5__user_role_mapping.sql
│   │   ├── V6__events_and_attendance.sql
│   │   ├── V7__audit_log.sql
│   │   └── V8__seed_data.sql             # Default admin + permissions + events
│   ├── templates/
│   │   ├── auth/login.html
│   │   ├── auth/register.html
│   │   ├── fragments/layout.html         # Shared sidebar + topbar
│   │   ├── dashboard.html
│   │   ├── events/{list,calendar,form,view}.html
│   │   ├── attendance/{index,event-detail}.html
│   │   ├── users/{list,view}.html
│   │   └── audit/list.html
│   └── static/
│       ├── css/main.css
│       └── js/main.js
│
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## 🔐 RBAC Permissions

| Permission | Description |
|---|---|
| `VIEW_DASHBOARD` | See the dashboard |
| `CREATE_EVENT` | Create new events |
| `EDIT_EVENT` | Edit existing events |
| `DELETE_EVENT` | Delete events |
| `VIEW_EVENT` | View events & calendar |
| `MARK_ATTENDANCE` | Check in via QR or code |
| `VIEW_ATTENDANCE` | View attendance records |
| `MANAGE_USERS` | Enable/disable/delete users |
| `VIEW_USERS` | View user list |
| `MANAGE_ROLES` | Assign/remove roles |
| `VIEW_AUDIT_LOGS` | View audit trail |
| `SEND_NOTIFICATION` | Send email notifications |

---

## 📋 Default Roles

| Role | Permissions |
|---|---|
| `ROLE_ADMIN` | All permissions |
| `ROLE_ORGANIZER` | VIEW, CREATE, EDIT events; view users |
| `ROLE_STAFF` | View events, mark & view attendance |
| `ROLE_USER` | View dashboard & events |

---

## 🔧 Switching to PostgreSQL

```bash
# Set profile to prod
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=youruser
export DB_PASSWORD=yourpassword

# Or update application-prod.properties
mvn spring-boot:run
```

---

## 📦 Build JAR

```bash
mvn clean package -DskipTests
java -jar target/bgc-event-1.0.0.jar
```
