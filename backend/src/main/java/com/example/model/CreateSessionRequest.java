package com.example.model;

import java.util.List;

public class CreateSessionRequest {
    private String name;
    private List<String> candidates;

    public CreateSessionRequest() {}

    public CreateSessionRequest(String name, List<String> candidates) {
        this.name = name;
        this.candidates = candidates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }
}
