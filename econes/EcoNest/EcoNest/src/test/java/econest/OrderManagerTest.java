package econest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OrderManagerTest {

    private OrderManager manager;

    /** The order created by the current test, cleaned up after it. */
    private String createdOrderId;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping OrderManager integration test");
        manager = new OrderManager();
        createdOrderId = null;
    }

    @AfterEach
    void cleanUp() {
        if (manager != null && createdOrderId != null) {
            try {
                manager.removeOrder(createdOrderId);
            } catch (RuntimeException ignored) {
                // nothing to clean up
            }
        }
    }

    /** A unique id short enough for the VARCHAR(20) orders.order_id. */
    private String uniqueOrderId() {
        return "TEST-O-" + (System.nanoTime() % 10_000_000_000L);
    }

    /** Finds the order in the list, or fails the assertion. */
    private Order findInList(String orderId) {
        for (Order order : manager.getOrderList()) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }

        fail("order " + orderId + " should appear in getOrderList");
        return null;
    }

    @Test
    void roundTripsAnOrderWithItsItems() {
        createdOrderId = uniqueOrderId();

        ArrayList<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", "Snake Plant premium", 2, 2500.00));
        items.add(new OrderItem("P002", "Aloe Vera", 1, 1200.00));

        Order order = new Order(createdOrderId, "C001", "Nimal Perera",
                LocalDate.now(), OrderStatus.PENDING, 0.0, items);

        manager.createOrder(order);

        assertEquals(6200.00, order.getTotalAmount(), 0.001,
                "createOrder should set the sum of the line totals back on the order");

        Order listed = findInList(createdOrderId);

        assertEquals("C001", listed.getClientId());
        assertEquals("Nimal Perera", listed.getClientName(),
                "client name should come from the clients JOIN");
        assertEquals(OrderStatus.PENDING, listed.getStatus());
        assertEquals(6200.00, listed.getTotalAmount(), 0.001);
        assertEquals(LocalDate.now(), listed.getOrderDate());

        ArrayList<OrderItem> loaded = manager.getOrderItems(createdOrderId);

        assertEquals(2, loaded.size());

        double sum = 0.0;

        for (OrderItem item : loaded) {
            assertEquals(item.getQuantity() * item.getUnitPrice(),
                    item.getLineTotal(), 0.001,
                    "line total should be quantity * unitPrice");
            sum += item.getLineTotal();
        }

        assertEquals(6200.00, sum, 0.001,
                "the stored lines should add up to the order total");
        assertEquals("Snake Plant premium", loaded.get(0).getProductName(),
                "product name should come from the products JOIN");

        manager.updateOrderStatus(createdOrderId, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED,
                findInList(createdOrderId).getStatus(),
                "the status update should be visible in the list");

        assertTrue(manager.removeOrder(createdOrderId));
        assertNull(findInListOrNull(createdOrderId));
        createdOrderId = null;
    }

    @Test
    void creatingAnOrderWithNoItemsIsRejected() {
        String orderId = uniqueOrderId();

        Order empty = new Order(orderId, "C001", "Nimal Perera",
                LocalDate.now(), OrderStatus.PENDING, 0.0,
                new ArrayList<>());

        assertThrows(IllegalArgumentException.class,
                () -> manager.createOrder(empty),
                "an order without line items must be rejected");

        assertNull(findInListOrNull(orderId),
                "a rejected order must not reach the database");
    }

    @Test
    void updatingTheStatusOfAMissingOrderThrows() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> manager.updateOrderStatus("DOES-NOT-EXIST",
                        OrderStatus.DELIVERED));

        assertTrue(error.getMessage().contains("DOES-NOT-EXIST"),
                "message should name the missing order, was: "
                        + error.getMessage());
    }

    @Test
    void removingAnAbsentOrderReportsFalse() {
        assertFalse(manager.removeOrder("DOES-NOT-EXIST"));
    }

    @Test
    void clientAndProductOptionsListTheSeededData() {
        boolean clientFound = false;

        for (String[] client : manager.getClientOptions()) {
            if ("C001".equals(client[0])) {
                clientFound = true;
                assertEquals("Nimal Perera", client[1]);
            }
        }

        assertTrue(clientFound, "seeded client C001 should be offered");

        boolean productFound = false;

        for (String[] product : manager.getProductOptions()) {
            if ("P001".equals(product[0])) {
                productFound = true;
                assertEquals("Snake Plant premium", product[1]);
                assertEquals(2500.00, Double.parseDouble(product[2]), 0.001);
            }
        }

        assertTrue(productFound, "seeded product P001 should be offered");
    }

    @Test
    void ordersAreListedNewestFirstWithClientNames() {
        ArrayList<Order> orders = manager.getOrderList();

        assertFalse(orders.isEmpty(), "schema.sql should have loaded orders");

        for (Order order : orders) {
            assertFalse(order.getClientName().isEmpty(),
                    "every order should carry the joined client name");
        }

        for (int i = 1; i < orders.size(); i++) {
            assertFalse(orders.get(i - 1).getOrderDate()
                            .isBefore(orders.get(i).getOrderDate()),
                    "orders should be listed newest date first");
        }
    }

    /** Finds the order in the list, or {@code null} when it is absent. */
    private Order findInListOrNull(String orderId) {
        for (Order order : manager.getOrderList()) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }

        return null;
    }
}
