package econest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String orderId;
    private final Client client;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;

    public Order(String orderId, Client client, List<OrderItem> items) {
        this(orderId, client, items, LocalDateTime.now());
    }

    public Order(String orderId, Client client, List<OrderItem> items,
                 LocalDateTime createdAt) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("An order needs an ID.");
        }
        if (client == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs a client and at least one item.");
        }
        this.orderId = orderId.trim();
        this.client = client;
        this.createdAt = createdAt == null
                ? LocalDateTime.now()
                : createdAt.truncatedTo(ChronoUnit.SECONDS);
        this.items = new ArrayList<>(items);
    }

    public String getOrderId() { return orderId; }
    public Client getClient() { return client; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) total += item.getSubtotal();
        return total;
    }
}
