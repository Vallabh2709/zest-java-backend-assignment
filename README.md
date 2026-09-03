Zest Assignment – Java Backend Developer

A production-oriented Java Spring Boot REST API developed as part of the Zest India IT Pvt Ltd Java Backend Developer assignment.

The application provides secure product and item management APIs with JWT authentication, refresh-token rotation, role-based authorization, validation, pagination, centralized exception handling, database persistence, Swagger/OpenAPI documentation, and automated testing.

1. Project Overview

This project implements a RESTful backend for managing Products and their associated Items.

The application follows a layered architecture to keep responsibilities separated:

Client
│
▼
Controller Layer
│
▼
Service Layer
│
▼
Repository Layer
│
▼
MySQL Database

Security is handled using Spring Security and JWT:

Client
│
│ Username + Password
▼
Authentication API
│
├── Access Token (JWT)
│
└── Refresh Token
│
▼
Database
2. Features
   Authentication & Security
   User registration
   User login
   JWT-based authentication
   Refresh tokens
   Refresh-token rotation
   Refresh-token revocation
   Secure password hashing using BCrypt
   Role-based authorization
   USER and ADMIN roles
   Logout with refresh-token revocation
   Refresh-token ownership validation
   Stateless Spring Security configuration
   Configurable CORS
   HTTPS-ready deployment architecture
   Product Management
   Create product
   Get all products
   Get product by ID
   Update product
   Delete product
   Pagination
   Audit information (createdBy, createdOn, modifiedBy, modifiedOn)
   Item Management
   Create item for a product
   Get items belonging to a product
   Pagination for item collections
   Product-item relationship using JPA
   API Quality
   RESTful API design
   /api/v1/ API versioning
   JSON request/response format
   Jakarta Bean Validation
   Centralized exception handling
   Standardized error responses
   HTTP status-code based responses
   Swagger/OpenAPI documentation
   Testing
   JUnit 5
   Mockito
   Spring Boot integration tests
   H2 in-memory database for tests
   Authentication testing
   Authorization testing
   Product CRUD testing
   Item testing
   Validation testing
   Exception testing
   Refresh-token rotation testing
   CORS testing
3. Technology Stack
   Technology	Purpose
   Java 21	Programming language
   Spring Boot	Backend framework
   Spring Web MVC	REST APIs
   Spring Data JPA	Data access
   Hibernate	ORM
   MySQL 8	Production database
   H2	Test database
   Spring Security	Authentication & authorization
   JWT	Access-token authentication
   BCrypt	Password hashing
   Jakarta Validation	Request validation
   JUnit 5	Unit testing
   Mockito	Mock-based testing
   Spring Boot Test	Integration testing
   Swagger / OpenAPI	API documentation
   Maven	Build and dependency management
   Docker	Containerization
   Docker Compose	Multi-container deployment
4. Architecture

The project follows a layered architecture.

┌─────────────────────────────┐
│          Client             │
│     Swagger / Postman       │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│       Controller Layer      │
│                             │
│ AuthController              │
│ ProductController           │
│ ItemController              │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│        Service Layer        │
│                             │
│ AuthService                 │
│ ProductService              │
│ ItemService                 │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│      Repository Layer       │
│                             │
│ UserRepository              │
│ RefreshTokenRepository      │
│ ProductRepository           │
│ ItemRepository              │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│        MySQL Database       │
│                             │
│ users                       │
│ refresh_tokens              │
│ product                     │
│ item                        │
└─────────────────────────────┘
Security Flow
Request
│
▼
JWT Authentication Filter
│
├── Valid JWT ──────► SecurityContext
│
└── Invalid JWT ────► Authentication failure
│
▼
Standard 401 Response
5. Project Structure
   src/
   ├── main/
   │   ├── java/
   │   │   └── com/
   │   │       └── zest/
   │   │           └── assignment/
   │   │               │
   │   │               ├── config/
   │   │               │   └── SecurityConfig.java
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
   │   │               │   ├── GlobalExceptionHandler.java
   │   │               │   ├── BadRequestException.java
   │   │               │   ├── ConflictException.java
   │   │               │   ├── UnauthorizedException.java
   │   │               │   └── ResourceNotFoundException.java
   │   │               │
   │   │               ├── repository/
   │   │               │   ├── UserRepository.java
   │   │               │   ├── RefreshTokenRepository.java
   │   │               │   ├── ProductRepository.java
   │   │               │   └── ItemRepository.java
   │   │               │
   │   │               ├── security/
   │   │               │   ├── JwtService.java
   │   │               │   ├── JwtAuthenticationFilter.java
   │   │               │   ├── CustomUserDetailsService.java
   │   │               │   ├── RestAuthenticationEntryPoint.java
   │   │               │   └── RestAccessDeniedHandler.java
   │   │               │
   │   │               ├── service/
   │   │               │   ├── AuthService.java
   │   │               │   ├── ProductService.java
   │   │               │   ├── ItemService.java
   │   │               │   └── impl/
   │   │               │       ├── AuthServiceImpl.java
   │   │               │       ├── ProductServiceImpl.java
   │   │               │       └── ItemServiceImpl.java
   │   │               │
   │   │               └── ZestAssignmentApplication.java
   │   │
   │   └── resources/
   │       └── application.properties
   │
   └── test/
   ├── java/
   │   └── com/zest/assignment/
   │       ├── controller/
   │       ├── service/
   │       └── integration/
   │
   └── resources/
   └── application-test.properties
