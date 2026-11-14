package econest.customer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Standalone connection settings for this module.
 * If your EcoNest project already has DBConnection.getConnection(), you can
 * replace calls to DatabaseConnection.getConnection() in CustomerDAO with
 * DBConnection.getConnection() and remove this class.
 */
public final class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/econest?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Add your MySQL password if you use one.

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
