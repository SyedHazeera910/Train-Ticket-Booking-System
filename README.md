# Railway Super-App 🚆

## Overview

The **Railway Super-App** is a robust, full-stack Train Ticket Booking and Management System built to handle end-to-end railway travel processes. Initially designed with a microservices-based architecture, this platform has been successfully adapted into a practical, modular Spring Boot monolith-first architecture for seamless local development while maintaining enterprise-level functionality.

The application allows users to search for trains, book tickets, check PNR status, view live train tracking, order food for their journey, manage an integrated walled for payments, and interact with a rule-based AI chatbot for quick automated assistance.

---

## 🏗 Supported Modules & Features

- **Authentication & Identity**: User registration, login, JWT-based stateless secure authentication, role management (Passenger, Admin).
- **Core Ticketing System**: Search trains by route and date, book seats (with class selection), passenger forms, seat locking to handle concurrent bookings, and cancel tickets.
- **Payment & Wallet**: Integrated user wallet for instant seamless payments. Features balance checking and top-up functionality.
- **Live Train Tracking**: Mock WebSocket-based real-time train tracking and map display.
- **Food & Catering**: Allow users to view menus by train, add items to cart, and place food orders connected to their actual booking (PNR).
- **Support & Helpdesk**: Create, manage, and view customer support tickets.
- **AI Chatbot**: A natural language rule-based AI chatbot providing automated assistance for tasks like checking PNR status, tracking trains, viewing bookings, and wallet balance.
- **Swagger Documentation**: Automated generated, interactive API Reference available out of the box using Springdoc OpenAPI.

---

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.3 (Java 17)
- **Database**: MySQL 8 (Hibernate ORM for mapping and auto-DDL)
- **Security**: Spring Security & JWT (jjwt)
- **Real-time**: Spring Boot WebSockets
- **API Docs**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven

### Frontend
- **Framework**: React.js (Vite)
- **Styling**: Tailwind CSS & Vanilla CSS (Dark mode with railway-themed palette)
- **State Management**: Zustand / Context API
- **HTTP Client**: Axios
- **Routing**: React Router

---

## 📂 Project Structure

```
Train-Ticket-Booking-System/
├── railway-backend/          ← Spring Boot REST API
│   ├── config/               # Security, CORS, JWT, Swagger Configurations
│   ├── identity/             # User Auth Models & Controllers
│   ├── ticketing/            # Booking, Station, Train Logic
│   ├── payments/             # Wallet, Checkout Logic
│   ├── tracking/             # WebSocket tracking simulation
│   ├── food/                 # Food system logic
│   ├── chatbot/              # Rule-based AI chat implementation
│   └── support/              # Ticketing & Helpdesk Support logic
│
└── railway-frontend/         ← React Single Page Application (SPA)
    ├── src/
    │   ├── components/       # Shared UI components (Navbar, Chat Widget, Cards)
    │   ├── pages/            # Application routes (Booking, Dashboard, Wallet, etc.)
    │   ├── api/              # Axios instance and API call abstractions
    │   └── store/            # State management 
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Node.js 18+** & modern npm
- **MySQL 8** running on local machine (port 3306) or Docker.

### 1. Database Setup
Create a new MySQL database corresponding to the `application.yml` file in the backend (commonly `railway_db`).
```sql
CREATE DATABASE railway_db;
```
The application relies on Hibernate DDL Auto-create, meaning tables will automatically be generated in the database upon running the backend server.

### 2. Running Backend (Spring Boot)
1. Navigate to the backend directory:
   ```bash
   cd railway-backend
   ```
2. Build and run via Maven Wrapper or standard Maven:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. The server will begin running on `http://localhost:8080`.
4. *Swagger UI is available at:* `http://localhost:8080/swagger-ui.html`

### 3. Running Frontend (React)
1. Navigate to the frontend directory:
   ```bash
   cd railway-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. The client will be available at `http://localhost:5173`. 

---

## 📡 API Reference Overview

The API endpoints are grouped by module. You can interact with these directly via the integrated **Swagger UI**.

- **Auth**: `POST /api/auth/register`, `POST /api/auth/login`
- **Trains**: `GET /api/trains/search`
- **Bookings**: `POST /api/bookings`, `GET /api/bookings/pnr/{pnr}`, `POST /api/bookings/{id}/cancel`
- **Wallet**: `GET /api/wallet/balance`, `POST /api/wallet/topup`
- **Tracking**: `GET /api/tracking/{trainId}`, plus WebSocket connection on `/ws`

*(For full extensive request/response definitions, simply visit `/swagger-ui.html` while the server is running)*