6. Database Design

The application uses MySQL for persistence.

Users
users
--------------------------------
id
username
password
role
created_on
modified_on

username is unique.

Passwords are stored using BCrypt hashing rather than plain text.

Refresh Tokens
refresh_tokens
--------------------------------
id
token
user_id
expires_at
revoked
created_on

Each refresh token belongs to a user.

Indexes are maintained on:

user_id
token
Product
product
--------------------------------
id
product_name
created_by
created_on
modified_by
modified_on
Item
item
--------------------------------
id
product_id
quantity

Relationship:

Product 1 ──────────── * Item

Each item belongs to exactly one product.

7. Role-Based Authorization

The application defines two roles.

Operation	USER	ADMIN
Register	✅	✅
Login	✅	✅
Refresh token	✅	✅
Logout	✅	✅
View products	✅	✅
View product by ID	✅	✅
View product items	✅	✅
Create product	❌	✅
Update product	❌	✅
Delete product	❌	✅
Create item	❌	✅

Public registration always creates a USER.

Users cannot register themselves as ADMIN.

8. Authentication
   Registration
   POST /api/v1/auth/register

Example request:

{
"username": "john",
"password": "Password@123"
}
Login
POST /api/v1/auth/login

Example:

{
"username": "john",
"password": "Password@123"
}

Response contains:

{
"accessToken": "JWT_TOKEN",
"refreshToken": "REFRESH_TOKEN",
"tokenType": "Bearer",
"expiresIn": 900
}
Refresh Token
POST /api/v1/auth/refresh

The refresh-token flow uses rotation.

Old Refresh Token
│
▼
Validate
│
▼
Revoke
│
▼
Generate New Refresh Token
│
▼
Generate New Access Token

A revoked refresh token cannot be reused.

Logout
POST /api/v1/auth/logout

Logout revokes the supplied refresh token after verifying that it belongs to the authenticated user.

9. API Endpoints
   Authentication
   Method	Endpoint	Access
   POST	/api/v1/auth/register	Public
   POST	/api/v1/auth/login	Public
   POST	/api/v1/auth/refresh	Public
   POST	/api/v1/auth/logout	Authenticated
   Products
   Method	Endpoint	Access
   GET	/api/v1/products	USER / ADMIN
   GET	/api/v1/products/{id}	USER / ADMIN
   POST	/api/v1/products	ADMIN
   PUT	/api/v1/products/{id}	ADMIN
   DELETE	/api/v1/products/{id}	ADMIN
   GET	/api/v1/products/{id}/items	USER / ADMIN
   Items

The assignment requires retrieving product items. An additional endpoint is provided to allow ADMIN users to populate the item relationship:

POST /api/v1/products/{productId}/items

Access:

ADMIN
10. Pagination

Collection endpoints support pagination.

Example:

GET /api/v1/products?page=0&size=10

Example response:

{
"content": [],
"page": 0,
"size": 10,
"totalElements": 25,
"totalPages": 3,
"first": true,
"last": false
}

Pagination is implemented using Spring Data Pageable.

11. Validation

Jakarta Bean Validation is used for incoming requests.

Examples:

Username
Minimum length: 3
Maximum length: 100
Password
Minimum length: 8
Maximum length: 100
Product name
Required
Maximum length: 255
Item quantity
Required
Must be positive

Invalid requests return HTTP 400 Bad Request.

12. Error Handling

The application uses a centralized GlobalExceptionHandler.

Errors follow a consistent format:

{
"timestamp": "2026-09-04T02:30:00",
"status": 404,
"error": "NOT_FOUND",
"message": "Product not found",
"path": "/api/v1/products/100"
}

Common HTTP responses:

Status	Meaning
200	Successful request
201	Resource created
204	Resource deleted
400	Invalid request
401	Authentication required/invalid
403	Access denied
404	Resource not found
409	Conflict
500	Internal server error
13. Database Indexing Strategy

Indexes are used on frequently searched or relationship columns.

Current indexes include:

users.username
refresh_tokens.token
refresh_tokens.user_id
product.product_name
item.product_id

The product_id index on item improves queries that retrieve all items belonging to a product.

14. Swagger / OpenAPI

Swagger/OpenAPI is included for interactive API documentation.

After starting the application, open:

http://localhost:8080/swagger-ui/index.html

Swagger can be used to:

View available endpoints
Inspect request/response models
Test APIs
Authenticate using JWT
Review API parameters and responses
15. Testing

The project uses multiple testing approaches.

Unit Testing

Implemented with:

JUnit 5
Mockito
Integration Testing

Implemented using:

Spring Boot Test
H2 in-memory database
Test Coverage Areas

Tests cover:

