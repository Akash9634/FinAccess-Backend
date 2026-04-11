# Finance Data Processing and Access Control Backend

A backend API for a finance dashboard system built with **Java Spring Boot**, **Spring Security (JWT)**, and **SQLite**. The system supports financial record management, role-based access control, and dashboard-level analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Database | SQLite via Hibernate |
| ORM | Spring Data JPA |
| Validation | Jakarta Validation |
| Build Tool | Maven |
| API Documentation | Springdoc OpenAPI (Swagger UI) |

---

## Project Structure

```
com.finaccess.api
├── config/                  ← Security config, JWT filter, OpenAPI config
├── controller/              ← REST controllers (Auth, User, Record, Dashboard)
├── service/                 ← Business logic (Auth, User, Record, Dashboard, JWT)
├── repository/              ← Spring Data JPA repositories
├── model/                   ← JPA entities (User, FinancialRecord)
├── dto/                     ← Request/Response DTOs
├── enums/                   ← Role, RecordType
└── exception/               ← Global exception handler, custom exceptions
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Setup and Run

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd finaccess-api

# 2. Run the application
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. SQLite database file `finance.db` is auto-created in the project root on first run.

### Configuration

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlite:finance.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

jwt.secret=finance-dashboard-super-secret-key-2024
jwt.expiration=86400000
```

> **Note:** In a production system, `jwt.secret` should be stored in environment variables, not in `application.properties`.

---

## API Documentation

Once the application is running, visit:

```
http://localhost:8080/swagger-ui/index.html
```

Swagger UI provides an interactive interface to explore and test all endpoints directly from the browser.

To authenticate in Swagger UI:
1. Call `POST /api/auth/login` to get your token
2. Click the **Authorize** button at the top right
3. Enter `Bearer <your_token>`
4. All subsequent requests will include the token automatically

---

## Roles and Permissions

| Action | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| Register / Login | ✅ | ✅ | ✅ |
| View records | ✅ | ✅ | ✅ |
| Filter / search records | ✅ | ✅ | ✅ |
| Create records | ❌ | ❌ | ✅ |
| Update records | ❌ | ❌ | ✅ |
| Delete records | ❌ | ❌ | ✅ |
| View dashboard summary | ❌ | ✅ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

Access control is enforced at the controller level using Spring Security's `@PreAuthorize` annotation.

---

## API Reference

### Authentication — `/api/auth` (public)

#### Register
```
POST /api/auth/register
```
```json
{
    "username": "akash123",
    "email": "akash123@gmail.com",
    "password": "password123",
    "role": "ADMIN"
}
```

#### Login
```
POST /api/auth/login
```
```json
{
    "email": "akash123@gmail.com",
    "password": "password123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "akash123",
    "email": "akash123@gmail.com",
    "role": "ADMIN"
}
```

> Use the returned token in all subsequent requests as:
> `Authorization: Bearer <token>`

---

### User Management — `/api/users` (ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users` | Get all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PATCH` | `/api/users/{id}/role?role=ANALYST` | Update user role |
| `PATCH` | `/api/users/{id}/status?active=false` | Activate or deactivate user |
| `DELETE` | `/api/users/{id}` | Delete user |

---

### Financial Records — `/api/records`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/records` | ADMIN | Create single record |
| `POST` | `/api/records/bulk` | ADMIN | Create multiple records |
| `GET` | `/api/records` | ALL | Get records with optional filters |
| `GET` | `/api/records/{id}` | ALL | Get single record |
| `PUT` | `/api/records/{id}` | ADMIN | Update record |
| `DELETE` | `/api/records/{id}` | ADMIN | Soft delete record |

#### Create Record
```json
{
    "amount": 75000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2024-03-01",
    "notes": "Monthly salary"
}
```

#### Filtering and Search Query Params

