# Implementation Plan: Full Stack Voting App

This document outlines the step-by-step, phase-wise implementation plan for the **Full Stack Voting App** (Java Servlet + JDBC + MySQL backend & ReactJS frontend with shadcn/ui). Each task includes a checkbox `[ ]` or `[x]` to track progress.

---

## 🏗 System Architecture Overview

```
+-------------------------------------------------------+
|                 ReactJS Frontend                      |
| (Vite + React 19 + shadcn/ui + Tailwind CSS + Fetch) |
+---------------------------+---------------------------+
                            | HTTP / JSON (CORS enabled)
                            v
+-------------------------------------------------------+
|                  Java Backend                         |
|     (Jakarta Servlet 5.0 + JDBC + Gson + Tomcat)      |
|  - Filters: CorsFilter                                |
|  - Servlets: Auth, Session, Candidate, Vote, Result   |
|  - DAO Layer: UserDAO, SessionDAO, CandidateDAO, etc. |
|  - Models / DTOs                                      |
+---------------------------+---------------------------+
                            | JDBC
                            v
+-------------------------------------------------------+
|                    MySQL Database                     |
|  - users, sessions, candidates, votes                 |
+-------------------------------------------------------+
```

---

## 📋 Phase 1: Project Setup & Database Configuration

- [x] **1.1 Database Schema Definition & Setup**
  - [x] Create database `voting_db` in MySQL.
  - [x] Write `schema.sql` script with the following tables:
    - [x] `users` (id BIGINT AUTO_INCREMENT PK, name VARCHAR(100), email VARCHAR(150) UNIQUE, password VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP).
    - [x] `sessions` (id BIGINT AUTO_INCREMENT PK, name VARCHAR(150), user_id BIGINT FK -> users(id) ON DELETE CASCADE, voting_enabled BOOLEAN DEFAULT FALSE, code VARCHAR(10) UNIQUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP).
    - [x] `candidates` (id BIGINT AUTO_INCREMENT PK, session_id BIGINT FK -> sessions(id) ON DELETE CASCADE, name VARCHAR(150)).
    - [x] `votes` (id BIGINT AUTO_INCREMENT PK, session_id BIGINT FK -> sessions(id) ON DELETE CASCADE, candidate_id BIGINT FK -> candidates(id) ON DELETE CASCADE, voter_id BIGINT FK -> users(id) ON DELETE CASCADE, UNIQUE KEY unique_voter_session (session_id, voter_id), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP).
  - [x] Add initial seed data script for development testing.

- [x] **1.2 Backend Dependencies & Configuration (`backend/pom.xml`)**
  - [x] Add `mysql-connector-j` dependency for MySQL JDBC driver.
  - [x] Add Google `gson` dependency for JSON serialization and deserialization.
  - [x] Add BCrypt or password hashing helper dependency (or lightweight SHA-256 utility for beginner friendliness).
  - [x] Verify Maven build and packaging (`mvn clean package`).

- [x] **1.3 Database Connection Utility**
  - [x] Create `com.example.util.DBUtil` class to manage JDBC connections.
  - [x] Read connection parameters (URL, user, password) with fallback defaults (e.g. `jdbc:mysql://localhost:3306/voting_db`).
  - [x] Add connection test helper method to verify database connectivity.

