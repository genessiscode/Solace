package econest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String orderId;
    private final String clientId;
    private final String clientName;
    private final LocalDate orderDate;
    private final OrderStatus status;
    private double totalAmount;
    private final List<OrderItem> items;

    public Order(String orderId, String clientId, String clientName,
                 LocalDate orderDate, OrderStatus status, double totalAmount,
                 List<OrderItem> items) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public String getOrderId() { return orderId; }
    public String getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public LocalDate getOrderDate() { return orderDate; }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    /**
     * Sums the line totals of this order's items.
     *
     * <p>Also stores the result on the order, so the in-memory object agrees
     * with the {@code orders.total_amount} column that
     * {@link OrderManager#createOrder(Order)} writes from this same
     * value.</p>
     *
     * @return the order total
     */
    public double recalculateTotal() {
        double sum = 0;
        for (OrderItem i : items) sum += i.getSubtotal();
        this.totalAmount = sum;
        return sum;
    }
}
