# Government Scheme Recommendation System (Rule-Based & Database-Driven)

A web-based platform built with **Java 23, Spring Boot 3, Spring Data JPA, Spring Security, JWT, Embedded H2 Database (or MySQL), HTML5, CSS3, JavaScript, and Bootstrap 5**.

This platform allows citizens to discover government schemes for which they are eligible based on personal metrics (Age, Gender, State, District, Annual Family Income, Occupation, Employment Status, Social Category, and Special Status flags for Students, Farmers, PwD, and Entrepreneurs).

> **IMPORTANT:** This application is **100% RULE-BASED and DATABASE-DRIVEN**. It does **NOT** use artificial intelligence, machine learning, deep learning, chatbots, or LLM APIs. Every recommendation decision is deterministic, transparent, and explainable with step-by-step rule breakdowns.

---

## 🏛️ Features Overview

### 👤 Citizen / User Features
1. **User Registration & JWT Login:** Secure authentication with password hashing and JWT token management.
2. **Citizen Profile Management:** Maintain and update personal demographic & eligibility metrics.
3. **Deterministic Rule Engine:** Instant eligibility evaluation comparing profile values against active database rules.
4. **Transparent Criteria Breakdown:** Displays green checks (`✓`) for satisfied conditions and red crosses (`✗`) with explicit failure explanations for unmet conditions.
5. **Scheme Search & Filtering:** Filter schemes by category, state, central/state type, and search keywords.
6. **Scheme Details Page:** Detailed view of benefits, required documents (mandatory vs optional), step-by-step application procedures, contact info, and official portal links.
7. **Saved Schemes / Bookmarks:** Save schemes to personal dashboard for quick tracking.
8. **Personalized User Dashboard:** Profile completion gauge, count of eligible schemes, and saved bookmarks manager.

### 🛡️ Admin Features
1. **Secure Admin Portal:** Role-based access control (`ROLE_ADMIN`).
2. **System Statistics:** Overview of total users, active schemes, category breakdown, and configured rules.
3. **Scheme CRUD:** Create, update, deactivate, and delete government schemes.
4. **Dynamic Eligibility Rule Builder:** Configure rules per scheme (min/max age, max income, gender, state, occupation, category, and status flags).
5. **User Management:** Activate or deactivate user accounts.

---

## 🛠️ Technology Stack

* **Backend Framework:** Java 23 / 17, Spring Boot 3.2.3, Spring MVC, Spring Data JPA, Hibernate, Spring Security, JWT (jjwt 0.11.5), Maven
* **Database:** Embedded H2 Database with local file persistence (`./data/gov_scheme_db`) & H2 Web Console (`http://localhost:8081/h2-console`). MySQL connector is also included for easy switching.
* **Frontend:** HTML5, CSS3 (Modern Glassmorphism Design System), JavaScript (Vanilla ES6 Fetch API), Bootstrap 5
* **Security:** BCrypt Password Hashing, Stateless JWT Bearer Authentication, Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`)

---

## 🚀 Running the Application

### 1. Prerequisites
* Java JDK 17 or Java JDK 23 installed
* Maven (included wrapper `mvn.cmd` or standard Maven installation)

### 2. Start the Backend Server (Spring Boot)
Open terminal in `backend/` directory:
```bash
cd backend
mvn spring-boot:run
```
* **Backend API Base URL:** `http://localhost:8081/api`
* **H2 Database Web Console:** `http://localhost:8081/h2-console`
  * **JDBC URL:** `jdbc:h2:file:./data/gov_scheme_db`
  * **User:** `sa`
  * **Password:** *(leave blank)*

### 3. Start the Frontend Application
Open terminal in `frontend/` directory:
```bash
cd frontend
python -m http.server 5500
```
Open your browser and navigate to: `http://localhost:5500`

---

## 🔑 Pre-Seeded Demo Credentials

The system auto-seeds demo accounts upon first launch:

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@gov.in` | `admin123` | Full access to Admin Command Center, Scheme CRUD, and Rule Builder |
| **Citizen (Student Demo)** | `user@gov.in` | `user123` | Pre-configured Profile: Age 22, Income ₹180,000, State Tamil Nadu, Student |

---

## 📊 Database Schema Summary

1. `users` (id, name, email, mobile, password, role, status, created_at, updated_at)
2. `user_profiles` (id, user_id, date_of_birth, age, gender, state, district, annual_income, occupation, employment_status, category, student_status, disability_status, farmer_status, entrepreneur_status)
3. `scheme_categories` (id, name, description, icon_name, status)
4. `schemes` (id, category_id, scheme_name, short_description, description, department, scheme_type, benefits, application_url, contact_information, status)
5. `eligibility_rules` (id, scheme_id, min_age, max_age, min_income, max_income, gender, state, district, category, occupation, employment_status, student_status, disability_status, farmer_status, entrepreneur_status)
6. `required_documents` (id, scheme_id, document_name, description, mandatory)
7. `application_procedures` (id, scheme_id, step_number, instruction)
8. `saved_schemes` (id, user_id, scheme_id, saved_at)

---

## 🔌 Core REST API Endpoints

### Authentication APIs
* `POST /api/auth/register` - Register citizen account
* `POST /api/auth/login` - Sign in & get JWT token
* `GET /api/auth/me` - Get current user profile summary

### Recommendation Engine APIs
* `POST /api/recommendations` - Run profile rule evaluation against all active schemes
* `GET /api/recommendations` - Run rule evaluation for logged-in user profile
* `GET /api/eligibility/{schemeId}` - Check profile eligibility for a specific scheme

### Schemes & Categories APIs
* `GET /api/schemes` - Get all active schemes
* `GET /api/schemes/{id}` - Get scheme details by ID
* `GET /api/schemes/category/{categoryId}` - Filter schemes by category
* `GET /api/schemes/search?query=...` - Search schemes by keyword
* `GET /api/categories` - Get all scheme categories

### Citizen Bookmarks & Dashboard APIs
* `GET /api/user/profile` - Fetch current user profile
* `PUT /api/user/profile` - Update current user profile
* `GET /api/user/dashboard` - Get dashboard metrics and saved schemes
* `POST /api/saved-schemes/{schemeId}` - Bookmark a scheme
* `DELETE /api/saved-schemes/{schemeId}` - Remove bookmark

### Admin Management APIs (`ROLE_ADMIN` required)
* `GET /api/admin/stats` - Fetch admin statistics
* `GET /api/admin/users` - Fetch registered users
* `PUT /api/admin/users/{userId}/status?status=...` - Activate / Deactivate user account
* `POST /api/admin/schemes` - Create new scheme
* `PUT /api/admin/schemes/{id}` - Update scheme
* `DELETE /api/admin/schemes/{id}` - Delete scheme
* `POST /api/admin/schemes/{schemeId}/rule` - Configure eligibility rule
