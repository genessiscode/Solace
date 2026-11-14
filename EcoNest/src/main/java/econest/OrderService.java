package econest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Database operations for module 5 (Client Orders). Clients and products are
 * read from the shared tables, and created orders are stored in
 * {@code orders} / {@code order_items}.
 */
public class OrderService {

    private static final String SELECT_CLIENTS =
            "SELECT client_id, name FROM clients ORDER BY client_id";

    private static final String SELECT_PRODUCTS =
            "SELECT product_id, name AS product_name, category, price, eco_rating, description, "
                    + "sunlight, pet_safe, material, diameter_cm, difficulty "
                    + "FROM products ORDER BY product_id";

    private static final String SELECT_HISTORY =
            "SELECT o.order_id, o.order_date, c.client_id, c.name AS client_name, "
                    + "oi.quantity, oi.unit_price, "
                    + "p.product_id, p.name AS product_name, p.category, "
                    + "p.eco_rating, p.description, p.sunlight, p.pet_safe, "
                    + "p.material, p.diameter_cm, p.difficulty "
                    + "FROM orders o "
                    + "JOIN clients c ON c.client_id = o.client_id "
                    + "JOIN order_items oi ON oi.order_id = o.order_id "
                    + "JOIN products p ON p.product_id = oi.product_id "
                    + "ORDER BY o.order_date, o.order_id, oi.item_id";

    public List<Client> loadClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CLIENTS);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                clients.add(new Client(
                        result.getString("client_id"),
                        result.getString("name")));
            }
        }
        return clients;
    }

    public List<Product> loadProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_PRODUCTS);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                products.add(mapProduct(result, result.getDouble("price")));
            }
        }
        return products;
    }

    /**
     * Stores the order and its items in a single transaction and returns the
     * saved order with the generated order ID.
     */
    public Order createOrder(Client client, List<OrderItem> items) throws SQLException {
        if (client == null) {
            throw new IllegalArgumentException("Choose a client before creating an order.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item.");
        }

        List<OrderItem> snapshot = new ArrayList<>(items);
        double total = 0;
        for (OrderItem item : snapshot) total += item.getSubtotal();

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String orderId = nextOrderId(connection);
                insertOrder(connection, orderId, client, total);
                insertItems(connection, orderId, snapshot);
                connection.commit();
                return new Order(orderId, client, snapshot);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Order> getOrderHistory() throws SQLException {
        Map<String, OrderBuilder> builders = new LinkedHashMap<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_HISTORY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String orderId = result.getString("order_id");
                OrderBuilder builder = builders.get(orderId);
                if (builder == null) {
                    builder = new OrderBuilder(
                            orderId,
                            new Client(result.getString("client_id"),
                                    result.getString("client_name")),
                            result.getDate("order_date").toLocalDate().atStartOfDay());
                    builders.put(orderId, builder);
                }
                builder.items.add(new OrderItem(
                        mapProduct(result, result.getDouble("unit_price")),
                        result.getInt("quantity")));
            }
        }

        List<Order> orders = new ArrayList<>();
        for (OrderBuilder builder : builders.values()) {
            orders.add(new Order(builder.orderId, builder.client,
                    builder.items, builder.createdAt));
        }
        return orders;
    }

    /**
     * Removes an order and its items. Used by tests and by future order
     * cancellation screens.
     */
    public void deleteOrder(String orderId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM orders WHERE order_id = ?")) {
            statement.setString(1, orderId);
            statement.executeUpdate();
        }
    }

    private String nextOrderId(Connection connection) throws SQLException {
        int next = 1;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT order_id FROM orders ORDER BY order_id")) {
            while (result.next()) {
                int number = trailingNumber(result.getString("order_id"));
                if (number >= next) next = number + 1;
            }
        }
        return String.format("O%03d", next);
    }

    private void insertOrder(Connection connection, String orderId, Client client,
                             double total) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (order_id, client_id, order_date, status, total_amount) "
                        + "VALUES (?, ?, ?, 'PENDING', ?)")) {
            statement.setString(1, orderId);
            statement.setString(2, client.getId());
            statement.setDate(3, Date.valueOf(LocalDate.now()));
            statement.setDouble(4, total);
            statement.executeUpdate();
        }
    }

    private void insertItems(Connection connection, String orderId,
                             List<OrderItem> items) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO order_items "
                        + "(order_id, product_id, quantity, unit_price, line_total) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            for (OrderItem item : items) {
                statement.setString(1, orderId);
                statement.setString(2, item.getProduct().getProductId());
                statement.setInt(3, item.getQuantity());
                statement.setDouble(4, item.getProduct().getPrice());
                statement.setDouble(5, item.getSubtotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Product mapProduct(ResultSet result, double price) throws SQLException {
        String id = result.getString("product_id");
        String name = result.getString("product_name");
        int ecoRating = result.getInt("eco_rating");
        String description = result.getString("description");
        String category = result.getString("category");

        if ("PLANTER".equals(category)) {
            int diameter = result.getInt("diameter_cm");
            return new Planter(id, name, price, ecoRating, description,
                    result.getString("material"), diameter);
        }
        if ("KIT".equals(category)) {
            return new GardenKit(id, name, price, ecoRating, description,
                    result.getString("difficulty"));
        }
        return new Plant(id, name, price, ecoRating, description,
                result.getString("sunlight"), result.getBoolean("pet_safe"));
    }

    private int trailingNumber(String orderId) {
        int index = orderId.length();
        while (index > 0 && Character.isDigit(orderId.charAt(index - 1))) index--;
        if (index == orderId.length()) return 0;
        try {
            return Integer.parseInt(orderId.substring(index));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static final class OrderBuilder {
        private final String orderId;
        private final Client client;
        private final java.time.LocalDateTime createdAt;
        private final List<OrderItem> items = new ArrayList<>();

        private OrderBuilder(String orderId, Client client,
                             java.time.LocalDateTime createdAt) {
            this.orderId = orderId;
            this.client = client;
            this.createdAt = createdAt;
        }
    }
}
