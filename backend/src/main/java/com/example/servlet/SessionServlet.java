package com.example.servlet;

import com.example.dao.SessionDAO;
import com.example.model.ApiResponse;
import com.example.model.Candidate;
import com.example.model.VotingSession;
import com.example.util.AuthHelper;
import com.example.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/api/sessions", "/api/sessions/status"})
public class SessionServlet extends HttpServlet {

    private final SessionDAO sessionDAO = new SessionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String idParam = request.getParameter("id");

        try {
            // Voter lookup by session code: /api/sessions?code=ABC123
            if (code != null && !code.trim().isEmpty()) {
                VotingSession session = sessionDAO.getSessionByCode(code.trim().toUpperCase());
                if (session == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Voting session not found with code: " + code);
                    return;
                }
                JsonUtil.sendOk(response, "Session details", session);
                return;
            }

            // Lookup by ID: /api/sessions?id=123
            if (idParam != null && !idParam.trim().isEmpty()) {
                long sessionId = Long.parseLong(idParam.trim());
                VotingSession session = sessionDAO.getSessionById(sessionId);
                if (session == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Voting session not found");
                    return;
                }
                JsonUtil.sendOk(response, "Session details", session);
                return;
            }

            // Host lookup: /api/sessions (requires X-User-Id)
            Long userId = AuthHelper.getUserId(request);
            if (userId == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to view created sessions");
                return;
            }

            List<VotingSession> sessions = sessionDAO.getSessionsByUserId(userId);
            JsonUtil.sendOk(response, "User sessions", sessions);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid session ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();

        if ("/api/sessions/status".equals(path)) {
            handleToggleStatus(request, response);
            return;
        }

        // POST /api/sessions (Create new session)
        Long userId = AuthHelper.getUserId(request);
        if (userId == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to create a session");
            return;
        }

        VotingSession session = JsonUtil.fromJson(request, VotingSession.class);
        if (session == null || session.getName() == null || session.getName().trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Session name is required");
            return;
        }

        List<String> candidateNames = new ArrayList<>();
        if (session.getCandidates() != null) {
            for (Candidate c : session.getCandidates()) {
                if (c != null && c.getName() != null && !c.getName().trim().isEmpty()) {
                    candidateNames.add(c.getName().trim());
                }
            }
        }

        if (candidateNames.size() < 2) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "At least 2 candidates are required to create a voting session");
            return;
        }

        try {
            session.setName(session.getName().trim());
            session.setUserId(userId);
            session.setVotingEnabled(true); // Default to enabled so voters can participate immediately

            VotingSession created = sessionDAO.createSession(session, candidateNames);
            JsonUtil.sendJson(response, HttpServletResponse.SC_CREATED, ApiResponse.ok("Session created successfully", created));
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = AuthHelper.getUserId(request);
        if (userId == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        VotingSession req = JsonUtil.fromJson(request, VotingSession.class);
        Long sessionId = req != null ? req.getId() : null;
        if (sessionId == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Session ID is required");
            return;
        }

        try {
            boolean updated = sessionDAO.updateVotingStatus(sessionId, userId, req.isVotingEnabled());
            if (!updated) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Unable to update session. You might not be the owner or session does not exist.");
                return;
            }

            VotingSession updatedSession = sessionDAO.getSessionById(sessionId);
            JsonUtil.sendOk(response, "Session voting status updated", updatedSession);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
