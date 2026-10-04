package com.example.model;

public class Candidate {
    private Long id;
    private Long sessionId;
    private String name;

    // Result fields calculated from votes
    private long voteCount;
    private double percentage;

    public Candidate() {}

    public Candidate(String name) {
        this.name = name;
    }

    public Candidate(Long id, Long sessionId, String name) {
        this.id = id;
        this.sessionId = sessionId;
        this.name = name;
    }

    public Candidate(Long sessionId, String name) {
        this.sessionId = sessionId;
        this.name = name;
    }

    public Candidate(Long id, String name, long voteCount, double percentage) {
        this.id = id;
        this.name = name;
        this.voteCount = voteCount;
        this.percentage = percentage;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