Authentication
Login
Registration
Invalid credentials
JWT/security behavior
Role-based authorization
Refresh-token rotation
Refresh-token revocation
Logout
Refresh-token ownership
Product CRUD
Product validation
Item operations
Pagination
Exception handling
CORS
Integration behavior
Current Test Result
Tests run: 58
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS

Run the test suite with:

./mvnw clean test

On Windows:

.\mvnw.cmd clean test
16. Environment Configuration

Sensitive configuration should be provided through environment variables.

Example:

APP_JWT_SECRET=your-base64-encoded-secret
APP_JWT_EXPIRATION_MS=900000
APP_REFRESH_TOKEN_EXPIRATION_DAYS=7

CORS_ALLOWED_ORIGINS=http://localhost:3000

ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-admin-password
Security

The .env file should not be committed to GitHub.

Use .env.example to document required variables without exposing real credentials.

17. Running Locally
    Prerequisites

Install:

Java 21
Maven or use the included Maven Wrapper
MySQL 8+
1. Clone the repository
   git clone <YOUR_GITHUB_REPOSITORY_URL>
   cd zest-assignment
2. Configure MySQL

Create a database:

CREATE DATABASE ZestAssignment;

Configure the database credentials through environment variables or application.properties.

3. Run the application

Windows:

.\mvnw.cmd spring-boot:run

Linux/macOS:

./mvnw spring-boot:run

Application:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui/index.html
18. Docker Deployment

The project includes Docker configuration for running the application together with MySQL.

Architecture:

┌──────────────────────┐
│   Spring Boot App    │
│       :8080          │
└──────────┬───────────┘
│
│ Docker Network
│
┌──────────▼───────────┐
│        MySQL         │
│        :3306         │
└──────────────────────┘

Build:

docker compose build

Start:

docker compose up -d

Check containers:

docker compose ps

View application logs:

docker compose logs -f app

Stop:

docker compose down

Persistent MySQL data is stored using a Docker volume.

Docker configuration is intended for containerized deployment. Production secrets should be supplied through a secure environment/secrets-management mechanism rather than committed to source control.

19. HTTPS & Deployment Security

For production deployment, HTTPS should be terminated at the infrastructure layer, such as a reverse proxy or load balancer.

Recommended architecture:

Internet
│
│ HTTPS
▼
┌─────────────────────┐
│ Reverse Proxy / LB  │
│ TLS Termination     │
└──────────┬──────────┘
│
│ Forwarded Headers
▼
┌─────────────────────┐
│    Spring Boot      │
│       :8080         │
└──────────┬──────────┘
│
▼
┌─────────────────────┐
│       MySQL         │
└─────────────────────┘

Spring Boot is configured to process forwarded headers so that the application can operate correctly behind a reverse proxy.

20. Security Considerations

The application follows several security practices:

Passwords are hashed using BCrypt.
JWT access tokens are used for stateless authentication.
Refresh tokens have expiration times.
Refresh tokens are revoked during rotation.
Refresh-token ownership is checked during logout.
Public registration cannot assign the ADMIN role.
Role-based authorization protects administrative operations.
Sensitive configuration is externalized.
CORS origins are configurable.
Standardized authentication and authorization error responses are provided.
Database access uses JPA repositories.
Production HTTPS is expected through the deployment infrastructure.
21. Design Decisions
    DTOs instead of exposing entities

Request and response DTOs are used to prevent JPA entities from being directly exposed through the REST API.

Service layer

Business logic is kept inside services instead of controllers.

Repository layer

Database operations are isolated inside Spring Data repositories.

Stateless authentication

The application uses JWT authentication and does not maintain server-side HTTP sessions.

Refresh-token rotation

Every successful refresh invalidates the previous refresh token and generates a new one.

Product deletion

A product with associated items is not deleted automatically. The application returns a conflict response instead of silently deleting related data.

Async processing

The current APIs are primarily CRUD and authentication operations, so asynchronous processing is not forced into operations where it would not provide a meaningful benefit.

22. Future Improvements

Possible production enhancements include:

Database migration management using Flyway or Liquibase
Hashing refresh tokens before database storage
Redis-based token/session management
Rate limiting for authentication endpoints
Centralized logging
Monitoring and metrics
CI/CD pipeline
Automated security scanning
Reverse proxy configuration with TLS certificates
Cloud deployment
More comprehensive API contract testing
23. Build

Build the application:

./mvnw clean package

Windows:

.\mvnw.cmd clean package

Run tests:

.\mvnw.cmd clean test
24. Project Status

Current backend validation:

✅ Spring Boot application
✅ REST API
✅ JWT authentication
✅ Refresh-token rotation
✅ Role-based authorization
✅ Product CRUD
✅ Item management
✅ Pagination
✅ Validation
✅ Global exception handling
✅ CORS
✅ Database indexing
✅ JUnit 5
✅ Mockito
✅ Integration testing
✅ H2 test database
✅ Swagger/OpenAPI
✅ GitHub repository
🟡 Docker deployment verification
25. Author

Java Backend Developer Assignment

Built using Java, Spring Boot, Spring Security, JPA/Hibernate, MySQL, JWT, JUnit 5, Mockito and Docker.