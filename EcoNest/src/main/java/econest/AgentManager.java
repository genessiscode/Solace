package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access for delivery agents and their vehicles.
 *
 * <p>Uses plain JDBC with prepared statements. Every {@link SQLException} is
 * wrapped in an {@link IllegalStateException} so a database failure never
 * reaches the UI uncaught.</p>
 */
public class AgentManager {

    private static final String AGENT_COLUMNS =
            "SELECT agent_id, name, phone, email, status FROM agents";

    private static final String VEHICLE_COLUMNS =
            "SELECT vehicle_id, agent_id, type, plate_number,"
                    + " capacity_kg FROM vehicles";

    // ================= AGENT CRUD =================

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

            if (isDuplicateKey(e)) {
                throw new IllegalStateException(
                        "Agent ID " + agent.getAgentId()
                                + " already exists.", e);
            }

            throw new IllegalStateException(
                    "Could not add agent " + agent.getAgentId()
                            + ": " + e.getMessage(), e);
        }
    }

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

            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "No agent found with ID "
                                + agent.getAgentId() + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update agent " + agent.getAgentId()
                            + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an agent. Its vehicle is removed by the
     * {@code ON DELETE CASCADE} foreign key.
     *
     * <p>An agent that still has deliveries is protected by
     * {@code ON DELETE RESTRICT}; that is reported as {@code false} because
     * it is an expected outcome, not a database failure.</p>
     */
    public boolean removeAgent(String agentId) {

        String sql = "DELETE FROM agents WHERE agent_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isConstraintViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove agent " + agentId
                            + ": " + e.getMessage(), e);
        }
    }

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
                            + ": " + e.getMessage(), e);
        }

        return null;
    }

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
                    "Could not read the agent list: " + e.getMessage(), e);
        }

        return agents;
    }

    // ================= VEHICLE CRUD =================

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
                    "add", vehicle.getVehicleId(),
                    vehicle.getPlateNumber(), e);
        }
    }

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

            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "No vehicle found with ID "
                                + vehicle.getVehicleId() + ".");
            }

        } catch (SQLException e) {

            throw describeVehicleFailure(
                    "update", vehicle.getVehicleId(),
                    vehicle.getPlateNumber(), e);
        }
    }

    public boolean removeVehicle(String vehicleId) {

        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, vehicleId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isConstraintViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove vehicle " + vehicleId
                            + ": " + e.getMessage(), e);
        }
    }

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
                            + ": " + e.getMessage(), e);
        }

        return null;
    }

    // ================= JOINED TABLE =================

    /**
     * Reads every agent together with its vehicle. Agents without a vehicle
     * still appear, with the vehicle columns set to {@code null}.
     */
    public ArrayList<AgentRow> getAgentTableList() {

        ArrayList<AgentRow> rows = new ArrayList<>();

        String sql = "SELECT a.agent_id, a.name, a.phone, a.email, a.status,"
                + " v.vehicle_id, v.type, v.plate_number, v.capacity_kg"
                + " FROM agents a"
                + " LEFT JOIN vehicles v ON v.agent_id = a.agent_id"
                + " ORDER BY a.agent_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

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

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the agent table: " + e.getMessage(), e);
        }

        return rows;
    }

    // ================= NESTED ROW CLASS =================

    /**
     * One row of the agents table: an agent joined with its vehicle.
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

        public String getAgentId() {
            return agentId;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public AgentStatus getStatus() {
            return status;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public VehicleType getVehicleType() {
            return vehicleType;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public Double getCapacityKg() {
            return capacityKg;
        }
    }

    // ================= HELPERS =================

    private static Agent mapAgent(ResultSet resultSet) throws SQLException {

        return new Agent(
                resultSet.getString("agent_id"),
                resultSet.getString("name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                AgentStatus.valueOf(resultSet.getString("status"))
        );
    }

    private static Vehicle mapVehicle(ResultSet resultSet)
            throws SQLException {

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

    private static void bindVehicle(
            PreparedStatement statement,
            Vehicle vehicle) throws SQLException {

        statement.setString(1, vehicle.getVehicleId());
        statement.setString(2, vehicle.getAgentId());
        statement.setString(3, vehicle.getType().name());
        statement.setString(4, vehicle.getPlateNumber());
        bindCapacity(statement, 5, vehicle.getCapacityKg());
    }

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
                                + " is already used by another vehicle.", e);
            }

            return new IllegalStateException(
                    "Vehicle ID " + vehicleId + " already exists.", e);
        }

        return new IllegalStateException(
                "Could not " + action + " vehicle " + vehicleId
                        + ": " + e.getMessage(), e);
    }

    private static boolean isConstraintViolation(SQLException e) {
        return "23000".equals(e.getSQLState());
    }

    private static boolean isDuplicateKey(SQLException e) {
        return isConstraintViolation(e)
                && e.getMessage() != null
                && e.getMessage().toLowerCase().contains("duplicate");
    }
}
