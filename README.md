# SubTrak - Subscription Tracker

A full-stack subscription management application with a modern celestial-themed UI.

## 🚀 Features

- **Dashboard**: Overview of monthly spending, active subscriptions, and upcoming renewals
- **Subscription Management**: Add, edit, delete, and track all your subscriptions
- **Categories**: Organize subscriptions by custom categories
- **Multi-Currency Support**: Track subscriptions in different currencies
- **User Authentication**: Secure JWT-based authentication with refresh tokens

## 📁 Project Structure

```
SubTrak/
├── backend/           # Spring Boot REST API
│   ├── src/
│   ├── pom.xml
│   └── schema.sql
├── frontend/          # Static HTML/CSS/JS UI
│   ├── index.html     # Login page
│   ├── register.html  # Registration page
│   ├── dashboard.html # Main dashboard
│   ├── subscriptions.html
│   ├── categories.html
│   └── settings.html
└── README.md
```

## 🛠️ Tech Stack

### Backend
- **Java 17** with **Spring Boot 3**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with MySQL
- **Maven** for dependency management

### Frontend
- **HTML5** with **Tailwind CSS**
- **Vanilla JavaScript**
- Modern glassmorphism UI design

## 🏃 Running Locally

### Prerequisites
- Java 17+
- Maven
- MySQL 8.0+

### Backend Setup

1. Create MySQL database:
```sql
CREATE DATABASE subtrak;
```

2. Run the schema:
```bash
mysql -u root -p subtrak < backend/schema.sql
```

3. Configure `application.properties` with your database credentials

4. Run the backend:
```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Frontend Setup

1. Open `frontend/index.html` in a browser, or serve with any static server:
```bash
cd frontend
npx serve .
```

## 📝 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Refresh token |
| GET | `/api/subscriptions` | List subscriptions |
| POST | `/api/subscriptions` | Create subscription |
| PUT | `/api/subscriptions/{id}` | Update subscription |
| DELETE | `/api/subscriptions/{id}` | Delete subscription |
| GET | `/api/categories` | List categories |
| POST | `/api/categories` | Create category |

## 📄 License

MIT License
