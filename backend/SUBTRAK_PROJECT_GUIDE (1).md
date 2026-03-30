# SubTrak — Full Project Guide for Claude Opus 4.5

> Feed this file to Claude Opus 4.5 CLI when working on SubTrak.
> It tells Claude exactly what is done, what is missing, and what to build next.

---

## 1. What Has Already Been Built (Phase 1 — Backend Auth)

### Backend — Spring Boot 3 + PostgreSQL

The entire auth system is production-grade and complete. Here is a precise rundown:

**Entities (`com.subtrak.entity`)**
- `User.java` — JPA entity implementing `UserDetails`. Fields: id (UUID), name, email, passwordHash, locale, displayCurrency, salary, salaryCurrency, budgetLimitPercent, createdAt. BCrypt strength 12.
- `RefreshToken.java` — JPA entity. Fields: id (UUID), user (FK), tokenHash (SHA-256 of raw token), expiresAt, revoked, createdAt. Has `isValid()` helper.

**Security (`com.subtrak.security`)**
- `JwtUtil.java` — Generates + validates HMAC-SHA access tokens (15 min expiry). Reads secret and expiry from `application.properties`.
- `JwtAuthFilter.java` — `OncePerRequestFilter`. Extracts Bearer token from `Authorization` header, validates, sets `SecurityContext`.
- `RateLimiter.java` — Bucket4j token bucket per IP. 10 requests/min on auth routes. In-memory `ConcurrentHashMap`.

**Config (`com.subtrak.config`)**
- `SecurityConfig.java` — Stateless sessions, CSRF off, CORS (localhost:5173 only), `/api/auth/**` is public, everything else requires auth. Registers `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.

**Services (`com.subtrak.service`)**
- `AuthService.java` — `register`, `login`, `refresh`, `logout`. Refresh token rotation on every refresh. Old tokens revoked on re-login. Tokens stored as SHA-256 hashes (raw token never persisted).
- `UserDetailsServiceImpl.java` — Loads user by email for Spring Security.
- `TokenCleanupService.java` — `@Scheduled` cron at 3 AM daily to delete expired/revoked refresh tokens.

**Controller (`com.subtrak.controller`)**
- `AuthController.java` — POST `/api/auth/register`, `/login`, `/refresh`, `/logout`. Rate limiting applied on register and login. IP extracted from `X-Forwarded-For` header.

**Exception Handling (`com.subtrak.exception`)**
- `GlobalExceptionHandler.java` — Handles `MethodArgumentNotValidException` (400), `EmailAlreadyExistsException` (409), `InvalidTokenException` (401), `BadCredentialsException` (401), `ResourceNotFoundException` (404), generic fallback (500). Returns structured `ErrorResponse` records.

**Database**
- `schema.sql` — Manual DDL for `users` and `refresh_tokens` tables. Indexes on email, token_hash, user_id.
- `application.properties` — PostgreSQL datasource, JWT config, FX API key placeholder, CORS config.

**pom.xml dependencies confirmed:**
Spring Boot 3.2.4, Spring Security, Spring Data JPA, PostgreSQL driver, JJWT 0.12.5, Bucket4j 7.6.0, Lombok, Validation starter.

> ⚠️ **Known issue in pom.xml**: `<java.version>25</java.version>` — Java 25 is not released yet (current LTS is 21). Change this to `17` or `21` before running.

---

## 2. What Is NOT Done Yet (Missing Files)

### Missing Java files that are referenced but not uploaded:

| File | Package | Purpose |
|------|---------|---------|
| `RegisterRequest.java` | `com.subtrak.dto.request` | Validation DTO: name, email, password |
| `LoginRequest.java` | `com.subtrak.dto.request` | Validation DTO: email, password |
| `RefreshTokenRequest.java` | `com.subtrak.dto.request` | Holds refreshToken string |
| `AuthResponse.java` | `com.subtrak.dto.response` | accessToken, refreshToken, UserResponse |
| `UserResponse.java` | `com.subtrak.dto.response` | Public user fields (no password). Has `from(User)` factory |
| `UserRepository.java` | `com.subtrak.repository` | JPA repo: `findByEmail`, `existsByEmail` |
| `RefreshTokenRepository.java` | `com.subtrak.repository` | JPA repo: `findByTokenHash`, `revokeAllByUser`, `deleteExpiredAndRevoked` |
| `EmailAlreadyExistsException.java` | `com.subtrak.exception` | Extends RuntimeException |
| `InvalidTokenException.java` | `com.subtrak.exception` | Extends RuntimeException |
| `ResourceNotFoundException.java` | `com.subtrak.exception` | Extends RuntimeException |

### Missing Phase 2 features (not started):

- Category CRUD (name, color, icon per user)
- Subscription CRUD (name, amount, currency, billingCycle, nextRenewalDate, categoryId)
- Monthly cost summary endpoint
- Upcoming renewals endpoint (next 30 days)
- Budget alert logic (notify when subscriptions exceed `budgetLimitPercent` of salary)
- FX (currency conversion) service using ExchangeRate-API
- Frontend (React + Vite + Tailwind)

---

## 3. Connecting to PostgreSQL (Database Setup)

### Step 1 — Install PostgreSQL locally

**Windows:**
Download from https://www.postgresql.org/download/windows/ and install. Default port 5432.

**Mac:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Step 2 — Create the database

```bash
psql -U postgres
```

```sql
CREATE DATABASE subtrak;
\q
```

### Step 3 — Configure application.properties

Open `src/main/resources/application.properties` and set:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/subtrak
spring.datasource.username=postgres
spring.datasource.password=YOUR_ACTUAL_PASSWORD

app.jwt.secret=GENERATE_THIS_BELOW
app.jwt.access-token-expiry-ms=900000
app.jwt.refresh-token-expiry-ms=604800000
```

