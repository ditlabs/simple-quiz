package dao;

import db.dbConnection;
import model.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class resultDao {

    // Menyimpan hasil kuis baru
    public boolean saveResult(int userId, int score) {
        String sql = "INSERT INTO results (user_id, score) VALUES (?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, score);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengambil riwayat kuis untuk satu pengguna
    public List<Result> getHistoryForUser(int userId) {
        List<Result> history = new ArrayList<>();
        String sql = "SELECT r.result_id, r.user_id, u.username, r.score, r.quiz_date " +
                "FROM results r JOIN users u ON r.user_id = u.id_users " +
                "WHERE r.user_id = ? ORDER BY r.quiz_date DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new Result(
                        rs.getInt("result_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getTimestamp("quiz_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Mengambil semua hasil untuk statistik admin
    public List<Result> getAllResults() {
        List<Result> allResults = new ArrayList<>();
        String sql = "SELECT r.result_id, r.user_id, u.username, r.score, r.quiz_date " +
                "FROM results r JOIN users u ON r.user_id = u.id_users " +
                "ORDER BY r.score DESC";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allResults.add(new Result(
                        rs.getInt("result_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getTimestamp("quiz_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allResults;
    }
}