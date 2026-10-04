# Full Stack Voting App

A lightweight, beginner-friendly online voting application built with **Java Servlets (Jakarta EE)**, **MySQL**, and **React (Vite + Tailwind CSS)**.

---

## Prerequisites

Before running the application, make sure you have the following installed:

- **Java JDK** (version 11, 17, or 21)
- **Apache Maven** (3.8+)
- **Apache Tomcat 10+** (Tomcat 10 or 10.1 supporting `jakarta.servlet`)
- **MySQL Database Server** (via Laragon, XAMPP, or standalone MySQL)
- **Node.js** (version 18+ or 20+) and **npm**

---

## Steps to Run the App

### Step 1: Set Up MySQL Database

1. Start your MySQL server (e.g., in Laragon or XAMPP, click **Start All**).
2. Open your MySQL client (HeidiSQL, phpMyAdmin, or terminal).
3. Import or execute the schema script located at:
   ```bash
   backend/schema.sql
   ```
   *(This creates the `voting_db` database, all 4 tables, and seeds initial demo data).*
4. If your MySQL credentials are not `root` with an empty password, you can set environment variables:
   - `DB_URL` (default: `jdbc:mysql://localhost:3306/voting_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
   - `DB_USER` (default: `root`)
   - `DB_PASSWORD` (default: `""`)

---

### Step 2: Build and Deploy the Backend

1. Open a terminal and navigate to the `backend` folder:
   ```bash
   cd backend
   ```
2. Build the project using Maven:
   ```bash
   mvn clean package
   ```
   This generates `ServletApp.war` inside the `backend/target/` directory.
3. Deploy to Tomcat:
   - Copy `backend/target/ServletApp.war` into your Tomcat `webapps/` folder, **or**
   - Configure Tomcat in your IDE (VSCODE) to deploy `ServletApp`.
4. Start Tomcat. The API will be available at:
   ```
   http://localhost:8080/ServletApp/api
   ```

---

### Step 3: Run the Frontend

1. Open a new terminal and navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and visit:
   ```
   http://localhost:5173
   ```

---

## Demo Accounts & Test Credentials

The database comes pre-seeded with sample users and a demo voting session:

| Name | Email | Password | Role |
| :--- | :--- | :--- | :--- |
| **Admin Host** | `admin@example.com` | `admin123` | Host (Can create sessions & view results) |
| **Alice Voter** | `alice@example.com` | `alice123` | Voter |
| **Bob Voter** | `bob@example.com` | `bob123` | Voter |

- **Demo Session Code:** `VOTE01` (Tech Lead Election)

---

## Application Flow

1. **Authentication:** User registers or logs in. Authentication is managed securely via standard Java **`HttpSession`** (tracked with `JSESSIONID` cookies).
2. **Host Dashboard:** A logged-in host can create a new voting session, add candidates, and receive a unique 6-character session code.
3. **Voting:** Any voter logs in, enters the 6-character session code, views the candidates, and casts their single ballot.
4. **Session Controls:** The host can toggle voting status (Active / Closed) at any time.
5. **Live Results:** Live results with candidate percentages and leader highlights are polled and updated automatically every 4 seconds.

---

## Architecture Overview

### Backend
- **Jakarta Servlets:** Handlers for authentication, voting sessions, votes, and live results.
- **Java `HttpSession`:** Server-side session management with CORS credentials enabled.
- **Direct 1-to-1 Models:** 4 database tables match exactly 4 Java model classes:
  - `User.java` $\leftrightarrow$ `users` table
  - `VotingSession.java` $\leftrightarrow$ `sessions` table
  - `Candidate.java` $\leftrightarrow$ `candidates` table
  - `Vote.java` $\leftrightarrow$ `votes` table
- **DAO Pattern:** Data Access Objects (`UserDAO`, `SessionDAO`, `CandidateDAO`, `VoteDAO`) handle clean JDBC SQL queries.
- **Security:** Passwords securely hashed with BCrypt.

### Frontend
- **React (Vite) + Tailwind CSS**: Fast, modern UI.
- **Context API (`AuthContext`)**: Synchronizes with server-side `HttpSession`.
- **Fetch Client (`api.js`)**: Configured with `credentials: 'include'` for automatic cookie transmission.