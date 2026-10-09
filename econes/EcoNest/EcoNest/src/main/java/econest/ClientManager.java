package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access for the {@code clients} table.
 *
 * <p>This class mirrors {@link ProductManager}: every SQL failure is wrapped
 * in an {@link IllegalStateException} so no {@link SQLException} ever escapes
 * to the UI, and an ordinary outcome such as a blocked delete is reported as
 * {@code false} rather than as an exception.</p>
 */
public class ClientManager {

    private static final String SELECT_COLUMNS =
            "SELECT client_id, name, client_type, address, phone,"
                    + " email, company_name FROM clients";

    /**
     * Inserts a new client.
     *
     * @param client the client to insert
     * @throws IllegalStateException if the id already exists or the insert fails
     */
    public void addClient(Client client) {

        String sql = "INSERT INTO clients "
                + "(client_id, name, client_type, address, phone, email,"
                + " company_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindClient(statement, client);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not add client " + client.getClientId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Updates every mutable column of an existing client. The client id is
     * the key and is not itself changed.
     *
     * @param client the client carrying the new values
     * @throws IllegalStateException if no row was updated or the update fails
     */
    public void updateClient(Client client) {

        String sql = "UPDATE clients SET "
                + "name = ?, client_type = ?, address = ?, phone = ?,"
                + " email = ?, company_name = ? "
                + "WHERE client_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getName());
            statement.setString(2, client.getClientType().name());
            statement.setString(3, client.getAddress());
            statement.setString(4, client.getPhone());
            statement.setString(5, client.getEmail());
            statement.setString(6, client.getCompanyName());
            statement.setString(7, client.getClientId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "No client found with ID "
                                + client.getClientId() + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update client " + client.getClientId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Deletes a client.
     *
     * <p>A client that still has orders cannot be deleted: the foreign key is
     * {@code ON DELETE RESTRICT}. That is reported as {@code false} rather
     * than as an exception, because it is an ordinary outcome the user needs
     * to be told about in plain words.</p>
     *
     * @param clientId the client to delete
     * @return {@code true} if a row was deleted, {@code false} if no such
     *         client exists or orders still reference it
     */
    public boolean removeClient(String clientId) {

        String sql = "DELETE FROM clients WHERE client_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, clientId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isForeignKeyViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove client " + clientId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Finds one client by id.
     *
     * @param clientId the id to look up
     * @return the client, or {@code null} when no such client exists
     */
    public Client findClient(String clientId) {

        String sql = SELECT_COLUMNS + " WHERE client_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, clientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read client " + clientId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    /**
     * Reads every client, ordered by client id.
     *
     * @return every client; never {@code null}
     */
    public ArrayList<Client> getClientList() {

        ArrayList<Client> clients = new ArrayList<>();

        String sql = SELECT_COLUMNS + " ORDER BY client_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                clients.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the client list: " + e.getMessage(),
                    e
            );
        }

        return clients;
    }

    // ================= HELPERS =================

    /**
     * Builds one {@link Client} from the current {@code clients} row.
     */
    private static Client mapRow(ResultSet resultSet) throws SQLException {

        return new Client(
                resultSet.getString("client_id"),
                resultSet.getString("name"),
                ClientType.valueOf(resultSet.getString("client_type")),
                resultSet.getString("address"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("company_name")
        );
    }

    /** Binds all seven INSERT parameters, in column order. */
    private static void bindClient(
            PreparedStatement statement,
            Client client) throws SQLException {

        statement.setString(1, client.getClientId());
        statement.setString(2, client.getName());
        statement.setString(3, client.getClientType().name());
        statement.setString(4, client.getAddress());
        statement.setString(5, client.getPhone());
        statement.setString(6, client.getEmail());
        statement.setString(7, client.getCompanyName());
    }

    /** MariaDB reports a constraint failure with SQLState 23000. */
    private static boolean isForeignKeyViolation(SQLException e) {
        return "23000".equals(e.getSQLState());
    }
}
