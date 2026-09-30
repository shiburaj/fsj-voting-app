-- Voting App Database Schema
CREATE DATABASE IF NOT EXISTS voting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE voting_db;

-- Drop existing tables if re-creating
DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS candidates;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS users;

-- 1. Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Sessions table
CREATE TABLE sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    user_id BIGINT NOT NULL,
    voting_enabled BOOLEAN DEFAULT FALSE,
    code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Candidates table
CREATE TABLE candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    CONSTRAINT fk_candidates_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Votes table
CREATE TABLE votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    voter_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_votes_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_voter FOREIGN KEY (voter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_voter_session UNIQUE (session_id, voter_id)
) ENGINE=InnoDB;

-- Initial Demo Data
-- Note: passwords shown here are stored; we will support standard hashing
INSERT INTO users (name, email, password) VALUES 
('Admin Host', 'admin@example.com', 'admin123'),
('Alice Voter', 'alice@example.com', 'alice123'),
('Bob Voter', 'bob@example.com', 'bob123');

-- Demo Session
INSERT INTO sessions (name, user_id, voting_enabled, code) VALUES
('Tech Lead Election 2026', 1, TRUE, 'VOTE01');

-- Demo Candidates
INSERT INTO candidates (session_id, name) VALUES
(1, 'Sarah Connor'),
(1, 'John Doe'),
(1, 'Jane Smith');

-- Demo Vote (Alice voted for Sarah Connor)
INSERT INTO votes (session_id, candidate_id, voter_id) VALUES
(1, 1, 2);
