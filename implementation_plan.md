# Railway Super-App — Full-Stack Implementation Plan

## Overview

Build a **Railway Super-App** with a React.js frontend, Spring Boot backend (adapted to MySQL), and MySQL database. The original spec uses PostgreSQL, Redis, Kafka, and microservices — we will **simplify into a practical monolith-first approach** for MySQL compatibility and ease of local development, while keeping the same feature set.

---

## User Review Required

> [!IMPORTANT]
> The original spec calls for **PostgreSQL + Redis + Kafka + Spring Cloud microservices**. Since you requested **MySQL** as the database, we will:
> - Replace PostgreSQL with **MySQL 8**
> - Keep Redis for session caching (optional — can be skipped initially)
> - Replace Kafka **event bus with Spring Application Events** (in-process, no separate broker needed to get started)
> - Build a **single Spring Boot monolith** (with packages mirroring each microservice) rather than 6+ separate services — this is far more practical for development/demo and can be split later

> [!WARNING]
> You will need the following installed locally:
> - **Java 17+** and **Maven 3.9+**
> - **Node.js 18+** and **npm**
> - **MySQL 8** running locally (or via Docker)

---

## Proposed Changes

### Project Structure

```
c:\Users\syedh\OneDrive\Documents\project\
├── railway-backend/          ← Spring Boot Maven project
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/railway/
│       │   │   ├── RailwayApplication.java
│       │   │   ├── config/         (Security, CORS, JWT)
│       │   │   ├── identity/       (User, Auth)
│       │   │   ├── ticketing/      (Train, Station, Booking)
│       │   │   ├── payments/       (Wallet, Transaction)
│       │   │   ├── tracking/       (TrainPosition - WebSocket)
│       │   │   ├── food/           (FoodMenu, FoodOrder)
│       │   │   └── support/        (Ticket, Chat)
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
└── railway-frontend/         ← React (Vite) project
    ├── package.json
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── pages/
        │   ├── LoginPage.jsx
        │   ├── RegisterPage.jsx
        │   ├── DashboardPage.jsx
        │   ├── SearchTrainsPage.jsx
        │   ├── BookingPage.jsx
        │   ├── MyBookingsPage.jsx
        │   ├── PnrStatusPage.jsx
        │   ├── WalletPage.jsx
        │   ├── TrackingPage.jsx
        │   ├── FoodOrderPage.jsx
        │   └── SupportPage.jsx
        ├── components/       (Navbar, ProtectedRoute, etc.)
        ├── api/              (axios service files)
        └── store/            (Zustand or Context for auth state)
```

---

### Backend — Spring Boot Monolith

#### [NEW] `railway-backend/pom.xml`
- Spring Boot 3.3, Java 17
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `mysql-connector-j`, `jjwt` (JWT), `lombok`, `spring-boot-starter-websocket`, `spring-boot-starter-validation`

#### [NEW] `railway-backend/src/main/resources/application.yml`
- MySQL datasource, Hibernate DDL auto-create, JWT secret, CORS config

#### Identity Module
- `User.java` — entity with roles (PASSENGER, ADMIN)
- `AuthController.java` — `/api/auth/register`, `/api/auth/login`
- `JwtUtil.java` + `JwtAuthFilter.java` — token generation/validation
- `SecurityConfig.java` — BCrypt, stateless JWT

#### Ticketing Module
- `Train.java`, `Station.java`, `Booking.java` entities
- `TrainController.java` — `GET /api/trains/search?from=&to=&date=`
- `BookingController.java` — `POST /api/bookings`, `GET /api/bookings/pnr/{pnr}`, `POST /api/bookings/{id}/cancel`
- `BookingService.java` — seat availability check with `synchronized` block (replaces Redis lock for MySQL)

#### Payments Module
- `Wallet.java`, `Transaction.java` entities
- `WalletController.java` — `GET /api/wallet/balance`, `POST /api/wallet/topup`
- `CheckoutController.java` — `POST /api/checkout` (deducts wallet, confirms booking)

#### Tracking Module
- `TrainPosition.java` entity
- `TrackingController.java` — `GET /api/tracking/{trainId}`
- `WebSocket` endpoint for live position updates
- `PositionSimulator.java` — scheduled task updating mock positions every 5s

#### Food Module
- `FoodMenu.java`, `FoodOrder.java` entities
- `FoodController.java` — `GET /api/food/menu/{trainId}`, `POST /api/food/order`

#### Support Module
- `SupportTicket.java` entity
- `SupportController.java` — `POST /api/support/tickets`, `GET /api/support/tickets`

---

### Frontend — React (Vite)

#### Pages & Features
| Page | Features |
|------|----------|
| **Login / Register** | JWT auth, form validation |
| **Dashboard** | User summary cards, quick links |
| **Search Trains** | Search by from/to/date, train list with seat count |
| **Booking** | Seat class selection, passenger forms, wallet payment |
| **My Bookings** | List of bookings with status badges & cancel action |
| **PNR Status** | Search by PNR number |
| **Wallet** | Balance display, top-up modal, transaction history |
| **Live Tracking** | Real-time train map with WebSocket updates |
| **Food Order** | Menu by train, add to cart, place order |
| **Support** | Create/view support tickets |

#### Design System
- Dark mode with railway-themed palette (deep navy, amber gold, teal accents)
- Google Font: **Inter**
- Glass morphism cards, smooth route transitions
- Responsive for mobile and desktop

---

## MySQL Schema (auto-created by Hibernate)

Key tables:
- `users` — id, name, email, password_hash, role
- `trains` — id, name, number, from_station, to_station, departure, arrival, total_seats, available_seats
- `stations` — id, name, code, city
- `bookings` — id, user_id, train_id, pnr, status, fare, travel_class, seat_numbers, created_at
- `wallets` — id, user_id, balance
- `transactions` — id, wallet_id, amount, type, description, created_at
- `train_positions` — id, train_id, latitude, longitude, speed, updated_at
- `food_orders` — id, user_id, booking_id, items_json, total, status
- `support_tickets` — id, user_id, subject, description, status, created_at

---

## Verification Plan

### Automated
- Spring Boot will run on `http://localhost:8080`
- React dev server on `http://localhost:5173`
- `mvn spring-boot:run` should start without errors
- `npm run dev` should start React app

### Manual (Browser)
1. Register a new user → Login → get JWT
2. Search trains → book → confirm payment from wallet
3. Check PNR status
4. Check live tracking (WebSocket mock)
5. Order food for a booking
6. Create a support ticket
