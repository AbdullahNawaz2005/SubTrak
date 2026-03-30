# SubTrak — Subscription Tracker

A secure, full-stack subscription tracking app built with Spring Boot + React.

---

## Tech Stack

| Layer      | Technology                                      |
|------------|-------------------------------------------------|
| Backend    | Spring Boot 3, Spring Security, JPA, MySQL      |
| Auth       | JWT (access + refresh token rotation)           |
| Frontend   | React + Vite, Tailwind, i18next, Recharts       |
| Currency   | ExchangeRate-API (cached hourly)                |

---

## Phase 1 Setup (Backend Auth)

### Prerequisites
- Java 21+
- MySQL running locally
- Maven 3.9+

### 1. Create the database
```sql
CREATE DATABASE subtrak;
```

### 2. Configure environment variables

Set the following environment variables before running the application:

```bash
# Linux/Mac
export DB_PASSWORD=your_database_password
export JWT_SECRET=$(openssl rand -hex 64)
export FX_API_KEY=your_exchangerate_api_key

# Windows PowerShell
$env:DB_PASSWORD="your_database_password"
$env:JWT_SECRET="your_64_char_random_string"
$env:FX_API_KEY="your_exchangerate_api_key"
```

Or copy `application-local.properties.example` to `application-local.properties` 
and fill in the values, then run with `--spring.profiles.active=local`.

### 3. Run the backend
```bash
mvn spring-boot:run
```

---

## Currency Conversion (ExchangeRate-API)

SubTrak uses ExchangeRate-API for real-time currency conversion.

### Getting a Free API Key

1. Go to https://app.exchangerate-api.com
2. Sign up for a free account (1,500 requests/month free)
3. Copy your API key from the dashboard
4. Set the `FX_API_KEY` environment variable

### Without an API Key

If you don't set `FX_API_KEY`, the app will:
- Use "placeholder" as the API key (which will fail)
- Log a warning on startup and every hour
- Continue to work, but currency conversion will use USD only
- Dashboard totals will show in USD regardless of user preference

### Currency API Endpoints

| Method | Endpoint                           | Description                    |
|--------|------------------------------------|--------------------------------|
| GET    | /api/currency/rates                | Get all supported rates        |
| GET    | /api/currency/convert?amount=100&from=USD&to=PKR | Convert amount |
| GET    | /api/currency/supported            | List all currency codes        |

---

## API Endpoints (Phase 1)

### Auth
| Method | Endpoint             | Description              | Auth required |
|--------|----------------------|--------------------------|---------------|
| POST   | /api/auth/register   | Create account           | No            |
| POST   | /api/auth/login      | Login, get tokens        | No            |
| POST   | /api/auth/refresh    | Rotate refresh token     | No            |
| POST   | /api/auth/logout     | Revoke refresh token     | No            |

### Example: Register
```json
POST /api/auth/register
{
  "name": "Abdullah",
  "email": "abdullah@example.com",
  "password": "SecurePass123"
}
```

### Example: Login response
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "uuid-string",
  "user": {
    "id": "...",
    "name": "Abdullah",
    "email": "abdullah@example.com",
    "locale": "en",
    "displayCurrency": "USD",
    "salary": 0,
    "salaryCurrency": "USD",
    "budgetLimitPercent": 20
  }
}
```

---

## Security Features
- BCrypt password hashing (strength 12)
- JWT access tokens (15 min expiry)
- Refresh token rotation — new token on every refresh, old one revoked
- Refresh tokens stored as SHA-256 hashes in DB (raw token never stored)
- Rate limiting: 10 requests/min per IP on auth routes
- CORS whitelist: localhost:5173 only
- No sensitive data in logs
- Secrets stored in environment variables (not in code)

---

## Project Structure
```
src/main/java/com/subtrak/
├── config/          SecurityConfig.java, AppConfig.java
├── controller/      AuthController, CategoryController, SubscriptionController, CurrencyController
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, CategoryRequest, SubscriptionRequest
│   └── response/    AuthResponse, UserResponse, CategoryResponse, SubscriptionResponse, DashboardResponse
├── entity/          User, RefreshToken, Category, Subscription, BillingCycle
├── exception/       GlobalExceptionHandler + custom exceptions
├── repository/      UserRepository, RefreshTokenRepository, CategoryRepository, SubscriptionRepository
├── security/        JwtUtil, JwtAuthFilter, RateLimiter
├── service/         AuthService, CategoryService, SubscriptionService, CurrencyService, TokenCleanupService
├── validation/      ValidPassword, NotPastDate + validators
└── SubTrakApplication.java
```

---

## Coming Next (Phase 3)
- User profile update
- Notification preferences
- Email reminders for renewals
- React frontend
