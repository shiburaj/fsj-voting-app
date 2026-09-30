package com.example.dao;

import com.example.model.VotingSession;
import com.example.util.DBUtil;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CandidateDAO candidateDAO = new CandidateDAO();

    public String generateUniqueCode() throws SQLException {
        for (int attempts = 0; attempts < 100; attempts++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            String code = sb.toString();
            if (getSessionByCode(code) == null) {
                return code;
            }
        }
        return "S" + System.currentTimeMillis() % 100000;
    }

    public VotingSession createSession(VotingSession session, List<String> candidateNames) throws SQLException {
        String sql = "INSERT INTO sessions (name, user_id, voting_enabled, code) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            if (session.getCode() == null || session.getCode().trim().isEmpty()) {
                session.setCode(generateUniqueCode());
            }

            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, session.getName().trim());
            stmt.setLong(2, session.getUserId());
            stmt.setBoolean(3, session.isVotingEnabled());
            stmt.setString(4, session.getCode().trim().toUpperCase());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating session failed, no rows affected.");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                session.setId(rs.getLong(1));
            }

            // Insert candidate names if provided
            if (candidateNames != null && !candidateNames.isEmpty()) {
                candidateDAO.addCandidates(session.getId(), candidateNames);
                session.setCandidates(candidateDAO.getCandidatesBySessionId(session.getId()));
            }

            return session;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public VotingSession getSessionById(Long id) throws SQLException {
        String sql = "SELECT id, name, user_id, voting_enabled, code, created_at FROM sessions WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                VotingSession session = mapRow(rs);
                session.setCandidates(candidateDAO.getCandidatesBySessionId(session.getId()));
                return session;
            }
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public VotingSession getSessionByCode(String code) throws SQLException {
        String sql = "SELECT id, name, user_id, voting_enabled, code, created_at FROM sessions WHERE UPPER(code) = UPPER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code.trim());
            rs = stmt.executeQuery();

            if (rs.next()) {
                VotingSession session = mapRow(rs);
                session.setCandidates(candidateDAO.getCandidatesBySessionId(session.getId()));
                return session;
            }
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public List<VotingSession> getSessionsByUserId(Long userId) throws SQLException {
        List<VotingSession> list = new ArrayList<>();
        String sql = "SELECT id, name, user_id, voting_enabled, code, created_at FROM sessions WHERE user_id = ? ORDER BY id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                VotingSession s = mapRow(rs);
                s.setCandidates(candidateDAO.getCandidatesBySessionId(s.getId()));
                list.add(s);
            }
            return list;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public boolean updateVotingStatus(Long sessionId, Long userId, boolean votingEnabled) throws SQLException {
        String sql = "UPDATE sessions SET voting_enabled = ? WHERE id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, votingEnabled);
            stmt.setLong(2, sessionId);
            stmt.setLong(3, userId);

            int updated = stmt.executeUpdate();
            return updated > 0;
        } finally {
            DBUtil.close(conn, stmt, null);
        }
    }

    private VotingSession mapRow(ResultSet rs) throws SQLException {
        VotingSession session = new VotingSession();
        session.setId(rs.getLong("id"));
        session.setName(rs.getString("name"));
        session.setUserId(rs.getLong("user_id"));
        session.setVotingEnabled(rs.getBoolean("voting_enabled"));
        session.setCode(rs.getString("code"));
        session.setCreatedAt(rs.getTimestamp("created_at"));
        return session;
    }
}
