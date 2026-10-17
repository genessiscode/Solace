package econest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database operations for Task 6: Delivery Assignment. */
public class DeliveryManager {

    public List<String[]> getAssignableOrders() throws SQLException {
        String sql = "SELECT o.order_id, c.name " +
                "FROM orders o JOIN clients c ON c.client_id=o.client_id " +
                "WHERE o.status IN ('PENDING','CONFIRMED') " +
                "AND NOT EXISTS (SELECT 1 FROM deliveries d WHERE d.order_id=o.order_id " +
                "AND d.status IN ('ASSIGNED','IN_TRANSIT')) " +
                "ORDER BY o.order_date, o.order_id";
        List<String[]> result = new ArrayList<>();
        try (Connection con = DeliveryDBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new String[]{rs.getString("order_id"), rs.getString("name")});
        }
        return result;
    }

    public List<String[]> getAvailableAgents() throws SQLException {
        String sql = "SELECT agent_id, name FROM agents WHERE status='AVAILABLE' ORDER BY agent_id";
        List<String[]> result = new ArrayList<>();
        try (Connection con = DeliveryDBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new String[]{rs.getString("agent_id"), rs.getString("name")});
        }
        return result;
    }

    public List<Delivery> getAllDeliveries() throws SQLException {
        String sql = "SELECT d.delivery_id, d.order_id, c.name AS client_name, " +
                "d.agent_id, a.name AS agent_name, d.scheduled_date, d.status " +
                "FROM deliveries d JOIN orders o ON o.order_id=d.order_id " +
                "JOIN clients c ON c.client_id=o.client_id " +
                "JOIN agents a ON a.agent_id=d.agent_id " +
                "ORDER BY d.scheduled_date DESC, d.delivery_id DESC";
        List<Delivery> result = new ArrayList<>();
        try (Connection con = DeliveryDBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date date = rs.getDate("scheduled_date");
                result.add(new Delivery(rs.getString("delivery_id"), rs.getString("order_id"),
                        rs.getString("client_name"), rs.getString("agent_id"),
                        rs.getString("agent_name"), date.toLocalDate(), rs.getString("status")));
            }
        }
        return result;
    }

    public void assignDelivery(String orderId, String agentId, LocalDate scheduledDate) throws SQLException {
        if (orderId == null || orderId.isBlank() || agentId == null || agentId.isBlank() || scheduledDate == null) {
            throw new IllegalArgumentException("Choose an order, an available agent, and a delivery date.");
        }

        try (Connection con = DeliveryDBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Lock the order and agent while validating the assignment.
                String orderStatus = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT status FROM orders WHERE order_id=? FOR UPDATE")) {
                    ps.setString(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) orderStatus = rs.getString("status");
                    }
                }
                if (orderStatus == null) throw new SQLException("The selected order no longer exists.");
                if (!(orderStatus.equals("PENDING") || orderStatus.equals("CONFIRMED"))) {
                    throw new SQLException("Only pending or confirmed orders can be assigned.");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT status FROM agents WHERE agent_id=? FOR UPDATE")) {
                    ps.setString(1, agentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"AVAILABLE".equals(rs.getString("status"))) {
                            throw new SQLException("This agent is no longer available. Refresh and choose another agent.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM deliveries WHERE order_id=? AND status IN ('ASSIGNED','IN_TRANSIT')")) {
                    ps.setString(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("This order already has an active delivery assignment.");
                        }
                    }
                }

                String deliveryId = nextDeliveryId(con);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO deliveries (delivery_id, order_id, agent_id, scheduled_date, status) VALUES (?, ?, ?, ?, 'ASSIGNED')")) {
                    ps.setString(1, deliveryId);
                    ps.setString(2, orderId);
                    ps.setString(3, agentId);
                    ps.setDate(4, Date.valueOf(scheduledDate));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE agents SET status='ON_DELIVERY' WHERE agent_id=?")) {
                    ps.setString(1, agentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE orders SET status='DISPATCHED' WHERE order_id=?")) {
                    ps.setString(1, orderId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void updateDeliveryStatus(String deliveryId, String newStatus) throws SQLException {
        if (!List.of("ASSIGNED", "IN_TRANSIT", "DELIVERED", "FAILED").contains(newStatus)) {
            throw new IllegalArgumentException("Choose a valid delivery status.");
        }
        try (Connection con = DeliveryDBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String orderId;
                String agentId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT order_id, agent_id FROM deliveries WHERE delivery_id=? FOR UPDATE")) {
                    ps.setString(1, deliveryId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Delivery assignment not found.");
                        orderId = rs.getString("order_id");
                        agentId = rs.getString("agent_id");
                    }
                }

                String sql = "UPDATE deliveries SET status=?, delivered_at=" +
                        ("DELIVERED".equals(newStatus) ? "CURRENT_TIMESTAMP" : "NULL") +
                        " WHERE delivery_id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newStatus);
                    ps.setString(2, deliveryId);
                    ps.executeUpdate();
                }
                if ("DELIVERED".equals(newStatus)) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE orders SET status='DELIVERED' WHERE order_id=?")) {
                        ps.setString(1, orderId); ps.executeUpdate();
                    }
                    releaseAgentIfNoActiveDelivery(con, agentId, deliveryId);
                } else if ("FAILED".equals(newStatus)) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE orders SET status='CONFIRMED' WHERE order_id=? AND status='DISPATCHED'")) {
                        ps.setString(1, orderId); ps.executeUpdate();
                    }
                    releaseAgentIfNoActiveDelivery(con, agentId, deliveryId);
                } else if ("IN_TRANSIT".equals(newStatus)) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE agents SET status='ON_DELIVERY' WHERE agent_id=?")) {
                        ps.setString(1, agentId); ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void releaseAgentIfNoActiveDelivery(Connection con, String agentId, String currentDeliveryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE agent_id=? AND delivery_id<>? AND status IN ('ASSIGNED','IN_TRANSIT')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, agentId); ps.setString(2, currentDeliveryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement update = con.prepareStatement("UPDATE agents SET status='AVAILABLE' WHERE agent_id=?")) {
                        update.setString(1, agentId); update.executeUpdate();
                    }
                }
            }
        }
    }

    private String nextDeliveryId(Connection con) throws SQLException {
        // IDs are D001, D002, ... and are generated from the existing delivery records.
        int max = 0;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT delivery_id FROM deliveries WHERE delivery_id REGEXP '^D[0-9]+$'")) {
            while (rs.next()) {
                String id = rs.getString(1);
                try { max = Math.max(max, Integer.parseInt(id.substring(1))); }
                catch (NumberFormatException ignored) { /* Ignore non-numeric suffixes. */ }
            }
        }
        return String.format("D%03d", max + 1);
    }
}
