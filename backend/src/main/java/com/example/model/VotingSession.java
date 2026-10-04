package com.example.model;

import java.sql.Timestamp;
import java.util.List;

public class VotingSession {
    private Long id;
    private Long sessionId;
    private String name;
    private Long userId;
    private boolean votingEnabled;
    private String code;
    private Timestamp createdAt;

    // Total votes across all candidates in this session
    private long totalVotes;

    // Embedded candidate list
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
        return id != null ? id : sessionId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId != null ? sessionId : id;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
