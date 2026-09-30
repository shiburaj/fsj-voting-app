package com.example.dao;

import com.example.model.Candidate;
import com.example.model.CandidateResultDTO;
import com.example.model.SessionResultDTO;
import com.example.model.Vote;
import com.example.model.VotingSession;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.*;

public class VoteDAO {

    private final SessionDAO sessionDAO = new SessionDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();

    public boolean hasUserVoted(Long sessionId, Long voterId) throws SQLException {
        String sql = "SELECT id FROM votes WHERE session_id = ? AND voter_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, sessionId);
            stmt.setLong(2, voterId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public Vote castVote(Vote vote) throws SQLException {
        String sql = "INSERT INTO votes (session_id, candidate_id, voter_id) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, vote.getSessionId());
            stmt.setLong(2, vote.getCandidateId());
            stmt.setLong(3, vote.getVoterId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to record vote.");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                vote.setId(rs.getLong(1));
            }
            return vote;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public SessionResultDTO getResultsBySessionId(Long sessionId) throws SQLException {
        VotingSession session = sessionDAO.getSessionById(sessionId);
        if (session == null) {
            return null;
        }

        List<Candidate> candidates = candidateDAO.getCandidatesBySessionId(sessionId);

        // Fetch vote counts grouped by candidate
        String sql = "SELECT candidate_id, COUNT(*) AS vote_count FROM votes WHERE session_id = ? GROUP BY candidate_id";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Map<Long, Long> countsMap = new HashMap<>();
        long totalVotes = 0;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, sessionId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                long cId = rs.getLong("candidate_id");
                long count = rs.getLong("vote_count");
                countsMap.put(cId, count);
                totalVotes += count;
            }
        } finally {
            DBUtil.close(conn, stmt, rs);
        }

        List<CandidateResultDTO> candidateResults = new ArrayList<>();
        for (Candidate c : candidates) {
            long count = countsMap.getOrDefault(c.getId(), 0L);
            double percentage = totalVotes > 0 ? ((double) count / totalVotes) * 100.0 : 0.0;
            // Round to 1 decimal place
            percentage = Math.round(percentage * 10.0) / 10.0;
            candidateResults.add(new CandidateResultDTO(c.getId(), c.getName(), count, percentage));
        }

        // Sort descending by vote count
        candidateResults.sort((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()));

        SessionResultDTO resultDTO = new SessionResultDTO();
        resultDTO.setSessionId(session.getId());
        resultDTO.setSessionName(session.getName());
        resultDTO.setCode(session.getCode());
        resultDTO.setVotingEnabled(session.isVotingEnabled());
        resultDTO.setTotalVotes(totalVotes);
        resultDTO.setCandidates(candidateResults);

        return resultDTO;
    }
}
