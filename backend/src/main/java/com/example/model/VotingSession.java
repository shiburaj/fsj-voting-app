package com.example.model;

import java.sql.Timestamp;
import java.util.List;

public class VotingSession {
    private Long id;
    private String name;
    private Long userId;
    private boolean votingEnabled;
    private String code;
    private Timestamp createdAt;
    
    // Optional embedded list for convenience when returning full session
    private List<Candidate> candidates;

    public VotingSession() {}

    public VotingSession(Long id, String name, Long userId, boolean votingEnabled, String code, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.votingEnabled = votingEnabled;
        this.code = code;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isVotingEnabled() {
        return votingEnabled;
    }

    public void setVotingEnabled(boolean votingEnabled) {
        this.votingEnabled = votingEnabled;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
