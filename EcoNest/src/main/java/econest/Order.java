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
    private final String status;

    public Order(String orderId, Client client, List<OrderItem> items) {
        this(orderId, client, items, LocalDateTime.now(), "PENDING");
    }

    public Order(String orderId, Client client, List<OrderItem> items,
                 LocalDateTime createdAt) {
        this(orderId, client, items, createdAt, "PENDING");
    }

    public Order(String orderId, Client client, List<OrderItem> items,
                 LocalDateTime createdAt, String status) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("An order needs an ID.");
        }
        if (client == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs a client and at least one item.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("An order needs a status.");
        }
        this.orderId = orderId.trim();
        this.client = client;
        this.createdAt = createdAt == null
                ? LocalDateTime.now()
                : createdAt.truncatedTo(ChronoUnit.SECONDS);
        this.items = new ArrayList<>(items);
        this.status = status.trim();
    }

    public String getOrderId() { return orderId; }
    public Client getClient() { return client; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public String getStatus() { return status; }

    public boolean isOpen() {
        return "PENDING".equals(status) || "CONFIRMED".equals(status);
    }

    public boolean isCancelled() { return "CANCELLED".equals(status); }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) total += item.getSubtotal();
        return total;
    }
}