Generate a JWT secret (run in terminal):
```bash
openssl rand -hex 64
```

### Step 4 — Fix the Java version in pom.xml

Change line:
```xml
<java.version>25</java.version>
```
To:
```xml
<java.version>21</java.version>
```

### Step 5 — Run the backend

```bash
mvn spring-boot:run
```

Hibernate will auto-create the tables on first run (`ddl-auto=update`).
Alternatively, run `schema.sql` manually in psql first for explicit control.

---

## 4. What to Build Next — Phase 2 Backend

### 4a. Generate missing DTOs and exceptions

Ask Claude to create:

```
Create the following files for the SubTrak Spring Boot project (package com.subtrak):

1. dto/request/RegisterRequest.java — @NotBlank name, @Email email, @Size(min=8) password
2. dto/request/LoginRequest.java — @Email email, @NotBlank password
3. dto/request/RefreshTokenRequest.java — @NotBlank refreshToken (public field, no getters needed)
4. dto/response/UserResponse.java — record with id, name, email, locale, displayCurrency, salary, salaryCurrency, budgetLimitPercent. Static factory: from(User user)
5. dto/response/AuthResponse.java — @Builder: accessToken, refreshToken, UserResponse user
6. repository/UserRepository.java — extends JpaRepository<User, String>. Methods: findByEmail, existsByEmail
7. repository/RefreshTokenRepository.java — extends JpaRepository<RefreshToken, String>. Methods: findByTokenHash, @Modifying @Query revokeAllByUser, @Modifying @Query deleteExpiredAndRevoked
8. exception/EmailAlreadyExistsException.java
9. exception/InvalidTokenException.java
10. exception/ResourceNotFoundException.java

No comments, no unnecessary annotations.
```

### 4b. Phase 2 — Category + Subscription entities

```
Add to SubTrak:

Entity: Category
- id (UUID), user (ManyToOne User), name (String), color (String, hex), icon (String, emoji or name), createdAt
- Table: categories

Entity: Subscription  
- id (UUID), user (ManyToOne User), category (ManyToOne Category, nullable)
- name (String), amount (BigDecimal), currency (String)
- billingCycle (Enum: MONTHLY, YEARLY, WEEKLY, QUARTERLY)
- nextRenewalDate (LocalDate), active (boolean, default true)
- notes (String, nullable), createdAt, updatedAt

CRUD controllers:
- GET/POST/PUT/DELETE /api/categories (authenticated)
- GET/POST/PUT/DELETE /api/subscriptions (authenticated)
- GET /api/subscriptions/upcoming?days=30 — returns subs renewing within N days
- GET /api/dashboard/summary — returns total monthly cost, by-category breakdown, budget used %
```

---

## 5. Frontend — React + Vite + Tailwind

### Setup

