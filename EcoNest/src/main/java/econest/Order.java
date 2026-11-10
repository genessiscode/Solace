package econest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final int orderId;
    private final Client client;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;

    public Order(int orderId, Client client, List<OrderItem> items) {
        if (client == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs a client and at least one item.");
        }
        this.orderId = orderId;
        this.client = client;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>(items);
    }

    public int getOrderId() { return orderId; }
    public Client getClient() { return client; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) total += item.getSubtotal();
        return total;
    }
}
