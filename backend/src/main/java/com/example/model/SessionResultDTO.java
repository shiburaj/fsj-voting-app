package com.example.model;

import java.util.List;

public class SessionResultDTO {
    private Long sessionId;
    private String sessionName;
    private String code;
    private boolean votingEnabled;
    private long totalVotes;
    private List<CandidateResultDTO> candidates;

    public SessionResultDTO() {}

    public SessionResultDTO(Long sessionId, String sessionName, String code, boolean votingEnabled, long totalVotes, List<CandidateResultDTO> candidates) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.code = code;
        this.votingEnabled = votingEnabled;
        this.totalVotes = totalVotes;
        this.candidates = candidates;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isVotingEnabled() {
        return votingEnabled;
    }

    public void setVotingEnabled(boolean votingEnabled) {
        this.votingEnabled = votingEnabled;
    }

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    public List<CandidateResultDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateResultDTO> candidates) {
        this.candidates = candidates;
    }
}