```
GET /api/records?type=INCOME
GET /api/records?type=EXPENSE&category=Rent
GET /api/records?from=2024-01-01&to=2024-03-31
GET /api/records?keyword=salary
GET /api/records?type=EXPENSE&keyword=food&page=0&size=10
```

| Param | Type | Description |
|---|---|---|
| `type` | `INCOME` or `EXPENSE` | Filter by record type |
| `category` | String | Filter by category (case insensitive) |
| `from` | `yyyy-MM-dd` | Filter from date |
| `to` | `yyyy-MM-dd` | Filter to date |
| `keyword` | String | Search in category and notes |
| `page` | Integer (default 0) | Page number |
| `size` | Integer (default 10) | Page size |

---

### Dashboard Summary — `/api/dashboard` (ANALYST, ADMIN)

```
GET /api/dashboard/summary
```

**Response:**
```json
{
    "totalIncome": 95000.00,
    "totalExpenses": 17500.00,
    "netBalance": 77500.00,
    "categoryTotals": [
        { "category": "Salary",    "total": 75000.00 },
        { "category": "Freelance", "total": 20000.00 },
        { "category": "Rent",      "total": 12000.00 }
    ],
    "monthlyTrends": [
        { "month": "2024-03", "income": 95000.00, "expenses": 17500.00 }
    ],
    "recentActivity": [.....]
}
```

---

## Key Design Decisions

### 1. Email as principal
Spring Security's `getUsername()` returns email since login is email-based. Username is stored separately as a display name.

### 2. Soft delete
Records are never physically deleted. A `deleted` flag is set to `true` and all queries filter by `deleted = false`. This preserves audit history and is standard practice in financial systems.

### 3. BigDecimal for amounts
All monetary values use `BigDecimal` instead of `double` or `float` to avoid floating point rounding errors — critical for financial data accuracy.

### 4. JWT claims
Token carries only `email` (subject) and `role` (claim) — minimal and sufficient. Additional user data is fetched from DB when needed using the email from the token.

### 5. Flexible filtering with JPQL
A single `findWithFilters` JPQL query handles all filter combinations using `(:param IS NULL OR condition)` — avoids combinatorial explosion of repository methods.

### 6. Monthly trends computed in Java
Monthly trend grouping is done in the service layer using Java streams rather than database-specific functions like SQLite's `STRFTIME`. This keeps the repository layer database-agnostic and easier to migrate if the database changes.

### 7. PATCH for partial updates
`PATCH` is used for role and status updates (single field changes) while `PUT` is used for full record replacement — consistent with REST semantics.

---

## Error Handling

All errors return a consistent JSON structure:

```json
{
    "status": 404,
    "message": "Record not found with id: 99",
    "timestamp": "2024-03-15T10:30:00"
}
```

| Scenario | Status Code |
|---|---|
| Resource not found | `404` |
| Duplicate email or username | `409` |
| Invalid credentials | `401` |
| Insufficient permissions | `403` |
| Validation failure | `400` |
| Unexpected error | `500` |

---

## Assumptions Made

1. **Registration is public** — any user can register with any role. In a real system, admin would create and assign roles to users. This assumption is documented here as a deliberate tradeoff for simplicity.
2. **Single role per user** — each user has exactly one role. No multi-role support.
3. **No token refresh** — tokens expire after 24 hours and the user must log in again. Token refresh was not implemented to keep auth simple.
4. **SQLite for persistence** — chosen for simplicity and zero-config setup. All queries are written to be compatible with standard JPQL to minimize SQLite-specific coupling.
5. **Soft delete on records only** — users are hard deleted since there is no business requirement to retain deleted user data in this scope.

---

## Optional Enhancements Implemented

- ✅ JWT Authentication
- ✅ Pagination for record listing
- ✅ Search support (keyword search on category and notes)
- ✅ Soft delete on financial records
- ✅ Bulk record creation
- ✅ Swagger API documentation

## Planned Improvements

- Unit and integration tests
- Token refresh endpoint
- Rate limiting