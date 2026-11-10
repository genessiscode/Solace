package econest;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/econest_db";

    private static final String DEFAULT_USER =
            "root";

    private static final String DEFAULT_PASSWORD =
            "";

    private DBConnection() {

    }

    public static Connection getConnection() throws SQLException {

        Properties properties = loadProperties();

        String url = properties.getProperty("db.url", DEFAULT_URL).trim();
        String user = properties.getProperty("db.user", DEFAULT_USER).trim();
        String password = properties.getProperty("db.password", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    public static boolean isAvailable() {

        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static Properties loadProperties() {

        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input != null) {
                properties.load(input);
            }
        } catch (Exception ignored) {

        }

        return properties;
    }
}
