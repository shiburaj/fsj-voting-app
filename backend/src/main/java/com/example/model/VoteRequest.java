package com.example.model;

public class VoteRequest {
    private Long sessionId;
    private Long candidateId;

    public VoteRequest() {}

    public VoteRequest(Long sessionId, Long candidateId) {
        this.sessionId = sessionId;
        this.candidateId = candidateId;
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
}
