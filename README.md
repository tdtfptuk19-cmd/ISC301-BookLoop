# BookLoop — C2C Second-Hand University Textbook Marketplace

BookLoop is a student-centric C2C (consumer-to-consumer) marketplace application designed for university students to buy, sell, and exchange used textbooks directly on campus, with specialized support for institutions in the **Hòa Lạc area of Hanoi** (**FPT University**, **Vietnam National University, Hanoi**, and **Academy of Finance**).

Built with **Java 17**, **Spring Boot 3.3.5**, **Thymeleaf**, **Bootstrap 5**, **Spring Security**, and **Microsoft SQL Server**.

---

## Table of Contents

1. [Features Overview](#features-overview)
2. [Phase 5 & 6 Product Enhancements](#phase-5--6-product-enhancements)
3. [Technology Stack](#technology-stack)
4. [Prerequisites & Environment Setup](#prerequisites--environment-setup)
5. [Database Setup & Schema](#database-setup--schema)
6. [Demo Accounts](#demo-accounts)
7. [Running the Application](#running-the-application)
8. [Running Tests](#running-tests)
9. [Main Routes](#main-routes)
10. [Academic Textbook Catalog & Promotion Workflow](#academic-textbook-catalog--promotion-workflow)

---

## Features Overview

### Phase 3 — Core Marketplace
- **Student Registration & Authentication**: BCrypt encryption, session-based auth, CSRF protection, duplicate email check.
- **Listing Management**: Create, edit, and delete used book listings with condition, campus, defect notes, and image URL.
- **Marketplace Browsing**: Keyword search, filters by category, campus, condition, and price range with pagination.
- **Student Profiles**: Manage contact info, university, and view public seller profiles.

### Phase 4 — Buying, Selling & Order Workflow
- **Shopping Cart (`/cart`)**: Add textbooks, live navbar badge, prevent self-purchase, detect unavailable books.
- **Checkout (`/checkout`)**: Choose transaction method (**In-person Campus Meetup** or **Direct Bank Transfer**), coordinate meetup notes and contact phone number.
- **Order Lifecycle**: `PENDING` → `CONFIRMED` → `COMPLETED` or `CANCELLED` (with automatic inventory restoration).
- **Reviews & Ratings (`/reviews`)**: Buyer rates seller 1–5 stars upon transaction completion.

### Phase 5 — Admin Dashboard & Platform Moderation
- **Dashboard Overview (`/admin/dashboard`)**: KPI statistics, status distribution doughnut and bar charts (Chart.js), recent registrations, listings, orders, and reports.
- **User Management (`/admin/users`)**: Search, filter, activate/deactivate accounts, promote to ADMIN or demote to STUDENT.
- **Listing Moderation (`/admin/listings`)**: Moderation actions (hide/restore listings with moderation notes).
- **Dispute & Violation Reports (`/admin/reports`)**: Resolve user reports, record resolution notes.
- **Order Monitoring (`/admin/orders`)**: Read-only platform inspection of transactions.
- **Review Moderation (`/admin/reviews`)**: Hide inappropriate or abusive reviews.
- **Immutable Audit Trail (`/admin/logs`)**: Tracks every admin moderation action.

---

## Phase 5 & 6 Product Enhancements

### 1. Academic Textbook Catalog for Hòa Lạc University Students
- Curated academic catalog for **FPT University (FPTU)**, **VNU Hanoi (UET, VNU-IS)**, and **Academy of Finance (AOF)**.
- Models: `Institution`, `Campus`, `AcademicProgram`, `Course`, `AcademicBook`, `CourseBook`.
- Curated course code mappings (e.g. `CSD201`, `PRF192`, `MAS291`, `INT1006`, `MAT101`, `FIN201`) with **Required** vs **Suggested Reference** relationship types.
- Discovery page at `/textbooks` with university shortcuts and direct matching of used student listings to official textbook titles.
- Seeded idempotently on startup via `AcademicCatalogSeeder`.

### 2. Paid Book Listing Promotion System
- Configurable promotion fee (default **10.000 ₫**) and duration (default **7 days**).
- Kept strictly isolated from buyer-seller book order transactions (`promotion_payments` table).
- After creating a listing, the seller is redirected to `/my-listings/{id}/promotion-payment` to submit their transaction reference and bank transfer notes.
- Admin verifies and approves payments at `/admin/promotion-payments`, activating the glowing `NỔI BẬT` badge and bumping listings to the top of search results and the homepage "Sách Nổi Bật" section.
- Admin manages fee amount and duration settings at `/admin/fee-settings`.

### 3. Complete Vietnamese Localization & VND Currency Formatting
- Standard Vietnamese currency formatting via `@formatUtils.formatVnd(...)`: strictly e.g. `10.000 ₫`, `50.000 ₫`, `150.000 ₫` using `vi-VN` locale without decimal places.
- Date formatting via `@formatUtils.formatDate(...)`: `dd/MM/yyyy` in Vietnam time zone (`Asia/Ho_Chi_Minh`).
- 100% natural Vietnamese user interface across all student-facing and administration pages.

### 4. Modern UI/UX Redesign
- Redesigned homepage hero with university quick pills, instant course search, and prominent Promoted Books section.
- Modern Bootstrap 5 cards with smooth hover transitions, clear condition badges, and responsive tables for desktop and mobile.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build Tool | Apache Maven 3.9 |
| Templates | Thymeleaf 3 + Spring Security Extras |
| Styling | Bootstrap 5.3 + Font Awesome 6 + Custom `bookloop.css` |
| Database | Microsoft SQL Server 2022 (`BookLoopDB`) |
| ORM | Spring Data JPA / Hibernate 6 |
| Security | Spring Security 6 (BCrypt, Sessions, CSRF, RBAC) |
| Testing | JUnit 5, AssertJ, Spring MockMvc, H2 In-Memory DB |

---

## Database Setup & Schema

1. Connect to SQL Server:
   ```powershell
   sqlcmd -S localhost -U sa -P 123 -Q "CREATE DATABASE BookLoopDB;"
   ```
2. Run the complete schema script:
   ```powershell
   sqlcmd -S localhost -U sa -P 123 -i sql/schema.sql
   ```
   The schema contains all 18 tables: `users`, `products`, `carts`, `cart_items`, `orders`, `order_items`, `reviews`, `reports`, `admin_logs`, `institutions`, `campuses`, `academic_programs`, `courses`, `academic_books`, `course_books`, `platform_settings`, `promotion_payments`.

---

## Demo Accounts

All accounts use password: `password123` (Admin uses: `admin123`).

| Email | Role | Full Name | Campus |
|---|---|---|---|
| `admin@bookloop.edu` | **ADMIN** | Platform Administrator | Hòa Lạc Admin |
| `alice@university.edu` | **STUDENT** | Alice Nguyen | ĐH FPT Hòa Lạc |
| `bob@university.edu` | **STUDENT** | Bob Tran | VNU Hòa Lạc (UET) |
| `carol@university.edu` | **STUDENT** | Carol Le | Học viện Tài chính CS2 |

---

## Running the Application

```powershell
cd c:\Users\ADMIN\Downloads\bookloop\bookloop
& "C:\Users\ADMIN\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run
```

Access the application in your browser:
- Marketplace: `http://localhost:8080/`
- Academic Catalog: `http://localhost:8080/textbooks`
- Admin Dashboard: `http://localhost:8080/admin/dashboard` (Sign in as `admin@bookloop.edu`)

---

## Running Tests

Execute the full regression suite (103 tests):

```powershell
cd c:\Users\ADMIN\Downloads\bookloop\bookloop
& "C:\Users\ADMIN\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" test
```

Build executable JAR package:

```powershell
& "C:\Users\ADMIN\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" package -DskipTests
```

---

## Main Routes

### Public
- `/` — Homepage (Hero, search, Hòa Lạc university discovery, promoted listings)
- `/books` — Marketplace search and filtering
- `/books/{id}` — Used book listing details
- `/textbooks` — Academic textbook catalog for Hòa Lạc universities
- `/textbooks/{id}` — Textbook details & matching used copies
- `/profile/seller/{id}` — Public seller profile & reviews
- `/login`, `/register` — Authentication

### Student (Authenticated)
- `/profile` — Personal profile view & edit
- `/my-listings` — Manage own listings
- `/my-listings/new` — Create listing & optional academic catalog link
- `/my-listings/{id}/promotion-payment` — Pay listing promotion fee
- `/my-listings/payments` — Promotion fee payment history
- `/cart` — Shopping cart
- `/checkout` — Order placement (Meetup or Bank Transfer)
- `/orders/my-purchases` — Buyer order history
- `/orders/my-sales` — Seller order management
- `/orders/{id}` — Order detail & workflow actions
- `/reviews` — Leave seller review
- `/reports/new` — Report a listing violation

### Admin (ROLE_ADMIN)
- `/admin/dashboard` — Platform overview & charts
- `/admin/users` — User management & RBAC
- `/admin/listings` — Book listing moderation
- `/admin/promotion-payments` — Verify and approve paid listing promotions
- `/admin/fee-settings` — Manage listing promotion fee and duration
- `/admin/orders` — Order monitoring
- `/admin/reports` — Violation report resolution
- `/admin/reviews` — Review moderation
- `/admin/logs` — Immutable audit log