```bash
npm create vite@latest subtrak-frontend -- --template react
cd subtrak-frontend
npm install
npm install axios react-router-dom @tanstack/react-query tailwindcss i18next react-i18next recharts
npx tailwindcss init -p
```

### Project structure to build

```
src/
├── api/           axios instance + auth interceptors
├── components/    Navbar, SubscriptionCard, CategoryBadge, BudgetBar
├── pages/         Login, Register, Dashboard, Subscriptions, Settings
├── hooks/         useAuth, useSubscriptions, useDashboard
├── i18n/          en.json, ur.json (Urdu support)
└── main.jsx
```

### Key prompt to give Claude for the frontend:

```
Build a React + Vite + Tailwind frontend for SubTrak (subscription tracker).

Backend runs at http://localhost:8080/api

Auth:
- Store accessToken in memory (not localStorage), refreshToken in httpOnly cookie OR localStorage for now
- Axios interceptor: attach Bearer token, refresh on 401 automatically

Pages needed:
1. /login — email + password form, calls POST /api/auth/login
2. /register — name + email + password form
3. /dashboard — shows monthly total, budget bar (salary-based), upcoming renewals this month, pie chart by category (use Recharts)
4. /subscriptions — list all, add/edit/delete modal, filter by category
5. /settings — change display currency, salary, locale

Use Tailwind for styling. Keep it clean and modern.
No TypeScript — plain JavaScript is fine.
```

---

## 6. FX Currency Service (Phase 2.5)

The `application.properties` already has:
```properties
app.fx.api-key=YOUR_EXCHANGERATE_API_KEY
app.fx.base-url=https://v6.exchangerate-api.com/v6
```

Get a free key at: https://app.exchangerate-api.com

Prompt to build it:
```
Add a CurrencyService to SubTrak that:
- Calls GET https://v6.exchangerate-api.com/v6/{apiKey}/latest/USD on startup and every hour (@Scheduled)
- Caches exchange rates in a ConcurrentHashMap<String, BigDecimal>
- Exposes convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) method
- Used by the dashboard summary endpoint to normalize all subscription amounts to the user's displayCurrency
```

---

## 7. Full Build Order (Recommended Sequence)

```
Phase 1 (DONE):
✅ Spring Boot project setup
✅ User + RefreshToken entities
✅ JWT auth (access + refresh token rotation)
✅ BCrypt password hashing
✅ Rate limiting (Bucket4j)
✅ CORS config
✅ Global exception handler
✅ Token cleanup scheduler
✅ PostgreSQL schema

Phase 1 Gaps (do first):
☐ Generate the 10 missing DTO/repo/exception files (Section 4a above)
☐ Fix pom.xml Java version (25 → 21)
☐ Fill in application.properties secrets
☐ Test all 4 auth endpoints with Postman/curl

Phase 2 (backend features):
☐ Category entity + CRUD
☐ Subscription entity + CRUD
☐ Dashboard summary endpoint
☐ Upcoming renewals endpoint
☐ Budget alert logic

Phase 2.5:
☐ CurrencyService (ExchangeRate-API, cached hourly)

Phase 3 (frontend):
☐ React + Vite + Tailwind setup
☐ Axios instance with auth interceptors
☐ Login + Register pages
☐ Dashboard with Recharts
☐ Subscriptions CRUD UI
☐ Settings page
☐ i18next (English + Urdu)
```

---

## 8. Quick curl Tests (Once Backend Is Running)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Abdullah","email":"a@test.com","password":"SecurePass123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"a@test.com","password":"SecurePass123"}'

# Refresh (paste refreshToken from login response)
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"PASTE_REFRESH_TOKEN_HERE"}'

# Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"PASTE_REFRESH_TOKEN_HERE"}'
```

---

## 9. Notes for Claude Opus 4.5 CLI

- The project package root is `com.subtrak`
- Prefer single-file implementations where possible
- No comments in code unless asked
- Use Lombok (`@Builder`, `@RequiredArgsConstructor`, `@Getter/@Setter`) consistently
- All controller routes are under `/api/`
- The User entity IS the UserDetails — no wrapper class needed
- RefreshTokenRepository needs a custom `@Modifying @Query` for `revokeAllByUser` and `deleteExpiredAndRevoked`
- Java version to use: 21 (not 25 as in pom.xml — that is a typo)
- When adding new entities, also update `schema.sql` with the corresponding `CREATE TABLE IF NOT EXISTS` block
