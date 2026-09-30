package com.example.dao;

import com.example.model.Candidate;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

    public void addCandidates(Long sessionId, List<String> candidateNames) throws SQLException {
        if (candidateNames == null || candidateNames.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO candidates (session_id, name) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            for (String name : candidateNames) {
                if (name != null && !name.trim().isEmpty()) {
                    stmt.setLong(1, sessionId);
                    stmt.setString(2, name.trim());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
    }

    public List<Candidate> getCandidatesBySessionId(Long sessionId) throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT id, session_id, name FROM candidates WHERE session_id = ? ORDER BY id ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, sessionId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Candidate c = new Candidate();
                c.setId(rs.getLong("id"));
                c.setSessionId(rs.getLong("session_id"));
                c.setName(rs.getString("name"));
                list.add(c);
            }
            return list;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public Candidate getCandidateById(Long candidateId) throws SQLException {
        String sql = "SELECT id, session_id, name FROM candidates WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, candidateId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Candidate c = new Candidate();
                c.setId(rs.getLong("id"));
                c.setSessionId(rs.getLong("session_id"));
                c.setName(rs.getString("name"));
                return c;
            }
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }
}
