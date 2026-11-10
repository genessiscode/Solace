package econest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1;

    public Order createOrder(Client client, List<OrderItem> items) {
        Order order = new Order(nextOrderId++, client, items);
        orders.add(order);
        return order;
    }

    public List<Order> getOrderHistory() {
        return Collections.unmodifiableList(orders);
    }
}
