package backend;

import database.DatabaseConnection;
import model.DeliveryAgent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryAgentService {

    public boolean addAgent(DeliveryAgent agent) throws SQLException {
        String sql = "INSERT INTO delivery_agents " +
                     "(agent_id, name, phone, address, vehicle_number, vehicle_type, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, agent.getAgentId());
            ps.setString(2, agent.getName());
            ps.setString(3, agent.getPhone());
            ps.setString(4, agent.getAddress());
            ps.setString(5, agent.getVehicleNumber());
            ps.setString(6, agent.getVehicleType());
            ps.setString(7, agent.getStatus());

            return ps.executeUpdate() > 0;
        }
    }

    public List<DeliveryAgent> getAllAgents() throws SQLException {
        List<DeliveryAgent> agents = new ArrayList<>();

        String sql = "SELECT agent_id, name, phone, address, vehicle_number, " +
                     "vehicle_type, status FROM delivery_agents ORDER BY agent_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                agents.add(new DeliveryAgent(
                    rs.getString("agent_id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("vehicle_number"),
                    rs.getString("vehicle_type"),
                    rs.getString("status")
                ));
            }
        }

        return agents;
    }

    public boolean updateAgent(DeliveryAgent agent) throws SQLException {
        String sql = "UPDATE delivery_agents SET name=?, phone=?, address=?, " +
                     "vehicle_number=?, vehicle_type=?, status=? WHERE agent_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, agent.getName());
            ps.setString(2, agent.getPhone());
            ps.setString(3, agent.getAddress());
            ps.setString(4, agent.getVehicleNumber());
            ps.setString(5, agent.getVehicleType());
            ps.setString(6, agent.getStatus());
            ps.setString(7, agent.getAgentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAgent(String agentId) throws SQLException {
        String sql = "DELETE FROM delivery_agents WHERE agent_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, agentId);
            return ps.executeUpdate() > 0;
        }
    }

    public DeliveryAgent searchAgent(String agentId) throws SQLException {
        String sql = "SELECT agent_id, name, phone, address, vehicle_number, " +
                     "vehicle_type, status FROM delivery_agents WHERE agent_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, agentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DeliveryAgent(
                        rs.getString("agent_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("status")
                    );
                }
            }
        }

        return null;
    }
}
