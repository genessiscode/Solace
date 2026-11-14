package econest;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Product product(double price) {
        return new GardenKit("P100", "Herb Kit", price, 4, "Starter kit", "BEGINNER");
    }

    @Test
    void orderStoresClientItemsAndTimestamp() {
        Client client = new Client("C001", "Nimal Perera");
        LocalDateTime created = LocalDateTime.of(2026, 10, 2, 9, 30);
        Order order = new Order("O010", client,
                List.of(new OrderItem(product(1200), 2)), created);

        assertEquals("O010", order.getOrderId());
        assertEquals(client, order.getClient());
        assertEquals(created, order.getCreatedAt());
        assertEquals(1, order.getItems().size());
        assertEquals(2400.00, order.calculateTotal(), 0.001);
    }

    @Test
    void orderRejectsMissingClientOrItems() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("O011", null, List.of(new OrderItem(product(100), 1))));
        assertThrows(IllegalArgumentException.class,
                () -> new Order("O012", new Client("C001", "Nimal"), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Order("  ", new Client("C001", "Nimal"),
                        List.of(new OrderItem(product(100), 1))));
    }

    @Test
    void orderItemValidatesQuantityAndComputesSubtotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(product(500), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(null, 1));

        OrderItem item = new OrderItem(product(750), 3);
        assertEquals(2250.00, item.getSubtotal(), 0.001);
    }

    @Test
    void clientRequiresIdentifierAndName() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null, "Nimal"));
        assertThrows(IllegalArgumentException.class, () -> new Client("C001", " "));
        assertEquals("C001", new Client(" C001 ", "Nimal").getId());
    }
}
