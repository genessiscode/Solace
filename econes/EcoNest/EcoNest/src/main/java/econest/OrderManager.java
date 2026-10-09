package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Data access for client orders and their line items.
 *
 * <p>Mirrors {@link ProductManager}: plain JDBC, prepared statements only,
 * and every {@link SQLException} wrapped in an
 * {@link IllegalStateException} so no SQL failure ever reaches the UI
 * uncaught.</p>
 *
 * <p>Creating an order is the only transactional operation in EcoNest: the
 * {@code orders} row and every {@code order_items} row are written on one
 * connection with autocommit switched off, and anything short of a full
 * success is rolled back, so a half-written order can never exist. The
 * client and product names are reached by JOIN, because only the ids are
 * stored.</p>
 */
public class OrderManager {

    // ================= CREATE =================

    /**
     * Writes a new order and all of its lines in a single transaction.
     *
     * <p>Each line's total is computed in Java as
     * {@code quantity * unitPrice}, and the order total is the sum of
     * those line totals; the recomputed total is also set back on the
     * order, so callers can read it after the call returns. If any
     * statement fails, everything is rolled back and the order does not
     * exist.</p>
     *
     * @param order the order to create, with at least one line item
     * @throws IllegalArgumentException if the order has no line items
     * @throws IllegalStateException    if the order id already exists or
     *                                  any part of the transaction fails
     */
    public void createOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be empty.");
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must have at least one line item.");
        }

        double total = order.recalculateTotal();

        String orderSql = "INSERT INTO orders "
                + "(order_id, client_id, order_date, status, total_amount) "
                + "VALUES (?, ?, ?, ?, ?)";

        String itemSql = "INSERT INTO order_items "
                + "(order_id, product_id, quantity, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            boolean previousAutoCommit = connection.getAutoCommit();

            try {

                connection.setAutoCommit(false);

                try (PreparedStatement orderStatement =
                             connection.prepareStatement(orderSql)) {

                    orderStatement.setString(1, order.getOrderId());
                    orderStatement.setString(2, order.getClientId());
                    orderStatement.setDate(
                            3,
                            java.sql.Date.valueOf(order.getOrderDate()));
                    orderStatement.setString(4, order.getStatus().name());
                    orderStatement.setDouble(5, total);

                    orderStatement.executeUpdate();
                }

                try (PreparedStatement itemStatement =
                             connection.prepareStatement(itemSql)) {

                    for (OrderItem item : order.getItems()) {

                        itemStatement.setString(1, order.getOrderId());
                        itemStatement.setString(2, item.getProductId());
                        itemStatement.setInt(3, item.getQuantity());
                        itemStatement.setDouble(4, item.getUnitPrice());
                        itemStatement.setDouble(5, item.getLineTotal());

                        itemStatement.executeUpdate();
                    }
                }

                connection.commit();

            } catch (SQLException e) {

                rollbackQuietly(connection, e);

                throw new IllegalStateException(
                        "Could not create order " + order.getOrderId()
                                + ": " + e.getMessage(),
                        e
                );

            } finally {

                restoreAutoCommit(connection, previousAutoCommit);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not create order " + order.getOrderId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    // ================= READ =================

    /**
     * Reads every order with its client's name, newest date first.
     *
     * <p>The line items are <em>not</em> loaded here - call
     * {@link #getOrderItems(String)} for the selected order.</p>
     *
     * @return every order; never {@code null}
     */
    public ArrayList<Order> getOrderList() {

        ArrayList<Order> orders = new ArrayList<>();

        String sql = "SELECT o.order_id, o.client_id, c.name AS client_name,"
                + " o.order_date, o.status, o.total_amount"
                + " FROM orders o"
                + " JOIN clients c ON c.client_id = o.client_id"
                + " ORDER BY o.order_date DESC, o.order_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                orders.add(new Order(
                        resultSet.getString("order_id"),
                        resultSet.getString("client_id"),
                        resultSet.getString("client_name"),
                        resultSet.getDate("order_date").toLocalDate(),
                        OrderStatus.valueOf(resultSet.getString("status")),
                        resultSet.getDouble("total_amount"),
                        null
                ));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the order list: " + e.getMessage(),
                    e
            );
        }

        return orders;
    }

    /**
     * Reads the lines of one order, with each product's name joined in.
     *
     * @param orderId the order whose lines to read
     * @return the order's lines in item order; never {@code null}
     */
    public ArrayList<OrderItem> getOrderItems(String orderId) {

        ArrayList<OrderItem> items = new ArrayList<>();

        String sql = "SELECT oi.product_id, p.name AS product_name,"
                + " oi.quantity, oi.unit_price, oi.line_total"
                + " FROM order_items oi"
                + " JOIN products p ON p.product_id = oi.product_id"
                + " WHERE oi.order_id = ?"
                + " ORDER BY oi.item_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(new OrderItem(
                            resultSet.getString("product_id"),
                            resultSet.getString("product_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("unit_price")
                    ));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the items of order " + orderId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return items;
    }

    /**
     * Reads the client picker entries: {@code {client_id, name}} per row.
     *
     * <p>This is the frame's own SQL against {@code clients}, so the
     * orders module does not depend on the clients module.</p>
     *
     * @return every client's id and name, ordered by id; never
     *         {@code null}
     */
    public ArrayList<String[]> getClientOptions() {

        ArrayList<String[]> options = new ArrayList<>();

        String sql = "SELECT client_id, name FROM clients"
                + " ORDER BY client_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                options.add(new String[]{
                        resultSet.getString("client_id"),
                        resultSet.getString("name")
                });
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the client options: " + e.getMessage(),
                    e
            );
        }

        return options;
    }

    /**
     * Reads the product picker entries:
     * {@code {product_id, name, price}} per row.
     *
     * @return every product's id, name and price, ordered by id; never
     *         {@code null}
     */
    public ArrayList<String[]> getProductOptions() {

        ArrayList<String[]> options = new ArrayList<>();

        String sql = "SELECT product_id, name, price FROM products"
                + " ORDER BY product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                options.add(new String[]{
                        resultSet.getString("product_id"),
                        resultSet.getString("name"),
                        resultSet.getString("price")
                });
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the product options: " + e.getMessage(),
                    e
            );
        }

        return options;
    }

    // ================= UPDATE =================

    /**
     * Moves one order to a new status. Nothing else about the order is
     * touched.
     *
     * @param orderId the order to move
     * @param status  the new status
     * @throws IllegalArgumentException if either argument is blank or
     *                                  {@code null}
     * @throws IllegalStateException    if no such order exists or the
     *                                  update fails
     */
    public void updateOrderStatus(String orderId, OrderStatus status) {

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order ID cannot be empty.");
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be empty.");
        }

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setString(2, orderId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "No order found with ID " + orderId + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update the status of order " + orderId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    // ================= DELETE =================

    /**
     * Deletes an order. Its line items go with it, because
     * {@code order_items.order_id} is {@code ON DELETE CASCADE}.
     *
     * @param orderId the order to delete
     * @return {@code true} if a row was deleted, {@code false} if no such
     *         order exists
     */
    public boolean removeOrder(String orderId) {

        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, orderId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not remove order " + orderId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    // ================= HELPERS =================

    /** Rolls a failed transaction back, keeping the original error. */
    private static void rollbackQuietly(
            Connection connection,
            SQLException original) {

        try {

            connection.rollback();

        } catch (SQLException rollbackFailure) {

            original.addSuppressed(rollbackFailure);
        }
    }

    /**
     * Puts autocommit back the way it was found. The connection is closed
     * by try-with-resources either way, but restoring it keeps the reused
     * pool state predictable.
     */
    private static void restoreAutoCommit(
            Connection connection,
            boolean previousAutoCommit) {

        try {

            connection.setAutoCommit(previousAutoCommit);

        } catch (SQLException ignored) {
            // the connection is about to close - nothing useful to do
        }
    }
}
