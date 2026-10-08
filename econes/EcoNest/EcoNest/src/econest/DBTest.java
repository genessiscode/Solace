package econest;

import java.sql.Connection;

public class DBTest {

    public static void main(String[] args) {

        Connection connection =
                DBConnection.getConnection();

        if (connection != null) {

            System.out.println(
                    "Connection Test PASSED!"
            );

        } else {

            System.out.println(
                    "Connection Test FAILED!"
            );
        }
    }
}
