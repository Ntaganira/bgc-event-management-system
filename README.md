BGC Events Management System
A comprehensive event management platform built with Spring Boot, featuring public registration, QR code check-ins, real-time analytics, and role-based access control.

https://via.placeholder.com/800x400?text=BGC+Events+Dashboard

📋 Table of Contents
Features

Tech Stack

Quick Start

System Architecture

Installation Guide

Configuration

API Documentation

Database Schema

User Roles

Screenshots

Deployment

Monitoring

Contributing

License

Support

✨ Features
For Public Users
✅ Browse events without login

✅ Register for events with one click

✅ Receive QR code ticket via email

✅ Cancel registration anytime

✅ View event calendar

For Organizers
✅ Create and manage events

✅ Set capacity and deadlines

✅ Track registrations in real-time

✅ Check-in attendees via QR code

✅ Export attendance reports (PDF/Excel)

✅ View analytics dashboard

For Administrators
✅ Full system control

✅ User management

✅ Audit logs

✅ System health monitoring

✅ Role-based permissions

✅ Advanced analytics

Technical Features
✅ JWT Authentication

✅ Role-based access control (RBAC)

✅ Redis caching for performance

✅ PostgreSQL with Flyway migrations

✅ QR code generation

✅ Email notifications

✅ RESTful API

✅ WebSocket for real-time updates

✅ FullCalendar integration

✅ Responsive UI with Bootstrap

✅ Dark/Light theme sidebar

✅ Skeleton loading

✅ Comprehensive error handling

🛠 Tech Stack
Backend
Java 17 - Core language

Spring Boot 3.x - Application framework

Spring Security - Authentication & authorization

Spring Data JPA - Database operations

PostgreSQL - Primary database

Redis - Caching layer

Flyway - Database migrations

JWT - Token-based authentication

Lombok - Boilerplate code reduction

MapStruct - Object mapping

iText - PDF generation

Apache POI - Excel export

ZXing - QR code generation

Thymeleaf - Server-side templating

Frontend
Thymeleaf - Template engine

Bootstrap 5 - CSS framework

FullCalendar - Calendar views

Chart.js - Analytics charts

DataTables - Advanced tables

Font Awesome - Icons

HTML5 QR Scanner - QR code scanning

Flatpickr - Date/time picker

Tagify - Tag input

DevOps
Docker - Containerization

Docker Compose - Multi-container orchestration

Nginx - Reverse proxy

Prometheus - Metrics collection

Grafana - Visualization

GitHub Actions - CI/CD

🚀 Quick Start
Prerequisites
Docker and Docker Compose

Java 17 (for local development)

Maven 3.8+

Node.js 16+ (for frontend assets)

5-Minute Setup
bash
# Clone the repository
git clone https://github.com/yourusername/bgc-events.git
cd bgc-events

# Copy environment variables
cp .env.example .env

# Start with Docker
docker-compose -f docker-compose.prod.yml up -d

# Access the application
open http://localhost:8080

# Default credentials
# Admin: admin@bgc.event / Admin@123
# Organizer: organizer@bgc.event / Organizer@123
🏗 System Architecture
text
┌─────────────────────────────────────────────────────────────┐
│                         Nginx (Reverse Proxy)                │
│                         Ports: 80, 443                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   Controllers Layer                    │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐       │   │
│  │  │ Auth   │ │ Event  │ │Registration│Calendar│       │   │
│  │  └────────┘ └────────┘ └────────┘ └────────┘       │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │                   Service Layer                        │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐       │   │
│  │  │ User   │ │Event   │ │Registration│Analytics│       │   │
│  │  └────────┘ └────────┘ └────────┘ └────────┘       │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │                   Repository Layer                     │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐       │   │
│  │  │ User   │ │Event   │ │Registration│Audit   │       │   │
│  │  └────────┘ └────────┘ └────────┘ └────────┘       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
    ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
    │   PostgreSQL  │ │     Redis     │ │     Redis     │
    │   (Primary)   │ │   (Cache)     │ │  (Sessions)   │
    └───────────────┘ └───────────────┘ └───────────────┘