package com.example.servlet;

import com.example.dao.VoteDAO;
import com.example.model.VotingSession;
import com.example.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/api/results", "/api/sessions/results"})
public class ResultServlet extends HttpServlet {

    private final VoteDAO voteDAO = new VoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionIdParam = request.getParameter("sessionId");
        if (sessionIdParam == null || sessionIdParam.trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "sessionId query parameter is required");
            return;
        }

        try {
            long sessionId = Long.parseLong(sessionIdParam.trim());
            VotingSession result = voteDAO.getResultsBySessionId(sessionId);
            if (result == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Voting session not found");
                return;
            }

            JsonUtil.sendOk(response, "Session results retrieved", result);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid sessionId format");
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
