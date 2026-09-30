package com.example.model;

import java.sql.Timestamp;

public class Vote {
    private Long id;
    private Long sessionId;
    private Long candidateId;
    private Long voterId;
    private Timestamp createdAt;

    public Vote() {}

    public Vote(Long id, Long sessionId, Long candidateId, Long voterId, Timestamp createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.candidateId = candidateId;
        this.voterId = voterId;
        this.createdAt = createdAt;
    }

    public Vote(Long sessionId, Long candidateId, Long voterId) {
        this.sessionId = sessionId;
        this.candidateId = candidateId;
        this.voterId = voterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
