package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping OrderService integration test");
        service = new OrderService();
    }

    @Test
    void loadsClientsFromDatabase() throws Exception {
        List<Client> clients = service.loadClients();
        assertFalse(clients.isEmpty(), "seed data should contain clients");
        assertTrue(clients.stream().allMatch(c -> !c.getId().isBlank()
                && !c.getName().isBlank()));
    }

    @Test
    void loadsProductsFromDatabase() throws Exception {
        List<Product> products = service.loadProducts();
        assertFalse(products.isEmpty(), "seed data should contain products");
        assertTrue(products.stream().allMatch(p -> p.getPrice() >= 0
                && p.getCategory() != null));
    }

    @Test
    void savesOrderAndReadsItBack() throws Exception {
        Client client = service.loadClients().get(0);
        List<Product> products = service.loadProducts();
        Product first = products.get(0);
        Product second = products.get(Math.min(1, products.size() - 1));

        List<OrderItem> items = List.of(
                new OrderItem(first, 2),
                new OrderItem(second, 1));
        double expectedTotal = 2 * first.getPrice() + second.getPrice();

        Order saved = service.createOrder(client, items);
        try {
            assertNotNull(saved.getOrderId());
            assertEquals(expectedTotal, saved.calculateTotal(), 0.001);

            Order reloaded = service.getOrderHistory().stream()
                    .filter(o -> o.getOrderId().equals(saved.getOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("saved order not found"));

            assertEquals(client.getId(), reloaded.getClient().getId());
            assertEquals(items.size(), reloaded.getItems().size());
            assertEquals(expectedTotal, reloaded.calculateTotal(), 0.001);
        } finally {
            service.deleteOrder(saved.getOrderId());
        }
    }

    @Test
    void orderReservesAndCancelRestoresStock() throws Exception {
        Client client = service.loadClients().get(0);
        Product product = service.loadProducts().stream()
                .filter(p -> serviceStock(p) >= 2)
                .findFirst().orElseThrow(() -> new AssertionError("no stocked product"));

        int before = serviceStock(product);
        Order saved = service.createOrder(client, List.of(new OrderItem(product, 1)));
        try {
            assertEquals(before - 1, serviceStock(product),
                    "creating an order should reserve one unit");

            assertTrue(service.cancelOrder(saved.getOrderId()));
            assertEquals(before, serviceStock(product),
                    "cancelling an order should return the unit");
            assertEquals("CANCELLED", service.getOrderHistory().stream()
                    .filter(o -> o.getOrderId().equals(saved.getOrderId()))
                    .findFirst().orElseThrow().getStatus());
        } finally {
            service.deleteOrder(saved.getOrderId());
        }
    }

    @Test
    void rejectsOrderBeyondAvailableStock() throws Exception {
        Client client = service.loadClients().get(0);
        Product product = service.loadProducts().get(0);
        int before = serviceStock(product);

        assertThrows(IllegalStateException.class,
                () -> service.createOrder(client,
                        List.of(new OrderItem(product, before + 999))));
        assertEquals(before, serviceStock(product),
                "a rejected order must not move stock");
    }

    private int serviceStock(Product product) {
        try {
            return service.loadStockLevels().getOrDefault(
                    product.getProductId(), 0);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    void rejectsEmptyOrders() {
        Client client = new Client("C001", "Nimal Perera");
        assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(client, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(null,
                        List.of(new OrderItem(
                                new GardenKit("P100", "Kit", 100, 3, "", "BEGINNER"), 1))));
    }
}
