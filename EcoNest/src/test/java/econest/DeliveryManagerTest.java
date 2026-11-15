package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryManagerTest {

    private final DeliveryManager manager = new DeliveryManager();

    private boolean databaseIsAvailable() {
        try (Connection ignored = DeliveryDBConnection.getConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private String uniqueId(String prefix) {
        return prefix + Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private void insertTestRecords(String clientId, String orderId, String agentId)
            throws SQLException {
        try (Connection con = DeliveryDBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO clients (client_id, name, client_type) VALUES (?, ?, 'HOUSEHOLD')")) {
                    ps.setString(1, clientId);
                    ps.setString(2, "Delivery Test Client");
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO orders (order_id, client_id, order_date, status, total_amount) " +
                                "VALUES (?, ?, ?, 'PENDING', 100.00)")) {
                    ps.setString(1, orderId);
                    ps.setString(2, clientId);
                    ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO agents (agent_id, name, status) VALUES (?, ?, 'AVAILABLE')")) {
                    ps.setString(1, agentId);
                    ps.setString(2, "Delivery Test Agent");
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private void cleanUp(String clientId, String orderId, String agentId) {
        try (Connection con = DeliveryDBConnection.getConnection()) {
            // Delete in foreign-key order. The delivery rows reference both order and agent.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM deliveries WHERE order_id=? OR agent_id=?")) {
                ps.setString(1, orderId);
                ps.setString(2, agentId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM orders WHERE order_id=?")) {
                ps.setString(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM agents WHERE agent_id=?")) {
                ps.setString(1, agentId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM clients WHERE client_id=?")) {
                ps.setString(1, clientId);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
            // Keep a test failure focused on the operation being tested.
        }
    }

    @Test
    void rejectsMissingAssignmentInformationBeforeAccessingDatabase() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.assignDelivery(null, "A001", LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> manager.assignDelivery("O001", " ", LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> manager.assignDelivery("O001", "A001", null));
    }

    @Test
    void rejectsAnUnknownDeliveryStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.updateDeliveryStatus("D001", "UNKNOWN"));
    }

    @Test
    void assignsDeliveryUpdatesOrderAndAgentAndCanCompleteDelivery() throws SQLException {
        Assumptions.assumeTrue(databaseIsAvailable(),
                "MySQL database is not reachable - skipping DeliveryManager integration test");
        String clientId = uniqueId("TC");
        String orderId = uniqueId("TO");
        String agentId = uniqueId("TA");
        LocalDate scheduledDate = LocalDate.now().plusDays(1);

        try {
            insertTestRecords(clientId, orderId, agentId);

            List<String[]> assignableOrders = manager.getAssignableOrders();
            assertTrue(assignableOrders.stream().anyMatch(row -> orderId.equals(row[0])));

            List<String[]> availableAgents = manager.getAvailableAgents();
            assertTrue(availableAgents.stream().anyMatch(row -> agentId.equals(row[0])));

            manager.assignDelivery(orderId, agentId, scheduledDate);

            List<Delivery> deliveries = manager.getAllDeliveries();
            Delivery created = deliveries.stream()
                    .filter(delivery -> orderId.equals(delivery.getOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("The new delivery should be listed"));

            assertEquals("Delivery Test Client", created.getClientName());
            assertEquals(agentId, created.getAgentId());
            assertEquals("Delivery Test Agent", created.getAgentName());
            assertEquals(scheduledDate, created.getScheduledDate());
            assertEquals("ASSIGNED", created.getStatus());

            manager.updateDeliveryStatus(created.getDeliveryId(), "DELIVERED");

            Delivery completed = manager.getAllDeliveries().stream()
                    .filter(delivery -> created.getDeliveryId().equals(delivery.getDeliveryId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("The completed delivery should remain listed"));
            assertEquals("DELIVERED", completed.getStatus());

            try (Connection con = DeliveryDBConnection.getConnection();
                 PreparedStatement orderQuery = con.prepareStatement(
                         "SELECT status FROM orders WHERE order_id=?");
                 PreparedStatement agentQuery = con.prepareStatement(
                         "SELECT status FROM agents WHERE agent_id=?")) {
                orderQuery.setString(1, orderId);
                try (var rs = orderQuery.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("DELIVERED", rs.getString("status"));
                }
                agentQuery.setString(1, agentId);
                try (var rs = agentQuery.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("AVAILABLE", rs.getString("status"));
                }
            }
        } finally {
            cleanUp(clientId, orderId, agentId);
        }
    }
}
