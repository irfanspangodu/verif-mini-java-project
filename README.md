# Verif — Enterprise Profile Verification System

> **Proprietary & Copyrighted Software**  
> **Copyright &copy; 2026 Verif Technologies Inc. All Rights Reserved.**  
> **LEGAL NOTICE: This software, its source code, design systems, algorithms, user interface components, and related documentation are the exclusive intellectual property of the author and copyright holder. Reproduction, redistribution, reverse engineering, unauthorized copying, public hosting, or commercial exploitation in whole or in part, in any form or by any means, without prior written authorization, is strictly prohibited and constitutes a violation of copyright law.**

---

## Mandatory Security Warning

> [!CAUTION]
> **CRITICAL SECURITY DIRECTIVE: DATABASE CREDENTIALS & SENSITIVE SECRETS**
> 
> - **NEVER commit sensitive credentials**including MySQL database passwords, SMTP credentials, TLS private keys, or API tokens to public or shared version control repositories (e.g., GitHub, GitLab, Bitbucket).
> - All sensitive configurations must be kept strictly local and excluded using the project's [`.gitignore`](file:///c:/Users/DELL/Documents/java/verif/.gitignore).
> - For production and staging environments, supply credentials exclusively via secure environment variables (`MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MAIL_USERNAME`, `MAIL_PASSWORD`) or an uncommitted `application-local.properties` file.
> - Verify that `git status` does not list files containing plain-text passwords before staging any commit.

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Key Architecture & Features](#key-architecture--features)
3. [Bento Grid Design System & Visuals](#bento-grid-design-system--visuals)
4. [Full-Page Screen Loading UI](#full-page-screen-loading-ui)
5. [Modular Directory Structure](#modular-directory-structure)
6. [Prerequisites](#prerequisites)
7. [Database Setup (MySQL)](#database-setup-mysql)
8. [Configuration](#configuration)
9. [Running the Application](#running-the-application)
10. [End-to-End User Verification Workflow](#end-to-end-user-verification-workflow)
11. [Admin Moderation & Rejection Workflow](#admin-moderation--rejection-workflow)
12. [Real-Time Polling & Email Notification System](#real-time-polling--email-notification-system)
13. [Production Deployment Guide](#production-deployment-guide)
14. [Testing & Verification](#testing--verification)
15. [License & Intellectual Property](#license--intellectual-property)

---

## System Overview

**Verif** is an enterprise-grade, full-stack identity verification system built entirely with **Java** for both the backend and frontend presentation layers, backed by **MySQL**. View logic is driven by **JavaServer Faces (Jakarta Faces 4.0 / Mojarra)** with Facelets XHTML templates and managed CDI beans, running on an embedded high-performance container.

Verif empowers organizations to onboard applicants, inspect identity documentation, track real-time verification lifecycles, and approve or cancel submissions with detailed administrative explanations and automated multi-party email notifications.

---

## Key Architecture & Features

- **100% Pure Java Full-Stack**: Eliminates disparate frontend build toolchains (Node/npm/webpack). All controllers, business rules, and views are managed via CDI/Faces Beans and Jakarta Faces Facelets.
- **Bento Grid Formation Layout**: Inspired by modern minimalist design aesthetics, featuring asymmetrical, high-density Bento cards, glassmorphic surfaces, and curated dark-mode color palettes.
- **Full-Page Screen Loading UI**: Seamlessly intercepts JSF Ajax lifecycle events (`jsf.ajax.addOnEvent`) and page navigations, providing animated brand pulsation and progress indicators.
- **Micro-Sized Assets (< 25 KB Total)**: Handcrafted CSS and inline SVGs eliminate multi-megabyte dependencies, ensuring sub-second initial page loads and rapid mobile rendering.
- **Automated Verification State Machine**:
  - `PENDING`: Initial state upon user registration; displays an instant "Verification Pending" banner.
  - `VERIFIED`: Approved by an administrator, triggering email certification.
  - `REJECTED`: Cancelled by an administrator with a mandatory, recorded reason and user notification.
- **Real-Time Dynamic Polling**: Continuous background synchronization of status checks and admin dashboards without full page reloads.
- **Automated Email Notification System**: Dispatches styled HTML notices to users and alerts to administrators, paired with a live in-dashboard mail outbox audit viewer.

---

## Bento Grid Design System & Visuals

The interface is structured upon a responsive 12-column Bento Grid:

- **Surface Glassmorphism**: Translucent panels (`rgba(17, 24, 39, 0.75)`) with multi-layer background blur (`backdrop-filter: blur(14px)`).
- **Curated Palette**:
  - Deep Obsidian Slate: `#080C14`
  - Emerald Green (Verified): `#10B981` / `#34D399`
  - Amber Gold (Pending): `#F59E0B` / `#FBBF24`
  - Crimson Rose (Rejected): `#F43F5E` / `#FB7185`
  - Indigo Accent: `#6366F1`
- **Micro-Animations**: Hover lifts, pulsing status dot indicators, glowing border transitions, and progress bar animations.

---

## Full-Page Screen Loading UI

The screen loader (`src/main/webapp/WEB-INF/templates/loader.xhtml`) provides visual feedback across all operations:

```javascript
// Automatically hooked to JSF Ajax events in app.js:
jsf.ajax.addOnEvent(function (data) {
  if (data.status === 'begin') {
    window.showLoader('Synchronizing verification state...');
  } else if (data.status === 'complete' || data.status === 'success') {
    setTimeout(window.hideLoader, 180);
  }
});
```

---

## Modular Directory Structure

The project adheres to a deep, component-wise, layered architecture:

```
verif/
├── pom.xml                                   # Maven Build Descriptor with JoinFaces & Jakarta Faces
├── schema.sql                                # Complete MySQL 8.0+ DDL & Seed Script
├── README.md                                 # Comprehensive System Guide & Legal Notices
├── .gitignore                                # Security exclusions for credentials & artifacts
├── mvnw.bat / run.bat                        # Convenience execution scripts
├── src/main/java/com/verif/
│   ├── VerifApplication.java                 # Application Entry Point & Servlet Initializer
│   ├── config/
│   │   ├── DatabaseConfig.java               # JPA & Repository Configuration
│   │   ├── JsfConfig.java                    # Mojarra / Facelets Servlet Parameters
│   │   └── MailConfig.java                   # Asynchronous Mail ThreadPool Executor
│   ├── core/
│   │   ├── constant/
│   │   │   ├── ProfileStatus.java            # PENDING, VERIFIED, REJECTED
│   │   │   ├── IdType.java                   # PASSPORT, NATIONAL_ID, DRIVERS_LICENSE
│   │   │   └── EmailType.java                # Multi-template Email Classifications
│   │   ├── exception/
│   │   │   └── VerificationException.java    # Domain Runtime Exception
│   │   └── util/
│   │       ├── VerificationCodeGenerator.java # Cryptographic tracking code generator
│   │       └── DateTimeUtils.java            # Centralized timestamp formatter
│   └── feature/
│       ├── profile/                          # User Profile Module
│       │   ├── model/UserProfile.java        # JPA Entity mapped to `user_profiles`
│       │   ├── repository/UserProfileRepository.java
│       │   ├── service/ProfileService.java
│       │   ├── service/impl/ProfileServiceImpl.java
│       │   └── controller/
│       │       ├── ProfileRegistrationBean.java # Managed Bean for register.xhtml
│       │       └── ProfileStatusBean.java    # Managed Bean for status.xhtml
│       ├── admin/                            # Admin Moderation Module
│       │   ├── dto/DashboardStatisticsDto.java# Bento KPI Metrics
│       │   ├── service/AdminVerificationService.java
│       │   ├── service/impl/AdminVerificationServiceImpl.java
│       │   └── controller/AdminDashboardBean.java # Managed Bean for admin/dashboard.xhtml
│       └── notification/                     # Email Notification Module
│           ├── model/EmailNotificationLog.java
│           ├── repository/EmailNotificationLogRepository.java
│           ├── service/EmailService.java
│           ├── service/impl/EmailServiceImpl.java
│           └── controller/NotificationBean.java
├── src/main/resources/
│   ├── application.properties                # Default Profile (Auto H2 / MySQL-ready)
│   ├── application-mysql.properties          # Dedicated Production MySQL Profile
│   └── data.sql                              # Auto-seeding demonstration script
└── src/main/webapp/
    ├── WEB-INF/
    │   ├── faces-config.xml                  # Jakarta Faces Configuration
    │   ├── web.xml                           # Web Descriptor
    │   └── templates/
    │       ├── layout.xhtml                  # Master Bento Template
    │       └── loader.xhtml                  # Full-Page Screen Loader Component
    ├── resources/
    │   ├── css/
    │   │   ├── bento.css                     # Bento Grid Layout Engine (< 6 KB)
    │   │   ├── theme.css                     # Theme, Typography & Components (< 12 KB)
    │   │   └── loader.css                    # Loading Screen Animations (< 3 KB)
    │   └── js/
    │       └── app.js                        # JSF Ajax Loader Lifecycle Hook (< 2 KB)
    ├── index.xhtml                           # Home Bento Landing & Overview
    ├── register.xhtml                        # User Registration Bento Form
    ├── status.xhtml                          # Real-Time Profile Status Tracker
    └── admin/
        └── dashboard.xhtml                   # Admin Bento Command Center
```

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 17+ (e.g., Eclipse Temurin 17)
- **Build Tool**: Apache Maven 3.9+
- **Database Engine**: MySQL 8.0+ (or MariaDB 10.5+)
- **Modern Web Browser**: Chrome, Firefox, Safari, or Edge

---

## Database Setup (MySQL)

1. Log into your MySQL database server:
   ```bash
   mysql -u root -p
   ```
2. Execute the provided [`schema.sql`](file:///c:/Users/DELL/Documents/java/verif/schema.sql) script:
   ```sql
   SOURCE c:/Users/DELL/Documents/java/verif/schema.sql;
   ```
   This will:
   - Create the `verif_db` database with `utf8mb4` character set.
   - Create the `user_profiles` and `email_notifications` tables.
   - Seed sample verification requests and notification outbox records.

---

## Configuration

### Activating the MySQL Profile

To run against your live MySQL instance, set your environment variables or pass Spring Boot profile flags:

```properties
# Defined in src/main/resources/application-mysql.properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:verif_db}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=${MYSQL_USER:root}
spring.datasource.password=${MYSQL_PASSWORD:your_secure_password}
```

> [!TIP]
> **Zero-Setup Quick Run**: If you run without `--spring.profiles.active=mysql`, Verif boots instantly using its built-in in-memory H2 database in MySQL-compatibility mode, pre-seeded with sample records so you can evaluate the UI and workflows immediately.

---

## Running the Application

### Option A: Using the Convenience Script (Windows)
```cmd
.\run.bat
```
To run with your dedicated MySQL server:
```cmd
.\run.bat --mysql
```

### Option B: Using Maven Directly
```bash
# Default mode
mvn spring-boot:run

# With production MySQL profile
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Application URLs:
- **Home Overview**: [http://localhost:8080](http://localhost:8080)
- **User Registration**: [http://localhost:8080/register.xhtml](http://localhost:8080/register.xhtml)
- **Real-Time Status Tracker**: [http://localhost:8080/status.xhtml](http://localhost:8080/status.xhtml)
- **Admin Verification Dashboard**: [http://localhost:8080/admin/dashboard.xhtml](http://localhost:8080/admin/dashboard.xhtml)

---

## End-to-End User Verification Workflow

1. Navigate to `/register.xhtml`.
2. Notice the **Verification Pending Notification Banner** prominently displayed upon entry, informing the user of the mandatory pending queue.
3. Fill in legal full name, email, phone number, date of birth, occupation, ID document type, and ID number.
4. Click **"Submit for Verification"**.
5. The full-page screen loading UI activates with a pulsing brand emblem.
6. The profile is saved to MySQL with `verification_status = 'PENDING'`, and a collision-resistant tracking code (e.g. `VRF-2026-X99AB`) is assigned.
7. An automated "Verification Pending" HTML email is dispatched to the user, and an alert is dispatched to the administrator.
8. The user is redirected to `/status.xhtml?trackingCode=...`, displaying the glowing amber **Verification Pending** status and real-time timeline.

---

## Admin Moderation & Rejection Workflow

1. Open `/admin/dashboard.xhtml`.
2. Observe live KPI cards: **Total Submissions**, **Pending Verification**, **Verified Profiles**, and **Cancelled/Denied**.
3. Under **Pending Verification Queue**, inspect applicant credentials.
4. **To Verify**: Click **"Verify Profile"**. The system executes an Ajax update, sets status to `VERIFIED`, logs the timestamp, and dispatches a verification approval email.
5. **To Reject / Cancel**:
   - Click **"Cancel Profile"**.
   - A modal dialog appears requiring a **Mandatory Cancellation Reason**.
   - Input the reason (e.g., *"Identity document blurred and expired; please resubmit a clear scan"*).
   - Click **"Confirm Cancellation & Notify User"**.
   - The profile is updated to `REJECTED`, the reason is persisted, and an explanatory cancellation email is sent to the user.

---

## Real-Time Polling & Email Notification System

- **Live Polling**: Both `status.xhtml` and `admin/dashboard.xhtml` execute unobtrusive background Ajax polls. When an admin approves or cancels a profile, the applicant's status page automatically transitions from "Verification Pending" to "Verified" or "Cancelled" without requiring manual page refreshes.
- **Outbox Audit Feed**: On the Admin dashboard, all emails dispatched by the system (to applicants and administrators) are recorded in the MySQL `email_notifications` table and displayed in the **Automated Email Outbox** table for inspection.

---

## Production Deployment Guide

### Standalone Executable WAR/JAR Deployment:
```bash
mvn clean package -DskipTests
java -jar target/verif-system-1.0.0.war --spring.profiles.active=mysql
```

### External Servlet Container (Tomcat 10+ / WildFly 27+):
Deploy `target/verif-system-1.0.0.war` directly into the container's `webapps` or `deployments` directory.

---

## Testing & Verification

Execute all automated unit and integration tests:
```bash
mvn test
```

---

## License & Intellectual Property

**COPYRIGHT &copy; 2026 VERIF TECHNOLOGIES INC. ALL RIGHTS RESERVED.**

This software and associated documentation are proprietary, confidential, and protected under domestic and international copyright and trade secret laws.

**RESTRICTIONS:**
1. You may NOT copy, modify, distribute, sell, sublicense, lease, or syndicate this software.
2. You may NOT reverse engineer, decompile, or disassemble any part of this software.
3. No public git repository hosting or redistribution is authorized.
4. Any breach of these terms is subject to strict civil liability and statutory damages under applicable copyright legislation.

