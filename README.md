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
├── src/main/java/com/bgc/event/
│ ├── config/ # Configuration classes
│ ├── controller/ # MVC controllers
│ ├── controller/api/ # REST API controllers
│ ├── model/ # JPA entities
│ ├── repository/ # Data repositories
│ ├── service/ # Business logic
│ ├── dto/ # Data transfer objects
│ ├── exception/ # Custom exceptions
│ ├── util/ # Utility classes
│ └── audit/ # Audit logging
├── src/main/resources/
│ ├── templates/ # Thymeleaf templates
│ ├── static/ # CSS, JS, images
│ ├── messages/ # i18n message files
│ ├── db/migration/ # Flyway migrations
│ └── application.properties
├── docker/
│ └── Dockerfile
└── docker-compose.yml     
          
      