- [x] **1.4 CORS & Request Filters**
  - [x] Create `com.example.filter.CorsFilter` to handle cross-origin requests from React (`http://localhost:5173`).
  - [x] Configure allowed headers (`Content-Type`, `Authorization`, `Accept`), methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`), and handle preflight `OPTIONS` requests with `200 OK`.

---

## 📦 Phase 2: Backend Models & Data Access Objects (DAO)

- [x] **2.1 Entity Models (POJOs)**
  - [x] Create `com.example.model.User` (`id`, `name`, `email`, `password`, `createdAt`).
  - [x] Create `com.example.model.VotingSession` (`id`, `name`, `userId`, `votingEnabled`, `code`, `createdAt`).
  - [x] Create `com.example.model.Candidate` (`id`, `sessionId`, `name`).
  - [x] Create `com.example.model.Vote` (`id`, `sessionId`, `candidateId`, `voterId`, `createdAt`).
  - [x] Create DTOs / Response classes:
    - [x] `ApiResponse<T>` (success, message, data).
    - [x] `CandidateResultDTO` (candidateId, candidateName, voteCount, percentage).
    - [x] `SessionResultDTO` (sessionId, sessionName, votingEnabled, totalVotes, List<CandidateResultDTO>).

- [x] **2.2 Data Access Objects (DAO Layer)**
  - [x] Implement `com.example.dao.UserDAO`:
    - [x] `register(User user)`
    - [x] `findByEmail(String email)`
    - [x] `findById(Long id)`
  - [x] Implement `com.example.dao.SessionDAO`:
    - [x] `createSession(VotingSession session)`
    - [x] `getSessionByCode(String code)`
    - [x] `getSessionById(Long id)`
    - [x] `getSessionsByUserId(Long userId)`
    - [x] `updateVotingStatus(Long sessionId, boolean enabled)`
    - [x] `generateUniqueCode()` helper (e.g. 6-character alphanumeric code).
  - [x] Implement `com.example.dao.CandidateDAO`:
    - [x] `addCandidates(Long sessionId, List<String> candidateNames)`
    - [x] `getCandidatesBySessionId(Long sessionId)`
  - [x] Implement `com.example.dao.VoteDAO`:
    - [x] `hasUserVoted(Long sessionId, Long voterId)`
    - [x] `castVote(Vote vote)` (guarded with unique check).
    - [x] `getResultsBySessionId(Long sessionId)` (SQL query grouping votes by candidate with vote count).

---

## 🌐 Phase 3: Backend REST API Servlets

- [x] **3.1 Servlet Helper Utilities**
  - [x] Create `com.example.util.JsonUtil` for parsing request JSON body and writing JSON responses via Gson.
  - [x] Create basic session / authentication helper (simple token or user ID header / session-based validation).

- [x] **3.2 Authentication Endpoints**
  - [x] Create `com.example.servlet.AuthServlet` mapping:
    - [x] `POST /api/auth/register` - Validate input, check duplicate email, hash password, insert user.
    - [x] `POST /api/auth/login` - Validate credentials, return user info (id, name, email).
    - [x] `GET /api/auth/me` - Return current logged-in user profile.

- [x] **3.3 Voting Session Endpoints**
  - [x] Create `com.example.servlet.SessionServlet` mapping:
    - [x] `GET /api/sessions` - Fetch list of sessions created by the logged-in user.
    - [x] `POST /api/sessions` - Create new session with title, generate unique code, insert candidates.
    - [x] `GET /api/sessions/by-code?code=XYZ` - Retrieve session info and candidates for voters.
    - [x] `POST /api/sessions/status` - Enable or disable voting for a session (host only).

- [x] **3.4 Vote Casting & Status Endpoints**
  - [x] Create `com.example.servlet.VoteServlet` mapping:
    - [x] `POST /api/votes` - Submit vote payload `{ sessionId, candidateId }`.
      - [x] Validate session exists and `voting_enabled == true`.
      - [x] Validate candidate belongs to session.
      - [x] Validate voter has not already voted in this session.
      - [x] Record vote and return success response.
    - [x] `GET /api/votes/status?sessionId={id}` - Check if voter already cast their vote.

- [x] **3.5 Results & Polling Endpoint**
  - [x] Create `com.example.servlet.ResultServlet` mapping:
    - [x] `GET /api/results?sessionId={id}` - Return aggregated results (candidates, vote counts, percentages, session status).

---

## 🎨 Phase 4: Frontend Foundations & UI Setup

- [x] **4.1 Styling & UI Library Setup**
  - [x] Install and configure Tailwind CSS in `frontend/`.
  - [x] Setup shadcn/ui inspired components (Button, Card, Modal, Input, Badge).
  - [x] Install `lucide-react` for clean icons.

- [x] **4.2 API Client & Utilities**
  - [x] Create `frontend/src/services/api.js` wrapper around `fetch`:
    - [x] Configure `BASE_URL` (`http://localhost:8080/ServletApp/api`).
    - [x] Centralize request error handling and JSON parsing.
    - [x] Include user ID / auth header in outgoing requests.

