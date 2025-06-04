package dao;

import db.dbConnection;
import model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class questionDao {

    // Menambahkan soal baru
    public static boolean addQuestion(Question question) {
        String query = "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getOptionA());
            stmt.setString(3, question.getOptionB());
            stmt.setString(4, question.getOptionC());
            stmt.setString(5, question.getOptionD());
            stmt.setString(6, question.getCorrectOption());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Jika berhasil insert
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
}
