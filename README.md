# Zest Assignment – Java Backend Developer

A secure RESTful backend application developed as part of the **Zest India IT Pvt Ltd Java Backend Developer assignment**.

The application provides product and item management APIs with JWT authentication, refresh-token rotation, role-based authorization, validation, pagination, centralized exception handling, MySQL persistence, Swagger/OpenAPI documentation, and automated testing.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Database Design](#database-design)
- [Authentication and Authorization](#authentication-and-authorization)
- [API Endpoints](#api-endpoints)
- [Request Examples](#request-examples)
- [Pagination](#pagination)
- [Validation](#validation)
- [Error Handling](#error-handling)
- [Database Indexing](#database-indexing)
- [Swagger / OpenAPI](#swagger--openapi)
- [Testing](#testing)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Docker Deployment](#docker-deployment)
- [HTTPS and Production Deployment](#https-and-production-deployment)
- [Security Considerations](#security-considerations)
- [Design Decisions](#design-decisions)
- [Future Improvements](#future-improvements)
- [Project Status](#project-status)

---

## Project Overview

This project implements a RESTful backend for managing **Products** and their associated **Items**.

The application follows a layered architecture with clear separation of responsibilities.

```text
Client
   |
   v
Controller Layer
   |
   v
Service Layer
   |
   v
Repository Layer
   |
   v
MySQL Database
```

Authentication is implemented using Spring Security and JWT.

```text
Username + Password
        |
        v
 Authentication
        |
        +--------------------+
        |                    |
        v                    v
 Access Token         Refresh Token
    (JWT)              (Database)
```

---

## Key Features

### Authentication and Security

- User registration
- User login
- JWT-based authentication
- Refresh tokens
- Refresh-token rotation
- Refresh-token revocation
- BCrypt password hashing
- Role-based authorization
- `USER` and `ADMIN` roles
- Secure logout
- Refresh-token ownership validation
- Stateless Spring Security configuration
- Configurable CORS
- HTTPS-ready deployment architecture

### Product Management

- Create product
- Get all products
- Get product by ID
- Update product
- Delete product
- Pagination
- Creation and modification audit information

### Item Management

- Create an item for a product
- Get items belonging to a product
- Pagination for item collections
- Product-item relationship using JPA

### API Quality

- RESTful API design
- `/api/v1/` API versioning
- JSON request/response format
- Jakarta Bean Validation
- Centralized exception handling
- Standardized error responses
- Appropriate HTTP status codes
- Swagger/OpenAPI documentation

### Testing

- JUnit 5
- Mockito
- Spring Boot integration tests
- H2 in-memory database
- Authentication testing
- Authorization testing
- CRUD testing
- Validation testing
- Exception testing
- Refresh-token testing
- CORS testing

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Backend framework |
| Spring Web MVC | REST API |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL 8 | Application database |
| H2 | Test database |
| Spring Security | Authentication and authorization |
| JWT | Access-token authentication |
| BCrypt | Password hashing |
| Jakarta Validation | Request validation |
| JUnit 5 | Unit testing |
| Mockito | Mock-based testing |
| Spring Boot Test | Integration testing |
| Swagger / OpenAPI | API documentation |
| Maven | Build and dependency management |
| Docker | Containerization |
| Docker Compose | Multi-container deployment |

---

## Architecture

The application follows a layered architecture.

```text
+-----------------------------+
|           Client            |
|      Swagger / Postman      |
+-------------+---------------+
              |
              v
+-----------------------------+
|      Controller Layer       |
|                             |
| AuthController              |
| ProductController           |
| ItemController              |
+-------------+---------------+
              |
              v
+-----------------------------+
|        Service Layer        |
|                             |
| AuthService                 |
| ProductService              |
| ItemService                 |
+-------------+---------------+
              |
              v
+-----------------------------+
|      Repository Layer       |
|                             |
| UserRepository              |
| RefreshTokenRepository      |
| ProductRepository           |
| ItemRepository              |
+-------------+---------------+
              |
              v
+-----------------------------+
|       MySQL Database        |
|                             |
| users                       |
| refresh_tokens              |
| product                     |
| item                        |
+-----------------------------+
```

### Security Flow

```text
HTTP Request
     |
     v
JWT Authentication Filter
     |
     +---- Valid JWT ----> SecurityContext
     |
     +---- Invalid JWT --> Authentication Failure
```

---

## Project Structure

```text
zest-assignment/
│
├── .gitignore
├── .gitattributes
├── pom.xml
├── mvnw
├── mvnw.cmd
│
├── .mvn/
│
└── src/
    │
    ├── main/
    │   │
    │   ├── java/
    │   │   └── com/
    │   │       └── zest/
    │   │           └── assignment/
    │   │               │
    │   │               ├── config/
    │   │               │
    │   │               ├── controller/
    │   │               │   ├── AuthController.java
    │   │               │   ├── ProductController.java
    │   │               │   └── ItemController.java
    │   │               │
    │   │               ├── dto/
    │   │               │   ├── auth/
    │   │               │   ├── product/
    │   │               │   ├── item/
    │   │               │   └── common/
    │   │               │
    │   │               ├── entity/
    │   │               │   ├── User.java
    │   │               │   ├── RefreshToken.java
    │   │               │   ├── Product.java
    │   │               │   └── Item.java
    │   │               │
    │   │               ├── enums/
    │   │               │   └── Role.java
    │   │               │
    │   │               ├── exception/
    │   │               │
    │   │               ├── repository/
    │   │               │
    │   │               ├── security/
    │   │               │
    │   │               └── service/
    │   │                   ├── AuthService.java
    │   │                   ├── ProductService.java
    │   │                   ├── ItemService.java
    │   │                   └── impl/
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        │
        ├── java/
        │   └── com/
        │       └── zest/
        │           └── assignment/
        │               ├── controller/
        │               ├── service/
        │               └── integration/
        │
        └── resources/
            └── application-test.properties
```

---

## Database Design

The application uses **MySQL** for persistence.

### Users

```text
users
--------------------------------
id
username
password
role
created_on
modified_on
```

`username` is unique.

Passwords are stored using BCrypt hashing.

### Refresh Tokens

```text
refresh_tokens
--------------------------------
id
token
user_id
expires_at
revoked
created_on
```

Indexes are maintained on `token` and `user_id`.

### Product

```text
product
--------------------------------
id
product_name
created_by
created_on
modified_by
modified_on
```

### Item

```text
item
--------------------------------
id
product_id
quantity
```

Relationship:

```text
Product 1 -------- * Item
```

Each item belongs to exactly one product.

---

## Authentication and Authorization

The application defines two roles:

| Operation | USER | ADMIN |
|---|:---:|:---:|
| Register | Yes | Yes |
| Login | Yes | Yes |
| Refresh token | Yes | Yes |
| Logout | Yes | Yes |
| View products | Yes | Yes |
| View product | Yes | Yes |
| View product items | Yes | Yes |
| Create product | No | Yes |
| Update product | No | Yes |
| Delete product | No | Yes |
| Create item | No | Yes |

Public registration always creates a `USER`.

Users cannot assign themselves the `ADMIN` role.

### Login Flow

```text
Username + Password
        |
        v
AuthenticationManager
        |
        v
JWT Access Token + Refresh Token
```

### Refresh Token Rotation

```text
Old Refresh Token
        |
        v
     Validate
        |
        v
      Revoke
        |
        v
Generate New Refresh Token
        |
        v
Generate New Access Token
```

After successful rotation, the old refresh token cannot be reused.

---

## API Endpoints

All APIs use the `/api/v1/` version prefix.

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Public |
| `POST` | `/api/v1/auth/login` | Public |
| `POST` | `/api/v1/auth/refresh` | Public |
| `POST` | `/api/v1/auth/logout` | Authenticated |

### Products

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/v1/products` | USER / ADMIN |
| `GET` | `/api/v1/products/{id}` | USER / ADMIN |
| `POST` | `/api/v1/products` | ADMIN |
| `PUT` | `/api/v1/products/{id}` | ADMIN |
| `DELETE` | `/api/v1/products/{id}` | ADMIN |
| `GET` | `/api/v1/products/{id}/items` | USER / ADMIN |

### Items

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/api/v1/products/{productId}/items` | ADMIN |

The item creation endpoint is provided to allow ADMIN users to populate the product-item relationship.

---

## Request Examples

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "username": "john",
  "password": "Password@123"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "username": "john",
  "password": "Password@123"
}
```

Example response:

```json
{
  "accessToken": "JWT_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Get Products

```http
GET /api/v1/products?page=0&size=10
Authorization: Bearer <access-token>
```

### Create Product

```http
POST /api/v1/products
Authorization: Bearer <admin-access-token>
Content-Type: application/json
```

```json
{
  "productName": "Laptop"
}
```

---

## Pagination

Collection endpoints support pagination using Spring Data `Pageable`.

Example:

```http
GET /api/v1/products?page=0&size=10
```

Example response:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

Pagination is supported for:

- Product collections
- Product item collections

---

## Validation

Jakarta Bean Validation is used for incoming requests.

| Field | Validation |
|---|---|
| Username | Required, 3–100 characters |
| Password | Required, 8–100 characters |
| Product name | Required, maximum 255 characters |
| Quantity | Required and must be positive |

Invalid requests return `400 Bad Request`.

---

## Error Handling

The application uses a centralized `GlobalExceptionHandler`.

Example:

```json
{
  "timestamp": "2026-09-04T02:30:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Product not found",
  "path": "/api/v1/products/100"
}
```

### HTTP Status Codes

| Status | Meaning |
|---:|---|
| `200` | Successful request |
| `201` | Resource created |
| `204` | Resource deleted |
| `400` | Invalid request |
| `401` | Authentication required or invalid |
| `403` | Access denied |
| `404` | Resource not found |
| `409` | Conflict |
| `500` | Internal server error |

---

## Database Indexing

Indexes are provided for frequently queried columns and relationships.

```text
users.username
refresh_tokens.token
refresh_tokens.user_id
product.product_name
item.product_id
```

The `item.product_id` index improves queries that retrieve all items belonging to a product.

---

## Swagger / OpenAPI

Swagger/OpenAPI is included for interactive API documentation.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to:

- View available endpoints
- Inspect request and response models
- Test APIs
- Authenticate using JWT
- Review API parameters and responses

---

## Testing

The project uses:

- JUnit 5
- Mockito
- Spring Boot Test
- H2 in-memory database

### Test Coverage Areas

Tests cover:

- User registration
- Login
- Invalid credentials
- JWT/security behavior
- Role-based authorization
- Refresh-token rotation
- Refresh-token revocation
- Refresh-token ownership validation
- Logout
- Product CRUD
- Product validation
- Item operations
- Pagination
- Exception handling
- CORS
- Integration behavior

### Test Result

```text
Tests run: 58
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Run the tests on Windows:

```powershell
.\mvnw.cmd clean test
```

Run on Linux/macOS:

```bash
./mvnw clean test
```

---

## Configuration

Sensitive configuration should be supplied through environment variables.

Example:

```env
APP_JWT_SECRET=your-base64-encoded-secret
APP_JWT_EXPIRATION_MS=900000
APP_REFRESH_TOKEN_EXPIRATION_DAYS=7

CORS_ALLOWED_ORIGINS=http://localhost:3000

ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-admin-password
```

### Important

Do not commit `.env` or real credentials to GitHub.

Use `.env.example` with placeholder values to document required environment variables.

---

## Running Locally

### Prerequisites

- Java 21
- MySQL 8+
- Git

The project includes Maven Wrapper, so Maven does not need to be installed separately.

### 1. Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd zest-assignment
```

### 2. Create the database

```sql
CREATE DATABASE ZestAssignment;
```

### 3. Configure the application

Configure database credentials, JWT settings, CORS, and admin credentials using environment variables or `application.properties`.

### 4. Run the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Docker Deployment

The project includes Docker configuration for running the Spring Boot application together with MySQL.

### Architecture

```text
+-----------------------+
|    Spring Boot App    |
|        :8080          |
+-----------+-----------+
            |
            | Docker Network
            |
+-----------v-----------+
|         MySQL         |
|        :3306          |
+-----------------------+
```

### Build

```bash
docker compose build
```

### Start

```bash
docker compose up -d
```

### Check containers

```bash
docker compose ps
```

### View application logs

```bash
docker compose logs -f app
```

### Stop

```bash
docker compose down
```

MySQL data is persisted using a Docker volume.

> Docker configuration is included for containerized deployment. Production secrets should be supplied using secure environment variables or a secrets-management system.

---

## HTTPS and Production Deployment

For production deployment, HTTPS should be terminated at the infrastructure layer, such as a reverse proxy or load balancer.

Recommended architecture:

```text
Internet
    |
    | HTTPS
    v
+----------------------+
| Reverse Proxy / LB   |
| TLS Termination      |
+----------+-----------+
           |
           | Forwarded Headers
           v
+----------------------+
|    Spring Boot       |
|        :8080         |
+----------+-----------+
           |
           v
+----------------------+
|        MySQL         |
+----------------------+
```

Spring Boot is configured to process forwarded headers so that the application can operate correctly behind a reverse proxy.

---

## Security Considerations

The application implements the following security practices:

- BCrypt password hashing
- JWT-based stateless authentication
- Refresh-token expiration
- Refresh-token rotation
- Refresh-token revocation
- Refresh-token ownership validation during logout
- Role-based authorization
- Public registration cannot create ADMIN users
- Sensitive configuration is externalized
- Configurable CORS
- Centralized authentication and authorization error responses
- HTTPS-ready deployment architecture

---

## Design Decisions

### DTOs

Request and response DTOs are used instead of exposing JPA entities directly through REST endpoints.

### Service Layer

Business logic is kept inside service classes rather than controllers.

### Repository Layer

Database operations are isolated inside Spring Data JPA repositories.

### Stateless Authentication

The application uses JWT authentication and does not maintain server-side HTTP sessions.

### Refresh-Token Rotation

Each successful refresh invalidates the previous refresh token and creates a new refresh token.

### Product Deletion

A product with associated items is not automatically deleted. The API returns a conflict response to prevent unintended data loss.

### Asynchronous Processing

The current operations are primarily CRUD and authentication operations. Asynchronous processing was therefore not forced into operations where it would not provide a meaningful benefit.

---

## Future Improvements

Possible production enhancements include:

- Flyway or Liquibase database migrations
- Hashing refresh tokens before database storage
- Redis-based token/session management
- Authentication rate limiting
- Centralized logging
- Monitoring and metrics
- CI/CD pipeline
- Automated security scanning
- Reverse proxy configuration with TLS certificates
- Cloud deployment
- More comprehensive API contract testing

---

## Project Status

| Component | Status |
|---|---|
| Spring Boot Backend | Complete |
| REST API | Complete |
| JWT Authentication | Complete |
| Refresh Token Rotation | Complete |
| Role-Based Authorization | Complete |
| Product CRUD | Complete |
| Item Management | Complete |
| Pagination | Complete |
| Validation | Complete |
| Global Exception Handling | Complete |
| CORS | Complete |
| Database Indexing | Complete |
| JUnit 5 / Mockito | Complete |
| Integration Testing | Complete |
| H2 Test Database | Complete |
| Swagger / OpenAPI | Implemented |
| GitHub Repository | Complete |
| Docker Configuration | Added |
| Docker Runtime Verification | Pending |

---

## Author

**Java Backend Developer Assignment**

Built using Java, Spring Boot, Spring Security, JPA/Hibernate, MySQL, JWT, JUnit 5, Mockito, and Docker.