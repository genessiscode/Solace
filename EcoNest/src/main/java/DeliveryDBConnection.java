package econest;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Uses the same config.properties keys as the Solace team project. */
public final class DeliveryDBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/econest_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DeliveryDBConnection() { }

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        try (InputStream input = DeliveryDBConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) properties.load(input);
        } catch (Exception ex) {
            throw new SQLException("Could not read config.properties", ex);
        }

        String url = properties.getProperty("db.url", DEFAULT_URL).trim();
        String user = properties.getProperty("db.user", DEFAULT_USER).trim();
        String password = properties.getProperty("db.password", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
