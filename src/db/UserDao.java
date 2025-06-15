package dao;

import db.dbConnection;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean createUser(User user) {
        // PERBARUI SQL: Tambahkan kolom 'role'
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole()); // Set role, defaultnya 'USER'

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String password) {
        // PERBARUI SQL: Ambil juga kolom 'role'
        String sql = "SELECT id_users, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // PERBARUI INSTANSIASI USER: Sertakan role
                    return new User(
                            rs.getInt("id_users"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role") // Ambil role dari database
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}