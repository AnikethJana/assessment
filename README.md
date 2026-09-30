# User Management System (Spring Boot + JWT + RBAC)

> [!TIP]
> **Quick Start / Get Authorised:**
> Paste this in `POST /api/auth/login` (or in Swagger UI at http://localhost:9990/swagger-ui.html) to get authorised:
> ```json
> {
>   "email": "admin@example.com",
>   "password": "Admin@123"
> }
> ```

A RESTful User Management System built with Spring Boot, featuring Role-Based Access Control (RBAC), JWT token authentication, in-memory relational database persistence, input validation, and centralized error handling.

---

## Features

- **Role-Based Access Control (RBAC):**
  - **Admin:** Full CRUD operations on users, role assignment.
  - **Manager:** View all users with assigned tasks, assign tasks to users.
  - **User:** View own profile and own assigned tasks only.
- **JWT Authentication:** Stateless Bearer token authentication powered by `io.jsonwebtoken` (JJWT 0.12.x).
- **Password Security:** Passwords hashed with BCrypt.
- **Data Persistence:** In-memory H2 relational database with pre-populated sample data and web console enabled.
- **Validation & Error Handling:**
  - Password strength validation (minimum 8 characters with at least 1 uppercase letter, 1 lowercase letter, and 1 number).
  - Email format validation (`@Email`).
  - Duplicate user / email conflict prevention.
  - Custom JSON responses for 401 Unauthorized (invalid/expired/missing JWT) and 403 Forbidden (insufficient role).

---

## Tech Stack

- **Java:** 21+
- **Framework:** Spring Boot (Spring Web MVC, Spring Security, Spring Data JPA, Spring Validation)
- **Database:** H2 In-Memory Database
- **JWT Library:** JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.6)
- **Build Tool:** Maven (via `mvnw` wrapper)

---

## Pre-populated Sample Data

The database is seeded on startup with the following test accounts:

| Role | Name | Email | Password | Allowed Access |
|---|---|---|---|---|
| **Admin** | System Admin | `admin@example.com` | `Admin@123` | `/api/admin/**`, `/api/user/**` |
| **Manager** | Project Manager | `manager@example.com` | `Manager@123` | `/api/manager/**`, `/api/user/**` |
| **User** | John Doe | `user@example.com` | `User@123` | `/api/user/**` |
| **Manager + User** | Alice Lead | `lead@example.com` | `Lead@123` | `/api/manager/**`, `/api/user/**` |

---

## Quick Start

### 1. Run the Application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The server will start on port **`9990`**.

### 2. Run Tests
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### 3. H2 Console
- **URL:** `http://localhost:9990/h2-console`
- **JDBC URL:** `jdbc:h2:mem:assessmentdb`
- **Username:** `sa`
- **Password:** *(leave empty)*

---

## API Documentation

All protected endpoints require the HTTP header:
```
Authorization: Bearer <your_jwt_token>
```

### 1. Authentication (Public)

#### Login & Obtain JWT Token
- **Endpoint:** `POST /api/auth/login`
- **Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "Admin@123"
}
```
- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "System Admin",
    "email": "admin@example.com",
    "roles": ["ROLE_ADMIN"]
  },
  "timestamp": "2026-09-30T17:40:00"
}
```

---

### 2. Admin APIs (`ROLE_ADMIN` Only)

#### Create User
- **Endpoint:** `POST /api/admin/users`
- **Request Body:**
```json
{
  "name": "Sarah Connor",
  "email": "sarah@example.com",
  "password": "Password@123",
  "roles": ["ROLE_USER"]
}
```
- **Response:** `201 Created` with the created user object.

#### List All Users
- **Endpoint:** `GET /api/admin/users`
- **Response:** `200 OK` with list of users, their roles, and assigned tasks.

#### Get User by ID
- **Endpoint:** `GET /api/admin/users/{id}`
- **Response:** `200 OK` with user details.

#### Update User
- **Endpoint:** `PUT /api/admin/users/{id}`
- **Request Body:**
```json
{
  "name": "Sarah Connor Updated",
  "email": "sarah.updated@example.com",
  "password": "NewPassword@123"
}
```
- **Response:** `200 OK` with updated user details.

#### Delete User
- **Endpoint:** `DELETE /api/admin/users/{id}`
- **Response:** `200 OK` `{"status": 200, "message": "User deleted successfully"}`.

#### Assign Roles to User
- **Endpoint:** `POST /api/admin/users/{id}/roles`
- **Request Body:**
```json
{
  "roles": ["ROLE_USER", "ROLE_MANAGER"]
}
```
- **Response:** `200 OK` with updated user and assigned roles.

---

### 3. Manager APIs (`ROLE_MANAGER` Only)

#### View All Users and Assigned Tasks
- **Endpoint:** `GET /api/manager/users`
- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Users with assigned tasks retrieved successfully",
  "data": [
    {
      "id": 3,
      "name": "John Doe",
      "email": "user@example.com",
      "roles": ["ROLE_USER"],
      "tasks": [
        {
          "id": 1,
          "title": "Complete unit test coverage",
          "userId": 3,
          "userName": "John Doe"
        }
      ]
    }
  ]
}
```

#### Assign Task to User
- **Endpoint:** `POST /api/manager/tasks`
- **Request Body:**
```json
{
  "userId": 3,
  "title": "Prepare release notes"
}
```
- **Response:** `201 Created` with task details.

---

### 4. User APIs (All Authenticated Users)

#### View Own Profile
- **Endpoint:** `GET /api/user/profile`
- **Response:** `200 OK` with caller's user profile and role details.

#### View Own Assigned Tasks Only
- **Endpoint:** `GET /api/user/tasks`
- **Response:** `200 OK` with only the tasks assigned to the caller.

---

## Validation & Error Responses

Standardized JSON error format:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input data validation failed. Please check the errors field.",
  "timestamp": "2026-09-30T17:40:00",
  "validationErrors": {
    "password": "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number",
    "email": "Invalid email format"
  }
}
```

| HTTP Status | Scenario | Description |
|---|---|---|
| **400 Bad Request** | Validation Error | Invalid email format, weak password, or missing required fields |
| **401 Unauthorized** | Authentication Error | Bad credentials, or missing / invalid / expired JWT token |
| **403 Forbidden** | Authorization Error | Accessing an endpoint without the required role (e.g., User accessing Admin API) |
| **404 Not Found** | Resource Not Found | Requested user ID does not exist |
| **409 Conflict** | Duplicate User | Attempting to create or update a user with an email that is already registered |