- [x] **4.3 Authentication Context / State Management**
  - [x] Create `frontend/src/context/AuthContext.jsx`:
    - [x] Persist logged-in user in `localStorage`.
    - [x] Provide `login`, `register`, and `logout` actions.
    - [x] Guard routes / views when user is not authenticated.

- [x] **4.4 Routing / Navigation Structure**
  - [x] Single-page tab-based responsive navigation:
    - [x] Cast Vote (`vote`)
    - [x] Host Dashboard (`host`)
    - [x] Live Results (`results`)
    - [x] Auth Modal (`login` / `register`)

---

## 💻 Phase 5: Frontend Feature Implementation

- [x] **5.1 Navbar & Layout**
  - [x] Responsive navigation bar with app logo, user welcome name, and Logout button.
  - [x] Navigation tabs: "Host Sessions" vs "Cast Vote".

- [x] **5.2 Authentication Pages**
  - [x] Login card with email and password inputs, validation, and error alerts.
  - [x] Register card with name, email, password inputs, and switch to login.

- [x] **5.3 Session Creator / Host Dashboard**
  - [x] "Create New Session" modal/dialog:
    - [x] Session title input.
    - [x] Dynamic candidate name inputs (add/remove candidate rows).
    - [x] Submit button to create session & generate unique 6-character code.
  - [x] "My Sessions" list card:
    - [x] Display session name, unique join code (with copy button), created date.
    - [x] Status badge: `Active (Voting Open)` vs `Closed (Voting Disabled)`.
    - [x] Toggle switch or button to Enable/Disable voting in real-time.
    - [x] "View Live Results" button linking to results dashboard.

- [x] **5.4 Voter Flow ("Cast Vote")**
  - [x] Step 1: Session code input form with validation.
  - [x] Step 2: Fetch and display session details & candidate options in selectable cards.
  - [x] Step 3: Handle closed session warning if `voting_enabled == false`.
  - [x] Step 4: Handle "Already Voted" state if the voter has previously voted.
  - [x] Step 5: Submit vote with confirmation modal and success message.

- [x] **5.5 Live Results Screen with 4-Second Polling**
  - [x] Display session title, host status, and live indicator badge.
  - [x] Custom polling hook `usePolling` polling `GET /api/results?sessionId={id}` every 4 seconds.
  - [x] Render candidates with vote count, progress bar percentage, and leader highlight.
  - [x] Total votes counter and last updated timestamp indicator.
  - [x] Host controls embedded on the results page (quick button to stop/restart voting).

---

## 🔒 Phase 6: Edge Cases, Security & Validation

- [x] **6.1 Data Integrity & Concurrency**
  - [x] Verify database UNIQUE constraint `(session_id, voter_id)` blocks concurrent duplicate votes.
  - [x] Handle SQL constraint violation gracefully and return HTTP 409 Conflict / friendly JSON message.
  - [x] Verify voting cannot occur if `voting_enabled == false` (return HTTP 403 Forbidden).

- [x] **6.2 Input Sanitization & Validation**
  - [x] Validate minimum 2 candidates per voting session.
  - [x] Validate non-empty candidate names and email format.
  - [x] Sanitize strings and hash passwords with BCrypt.

- [x] **6.3 Error Handling & Fallbacks**
  - [x] Add consistent JSON error payloads `{ "success": false, "message": "..." }`.
  - [x] Handle network connection failure or Tomcat downtime in React UI with user-friendly alerts.

---

## 🧪 Phase 7: Verification & Testing

- [x] **7.1 Backend API Verification**
  - [x] MySQL database tables created and seeded (`voting_db`).
  - [x] Backend compilation and WAR packaging verified with Maven (`ServletApp.war`).
- [ ] **7.2 Frontend End-to-End User Flow Test**
  - [ ] Start frontend dev server (`npm run dev`) and test with backend on Tomcat.
  - [ ] Register new user / login as demo user (`admin@example.com` / `admin123`).
  - [ ] Create session -> Cast vote -> Verify 4-second polling live update -> Close session.

---

## 🚀 Phase 8: Deployment & Handover

- [x] Build WAR package with `mvn clean package`.
- [ ] Deploy WAR to Apache Tomcat (`webapps/`).
- [x] Build frontend production bundle with `npm run build`.
- [x] Document instructions and database setup.
