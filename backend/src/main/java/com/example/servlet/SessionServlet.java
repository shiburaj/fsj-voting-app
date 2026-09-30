package com.example.servlet;

import com.example.dao.SessionDAO;
import com.example.model.CreateSessionRequest;
import com.example.model.ToggleStatusRequest;
import com.example.model.VotingSession;
import com.example.util.AuthHelper;
import com.example.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/sessions", "/api/sessions/status"})
public class SessionServlet extends HttpServlet {

    private final SessionDAO sessionDAO = new SessionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String idParam = request.getParameter("id");

        try {
            // Voter code lookup: /api/sessions?code=XYZ
            if (code != null && !code.trim().isEmpty()) {
                VotingSession session = sessionDAO.getSessionByCode(code.trim());
                if (session == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "No voting session found with code " + code.trim());
                    return;
                }
                JsonUtil.sendOk(response, "Session found", session);
                return;
            }

            // Direct ID lookup: /api/sessions?id=123
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam.trim());
                VotingSession session = sessionDAO.getSessionById(id);
                if (session == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Session not found");
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

        CreateSessionRequest req = JsonUtil.fromJson(request, CreateSessionRequest.class);
        if (req == null || req.getName() == null || req.getName().trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Session name is required");
            return;
        }

        if (req.getCandidates() == null || req.getCandidates().size() < 2) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "At least 2 candidates are required to create a voting session");
            return;
        }

        try {
            VotingSession session = new VotingSession();
            session.setName(req.getName().trim());
            session.setUserId(userId);
            session.setVotingEnabled(true); // Default to enabled so voters can participate immediately

            VotingSession created = sessionDAO.createSession(session, req.getCandidates());
            JsonUtil.sendJson(response, HttpServletResponse.SC_CREATED, com.example.model.ApiResponse.ok("Session created successfully", created));
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

        ToggleStatusRequest req = JsonUtil.fromJson(request, ToggleStatusRequest.class);
        if (req == null || req.getSessionId() == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Session ID is required");
            return;
        }

        try {
            boolean updated = sessionDAO.updateVotingStatus(req.getSessionId(), userId, req.isVotingEnabled());
            if (!updated) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Unable to update session. You might not be the owner or session does not exist.");
                return;
            }

            VotingSession updatedSession = sessionDAO.getSessionById(req.getSessionId());
            JsonUtil.sendOk(response, "Session voting status updated", updatedSession);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
