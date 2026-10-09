package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access for delivery agents and their vehicles.
 *
 * <p>Mirrors {@link ProductManager}: plain JDBC, prepared statements only,
 * and every {@link SQLException} wrapped in an
 * {@link IllegalStateException} so no SQL failure ever reaches the UI
 * uncaught.</p>
 *
 * <p>The vehicles table carries a {@code UNIQUE} plate number, so a
 * duplicate plate is reported with a message that names the plate rather
 * than with the raw driver error. Removing an agent whose delivery history
 * still references them ({@code ON DELETE RESTRICT} on
 * {@code deliveries.agent_id}) returns {@code false} instead of throwing,
 * because it is an ordinary outcome the user needs to be told about in
 * plain words.</p>
 */
public class AgentManager {

    private static final String AGENT_COLUMNS =
            "SELECT agent_id, name, phone, email, status FROM agents";

    private static final String VEHICLE_COLUMNS =
            "SELECT vehicle_id, agent_id, type, plate_number,"
                    + " capacity_kg FROM vehicles";

    // ================= AGENT CRUD =================

    /**
     * Inserts a new agent.
     *
     * @param agent the agent to insert
     * @throws IllegalStateException if the id already exists or the insert
     *                               fails
     */
    public void addAgent(Agent agent) {

        String sql = "INSERT INTO agents "
                + "(agent_id, name, phone, email, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agent.getAgentId());
            statement.setString(2, agent.getName());
            statement.setString(3, agent.getPhone());
            statement.setString(4, agent.getEmail());
            statement.setString(5, agent.getStatus().name());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not add agent " + agent.getAgentId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Updates every column of an existing agent. The agent id is the key
     * and is not itself changed.
     *
     * @param agent the agent carrying the new values
     * @throws IllegalStateException if no row was updated or the update
     *                               fails
     */
    public void updateAgent(Agent agent) {

        String sql = "UPDATE agents SET "
                + "name = ?, phone = ?, email = ?, status = ? "
                + "WHERE agent_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agent.getName());
            statement.setString(2, agent.getPhone());
            statement.setString(3, agent.getEmail());
            statement.setString(4, agent.getStatus().name());
            statement.setString(5, agent.getAgentId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "No agent found with ID "
                                + agent.getAgentId() + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update agent " + agent.getAgentId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Deletes an agent. The agent's vehicle goes with it, because
     * {@code vehicles.agent_id} is {@code ON DELETE CASCADE}.
     *
     * <p>An agent that still has deliveries cannot be deleted: the foreign
     * key is {@code ON DELETE RESTRICT}. That is reported as
     * {@code false} rather than as an exception, because it is an ordinary
     * outcome the user needs to be told about in plain words.</p>
     *
     * @param agentId the agent to delete
     * @return {@code true} if a row was deleted, {@code false} if no such
     *         agent exists or deliveries still reference them
     */
    public boolean removeAgent(String agentId) {

        String sql = "DELETE FROM agents WHERE agent_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isForeignKeyViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove agent " + agentId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Finds one agent by id.
     *
     * @param agentId the id to look up
     * @return the agent, or {@code null} when no such agent exists
     */
    public Agent findAgent(String agentId) {

        String sql = AGENT_COLUMNS + " WHERE agent_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAgent(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read agent " + agentId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    /**
     * Reads every agent, ordered by agent id.
     *
     * @return every agent; never {@code null}
     */
    public ArrayList<Agent> getAgentList() {

        ArrayList<Agent> agents = new ArrayList<>();

        String sql = AGENT_COLUMNS + " ORDER BY agent_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                agents.add(mapAgent(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the agent list: " + e.getMessage(),
                    e
            );
        }

        return agents;
    }

    // ================= VEHICLE CRUD =================

    /**
     * Inserts a new vehicle for an existing agent.
     *
     * <p>The plate number is {@code UNIQUE}, so a plate already in use is
     * reported as an {@link IllegalStateException} naming the plate. A
     * capacity of {@code NaN} is stored as SQL {@code NULL}.</p>
     *
     * @param vehicle the vehicle to insert
     * @throws IllegalStateException if the id or plate already exists, the
     *                               agent does not exist, or the insert
     *                               fails
     */
    public void addVehicle(Vehicle vehicle) {

        String sql = "INSERT INTO vehicles "
                + "(vehicle_id, agent_id, type, plate_number, capacity_kg) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindVehicle(statement, vehicle);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw describeVehicleFailure(
                    "add", vehicle.getVehicleId(), vehicle.getPlateNumber(), e);
        }
    }

    /**
     * Updates every column of an existing vehicle. The vehicle id is the
     * key and is not itself changed.
     *
     * <p>A capacity of {@code NaN} is stored as SQL {@code NULL}.</p>
     *
     * @param vehicle the vehicle carrying the new values
     * @throws IllegalStateException if no row was updated, the plate is
     *                               already used by another vehicle, or
     *                               the update fails
     */
    public void updateVehicle(Vehicle vehicle) {

        String sql = "UPDATE vehicles SET "
                + "agent_id = ?, type = ?, plate_number = ?, capacity_kg = ? "
                + "WHERE vehicle_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, vehicle.getAgentId());
            statement.setString(2, vehicle.getType().name());
            statement.setString(3, vehicle.getPlateNumber());
            bindCapacity(statement, 4, vehicle.getCapacityKg());
            statement.setString(5, vehicle.getVehicleId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "No vehicle found with ID "
                                + vehicle.getVehicleId() + ".");
            }

        } catch (SQLException e) {

            throw describeVehicleFailure(
                    "update", vehicle.getVehicleId(), vehicle.getPlateNumber(), e);
        }
    }

    /**
     * Deletes a vehicle. The agent it belonged to is not affected.
     *
     * @param vehicleId the vehicle to delete
     * @return {@code true} if a row was deleted, {@code false} if no such
     *         vehicle exists
     * @throws IllegalStateException if the delete fails for another reason
     */
    public boolean removeVehicle(String vehicleId) {

        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, vehicleId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isForeignKeyViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove vehicle " + vehicleId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Finds the vehicle assigned to one agent, if any.
     *
     * @param agentId the agent to look up
     * @return the agent's vehicle, or {@code null} when the agent has no
     *         vehicle
     */
    public Vehicle findVehicleByAgent(String agentId) {

        String sql = VEHICLE_COLUMNS + " WHERE agent_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapVehicle(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the vehicle of agent " + agentId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    // ================= JOINED TABLE =================

    /**
     * Reads every agent together with its vehicle for the agents table.
     *
     * <p>Agents without a vehicle still appear, with the vehicle columns
     * set to {@code null}.</p>
     *
     * @return one row per agent, ordered by agent id; never {@code null}
     */
    public ArrayList<AgentRow> getAgentTableList(String travelDate) {

        ArrayList<AgentRow> rows = new ArrayList<>();

        // Filter by delivery schedule on travel date; derived from deliveries
        String sql = "SELECT a.agent_id, a.name, a.phone, a.email, a.status,"
                + " v.vehicle_id, v.type, v.plate_number, v.capacity_kg"
                + " FROM agents a"
                + " LEFT JOIN vehicles v ON v.agent_id = a.agent_id"
                + " LEFT JOIN deliveries d ON d.agent_id = a.agent_id"
                + " AND d.scheduled_date = ?"
                + " WHERE d.delivery_id IS NULL OR d.status IN ('ASSIGNED','IN_TRANSIT')"
                + " ORDER BY a.agent_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, travelDate);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String vehicleId = resultSet.getString("vehicle_id");

                    VehicleType vehicleType = null;
                    String plateNumber = null;
                    Double capacityKg = null;

                    if (vehicleId != null) {

                        vehicleType = VehicleType.valueOf(
                                resultSet.getString("type"));

                        plateNumber = resultSet.getString("plate_number");

                        double capacity = resultSet.getDouble("capacity_kg");

                        if (!resultSet.wasNull()) {
                            capacityKg = capacity;
                        }
                    }

                    rows.add(new AgentRow(
                            resultSet.getString("agent_id"),
                            resultSet.getString("name"),
                            resultSet.getString("phone"),
                            resultSet.getString("email"),
                            AgentStatus.valueOf(resultSet.getString("status")),
                            vehicleId,
                            vehicleType,
                            plateNumber,
                            capacityKg
                    ));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the agent table: " + e.getMessage(),
                    e
            );
        }

        return rows;
    }

    // ================= NESTED ROW CLASS =================

    /**
     * One row of the agents table: an agent LEFT JOINed with its vehicle.
     *
     * <p>The vehicle columns are {@code null} when the agent has no
     * vehicle, so the UI can show empty cells without special-casing the
     * join.</p>
     */
    public static final class AgentRow {

        private final String agentId;

        private final String name;

        private final String phone;

        private final String email;

        private final AgentStatus status;

        private final String vehicleId;

        private final VehicleType vehicleType;

        private final String plateNumber;

        private final Double capacityKg;

        /**
         * @param agentId     the agent identifier
         * @param name        the agent's display name
         * @param phone       the agent's phone number, or {@code null}
         * @param email       the agent's email address, or {@code null}
         * @param status      the agent's availability
         * @param vehicleId   the vehicle id, or {@code null} when the
         *                    agent has no vehicle
         * @param vehicleType the vehicle type, or {@code null} when the
         *                    agent has no vehicle
         * @param plateNumber the plate number, or {@code null} when the
         *                    agent has no vehicle
         * @param capacityKg  the load capacity, or {@code null} when the
         *                    agent has no vehicle or no capacity is
         *                    recorded
         */
        public AgentRow(
                String agentId,
                String name,
                String phone,
                String email,
                AgentStatus status,
                String vehicleId,
                VehicleType vehicleType,
                String plateNumber,
                Double capacityKg) {

            this.agentId = agentId;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.status = status;
            this.vehicleId = vehicleId;
            this.vehicleType = vehicleType;
            this.plateNumber = plateNumber;
            this.capacityKg = capacityKg;
        }

        /**
         * @return the agent identifier
         */
        public String getAgentId() {
            return agentId;
        }

        /**
         * @return the agent's display name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the agent's phone number, or {@code null} when absent
         */
        public String getPhone() {
            return phone;
        }

        /**
         * @return the agent's email address, or {@code null} when absent
         */
        public String getEmail() {
            return email;
        }

        /**
         * @return the agent's availability
         */
        public AgentStatus getStatus() {
            return status;
        }

        /**
         * @return the vehicle id, or {@code null} when the agent has no
         *         vehicle
         */
        public String getVehicleId() {
            return vehicleId;
        }

        /**
         * @return the vehicle type, or {@code null} when the agent has no
         *         vehicle
         */
        public VehicleType getVehicleType() {
            return vehicleType;
        }

        /**
         * @return the plate number, or {@code null} when the agent has no
         *         vehicle
         */
        public String getPlateNumber() {
            return plateNumber;
        }

        /**
         * @return the load capacity in kilograms, or {@code null} when
         *         the agent has no vehicle or no capacity is recorded
         */
        public Double getCapacityKg() {
            return capacityKg;
        }
    }

    // ================= HELPERS =================

    /** Maps one {@code agents} row. */
    private static Agent mapAgent(ResultSet resultSet) throws SQLException {

        return new Agent(
                resultSet.getString("agent_id"),
                resultSet.getString("name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                AgentStatus.valueOf(resultSet.getString("status"))
        );
    }

    /** Maps one {@code vehicles} row. */
    private static Vehicle mapVehicle(ResultSet resultSet) throws SQLException {

        double capacity = resultSet.getDouble("capacity_kg");

        double capacityKg = resultSet.wasNull() ? Double.NaN : capacity;

        return new Vehicle(
                resultSet.getString("vehicle_id"),
                resultSet.getString("agent_id"),
                VehicleType.valueOf(resultSet.getString("type")),
                resultSet.getString("plate_number"),
                capacityKg
        );
    }

    /** Binds all five vehicle INSERT parameters, in column order. */
    private static void bindVehicle(
            PreparedStatement statement,
            Vehicle vehicle) throws SQLException {

        statement.setString(1, vehicle.getVehicleId());
        statement.setString(2, vehicle.getAgentId());
        statement.setString(3, vehicle.getType().name());
        statement.setString(4, vehicle.getPlateNumber());
        bindCapacity(statement, 5, vehicle.getCapacityKg());
    }

    /** Writes the capacity, treating {@code NaN} as SQL NULL. */
    private static void bindCapacity(
            PreparedStatement statement,
            int index,
            double capacityKg) throws SQLException {

        if (Double.isNaN(capacityKg)) {
            statement.setNull(index, java.sql.Types.DECIMAL);
        } else {
            statement.setDouble(index, capacityKg);
        }
    }

    /**
     * Wraps a failed vehicle statement, turning the duplicate-key error on
     * {@code vehicles.plate_number} into a message that names the plate.
     */
    private static IllegalStateException describeVehicleFailure(
            String action,
            String vehicleId,
            String plateNumber,
            SQLException e) {

        if (isDuplicateKey(e)) {

            String detail = e.getMessage() == null
                    ? ""
                    : e.getMessage().toLowerCase();

            if (detail.contains("plate")) {
                return new IllegalStateException(
                        "Plate number " + plateNumber
                                + " is already used by another vehicle.",
                        e
                );
            }

            return new IllegalStateException(
                    "Vehicle ID " + vehicleId + " already exists.",
                    e
            );
        }

        return new IllegalStateException(
                "Could not " + action + " vehicle " + vehicleId
                        + ": " + e.getMessage(),
                e
        );
    }

    /** MariaDB reports a constraint failure with SQLState 23000. */
    private static boolean isForeignKeyViolation(SQLException e) {
        return "23000".equals(e.getSQLState());
    }

    /** A duplicate primary or unique key - never a foreign key. */
    private static boolean isDuplicateKey(SQLException e) {
        return isForeignKeyViolation(e)
                && e.getMessage() != null
                && e.getMessage().toLowerCase().contains("duplicate");
    }
}
