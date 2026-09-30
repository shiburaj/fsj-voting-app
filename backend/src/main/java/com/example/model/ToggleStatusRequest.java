package com.example.model;

public class ToggleStatusRequest {
    private Long sessionId;
    private boolean votingEnabled;

    public ToggleStatusRequest() {}

    public ToggleStatusRequest(Long sessionId, boolean votingEnabled) {
        this.sessionId = sessionId;
        this.votingEnabled = votingEnabled;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isVotingEnabled() {
        return votingEnabled;
    }

    public void setVotingEnabled(boolean votingEnabled) {
        this.votingEnabled = votingEnabled;
    }
}
