package com.example.servlet;

import com.example.dao.CandidateDAO;
import com.example.dao.SessionDAO;
import com.example.dao.VoteDAO;
import com.example.model.ApiResponse;
import com.example.model.Candidate;
import com.example.model.Vote;
import com.example.model.VoteRequest;
import com.example.model.VotingSession;
import com.example.util.AuthHelper;
import com.example.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/votes", "/api/votes/status"})
public class VoteServlet extends HttpServlet {

    private final VoteDAO voteDAO = new VoteDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();

        if ("/api/votes/status".equals(path)) {
            Long userId = AuthHelper.getUserId(request);
            if (userId == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }

            String sessionIdParam = request.getParameter("sessionId");
            if (sessionIdParam == null || sessionIdParam.trim().isEmpty()) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "sessionId is required");
                return;
            }

            try {
                long sessionId = Long.parseLong(sessionIdParam.trim());
                boolean hasVoted = voteDAO.hasUserVoted(sessionId, userId);
                Map<String, Object> data = new HashMap<>();
                data.put("hasVoted", hasVoted);
                data.put("sessionId", sessionId);
                JsonUtil.sendOk(response, "Vote status", data);
            } catch (SQLException e) {
                e.printStackTrace();
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid sessionId format");
            }
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = AuthHelper.getUserId(request);
        if (userId == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to cast a vote");
            return;
        }

        VoteRequest req = JsonUtil.fromJson(request, VoteRequest.class);
        if (req == null || req.getSessionId() == null || req.getCandidateId() == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Session ID and Candidate ID are required");
            return;
        }

        try {
            // 1. Check if session exists
            VotingSession session = sessionDAO.getSessionById(req.getSessionId());
            if (session == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Voting session not found");
                return;
            }

            // 2. Check if voting is enabled
            if (!session.isVotingEnabled()) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Voting is currently closed for this session.");
                return;
            }

            // 3. Check if candidate belongs to this session
            Candidate candidate = candidateDAO.getCandidateById(req.getCandidateId());
            if (candidate == null || !candidate.getSessionId().equals(session.getId())) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Candidate does not belong to this voting session");
                return;
            }

            // 4. Check if user already voted in this session
            if (voteDAO.hasUserVoted(session.getId(), userId)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "You have already cast your vote in this session.");
                return;
            }

            // 5. Cast vote
            Vote vote = new Vote(session.getId(), candidate.getId(), userId);
            Vote recorded = voteDAO.castVote(vote);

            JsonUtil.sendJson(response, HttpServletResponse.SC_CREATED, ApiResponse.ok("Vote cast successfully!", recorded));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle duplicate key error if caught concurrently
            if (e.getMessage() != null && e.getMessage().contains("unique_voter_session")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "You have already cast your vote in this session.");
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
            }
        }
    }
}
