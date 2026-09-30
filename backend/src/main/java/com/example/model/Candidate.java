package com.example.model;

public class Candidate {
    private Long id;
    private Long sessionId;
    private String name;

    public Candidate() {}

    public Candidate(Long id, Long sessionId, String name) {
        this.id = id;
        this.sessionId = sessionId;
        this.name = name;
    }

    public Candidate(Long sessionId, String name) {
        this.sessionId = sessionId;
        this.name = name;
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
}
