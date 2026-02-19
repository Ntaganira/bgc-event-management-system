# BGC Event Management System (EMS)

A comprehensive web-based event management platform for Bible Global Congress, built with Spring Boot, Thymeleaf, and PostgreSQL.

![BGC EMS Dashboard](https://via.placeholder.com/800x400?text=BGC+EMS+Dashboard)

## 📋 Features

### Core Functionality
- **Event Management**: Create, publish, and manage single-day and multi-day events
- **Self-Registration**: Attendees can register online with ROLE_ATTENDEE
- **Attendance Tracking**: Dual check-in system (QR code & manual code)
- **Role-Based Access Control**: Multi-role support with granular permissions
- **Analytics Dashboard**: Real-time statistics with Chart.js visualizations
- **Email Notifications**: Automated emails for registrations and updates
- **Multi-Language Support**: English, French, Spanish, German interfaces

### Technical Features
- **Redis Caching**: High-performance caching for events, sessions, and attendance tokens
- **Docker Containerization**: Easy deployment with docker-compose
- **Database Versioning**: Flyway migrations for schema management
- **Audit Logging**: Immutable logs for all critical actions
- **Responsive UI**: Mobile-friendly interface with collapsible sidebar

## 🚀 Technology Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Spring Boot 3.x, Spring Security, Spring Data JPA |
| **Frontend** | Thymeleaf, Bootstrap 5, FullCalendar, Chart.js, jQuery |
| **Database** | PostgreSQL 15 |
| **Cache/Session** | Redis 7 |
| **Build Tool** | Maven |
| **Migration** | Flyway |
| **Container** | Docker, Docker Compose |
| **Testing** | JUnit 5, Mockito |

## 📁 Project Structure
bgc-event-management-system/
│
├── 📦 src/
│   └── 📦 main/
│       ├── 📦 java/com/bgc/event/
│       │   ├── BgcEventManagementApplication.java
│       │   │
│       │   ├── 📂 config/          # Application configuration (Security, CORS, Beans)
│       │   ├── 📂 controller/      # MVC Controllers (Admin, Organizer, Attendee, Auth)
│       │   ├── 📂 model/           # JPA Entities (User, Event, Role, Category, etc.)
│       │   ├── 📂 repository/      # Spring Data JPA Repositories
│       │   ├── 📂 service/         # Business Logic Layer
│       │   ├── 📂 security/        # Spring Security Config & JWT/Auth logic
│       │   ├── 📂 dto/             # Data Transfer Objects
│       │   ├── 📂 exception/       # Global & Custom Exception Handling
│       │   ├── 📂 util/            # Utility Classes
│       │   └── 📂 audit/           # Audit Logging & Tracking
│       │
│       └── 📦 resources/
│           ├── 📂 templates/       # Thymeleaf Templates
│           │
│           │   ├── 📂 layouts/
│           │   │   └── main-layout.html
│           │   │
│           │   ├── 📂 admin/
│           │   │   ├── dashboard.html
│           │   │   ├── users.html
│           │   │   ├── roles.html
│           │   │   ├── categories.html
│           │   │   └── audit-logs.html
│           │   │
│           │   ├── 📂 organizer/
│           │   │   ├── dashboard.html
│           │   │   ├── events.html
│           │   │   ├── event-form.html
│           │   │   ├── attendance.html
│           │   │   ├── reports.html
│           │   │   └── analytics.html
│           │   │
│           │   ├── 📂 attendee/
│           │   │   ├── dashboard.html
│           │   │   ├── events.html
│           │   │   ├── calendar.html
│           │   │   ├── registrations.html
│           │   │   └── profile.html
│           │   │
│           │   ├── 📂 auth/
│           │   │   ├── login.html
│           │   │   ├── register.html
│           │   │   └── forgot-password.html
│           │   │
│           │   ├── 📂 fragments/
│           │   │   ├── header.html
│           │   │   ├── sidebar.html
│           │   │   ├── footer.html
│           │   │   │
│           │   │   ├── 📂 modals/
│           │   │   │   ├── user-modal.html
│           │   │   │   ├── event-modal.html
│           │   │   │   ├── role-modal.html
│           │   │   │   ├── category-modal.html
│           │   │   │   └── confirm-delete-modal.html
│           │   │   │
│           │   │   └── 📂 charts/
│           │   │       ├── attendance-chart.html
│           │   │       └── revenue-chart.html
│           │   │
│           │   └── 📂 error/
│           │       ├── 403.html
│           │       ├── 404.html
│           │       └── 500.html
│           │
│           ├── 📂 static/          # CSS, JS, Images, Fonts
│           ├── 📂 messages/        # i18n message bundles
│           ├── 📂 db/migration/    # Flyway/Liquibase migration scripts
│           └── application.properties
│
├── 📂 docker/
│   └── Dockerfile
│
└── docker-compose.yml
