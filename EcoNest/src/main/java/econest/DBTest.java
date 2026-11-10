package econest;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println(
                    "Connection Test PASSED!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Connection Test FAILED! "
                            + e.getMessage()
            );
        }
    }
}
