package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnection {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost/db_quiz"; // Nama Database
            String user = "root"; // Username
            String pass = "dit"; // Password
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
