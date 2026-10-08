package econest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC connections to the EcoNest database.
 *
 * <p>Connection details come from {@code config.properties}, falling back to
 * the values in {@code config.properties.template}. A failed connection throws
 * an {@link IllegalStateException} naming the URL and the configuration file,
 * rather than returning {@code null} and leaving callers to fail later with a
 * confusing {@link NullPointerException}.</p>
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/econest_db";

    private static final String DEFAULT_USER = "root";

    private static final String DEFAULT_PASSWORD = "";

    private DBConnection() {
        // utility class - not instantiable
    }

    /**
     * Opens a connection using the configured database settings.
     *
     * @return a live connection; never {@code null}
     * @throws IllegalStateException if the database cannot be reached
     */
    public static Connection getConnection() {

        String url = AppConfig.get("db.url", DEFAULT_URL);

        String user = AppConfig.get("db.user", DEFAULT_USER);

        String password = AppConfig.get("db.password", DEFAULT_PASSWORD);

        return openConnection(url, user, password);
    }

    /**
     * Opens a connection to an explicit URL. Exposed so the failure path can be
     * tested without taking the configured database down.
     *
     * @param url      the JDBC URL
     * @param user     the database user
     * @param password the database password
     * @return a live connection; never {@code null}
     * @throws IllegalStateException if the database cannot be reached
     */
    public static Connection openConnection(
            String url,
            String user,
            String password) {

        try {

            return DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Cannot connect to the EcoNest database at " + url
                            + ". Check that MariaDB is running and that"
                            + " config.properties holds the right"
                            + " credentials (copy config.properties.template"
                            + " if the file is missing).",
                    e
            );
        }
    }

    /**
     * Reports whether the configured database is reachable, without throwing.
     * Used by tests to skip when no database is present.
     *
     * @return {@code true} if a connection could be opened and closed
     */
    public static boolean isAvailable() {

        try (Connection connection = getConnection()) {

            return connection != null && !connection.isClosed();

        } catch (Exception e) {

            return false;
        }
    }
}
