package dao;

import db.dbConnection;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    /**
     * Menyimpan user baru ke database.
     * @param user Objek User yang akan disimpan.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Memeriksa kredensial login.
     * @param username Username yang dimasukkan.
     * @param password Password yang dimasukkan.
     * @return Objek User jika login berhasil, null jika gagal.
     */
    public User login(String username, String password) {
        String sql = "SELECT id_users, username, password FROM users WHERE username = ? AND password = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Jika data ditemukan, buat objek User dan kembalikan
                    return new User(
                            // PERUBAHAN DI SINI: Mengambil kolom "id_users" sesuai nama di database Anda
                            rs.getInt("id_users"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Jika tidak ada data yang cocok atau terjadi error, kembalikan null
        return null;
    }
}