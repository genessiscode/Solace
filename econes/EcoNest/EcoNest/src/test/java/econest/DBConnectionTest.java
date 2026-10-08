package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {

    @Test
    void configFallsBackToDefaultWhenFileIsAbsent() {
        // config.properties is gitignored and will be missing on a fresh clone.
        String url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/econest_db");
        assertNotNull(url);
        assertFalse(url.isBlank(), "db.url must never resolve to blank");
    }

    @Test
    void unknownKeyReturnsItsFallback() {
        assertEquals("fallback-value", AppConfig.get("no.such.key", "fallback-value"));
    }

    @Test
    void getConnectionNeverReturnsNull() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping live connection test");

        try (Connection connection = DBConnection.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        } catch (Exception e) {
            fail("connection should be usable: " + e.getMessage());
        }
    }

    @Test
    void unreachableDatabaseThrowsReadableError() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> DBConnection.openConnection(
                        "jdbc:mysql://localhost:1/definitely_not_a_database",
                        "nobody",
                        "nothing"));

        String message = error.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("jdbc:mysql://localhost:1/definitely_not_a_database"),
                "message must name the URL that failed, was: " + message);
        assertTrue(message.contains("config.properties"),
                "message must point at config.properties, was: " + message);
    }
}
