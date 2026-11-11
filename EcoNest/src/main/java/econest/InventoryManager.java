package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Database access for stock records.
 *
 * <p>Every stock row belongs to one catalogue product through the
 * {@code inventory.product_id} foreign key. The product name shown in the UI
 * always comes from the {@code products} table, so it cannot drift away from
 * the catalogue.</p>
 */
public class InventoryManager {

    private static final String SELECT_COLUMNS =
            "SELECT i.product_id, p.name AS product_name, i.quantity,"
                    + " i.reorder_level, i.supplier_name"
                    + " FROM inventory i"
                    + " JOIN products p ON p.product_id = i.product_id";

    // ================= INVENTORY CRUD =================

    public void addInventory(Inventory inventory) {

        String sql = "INSERT INTO inventory "
                + "(product_id, quantity, reorder_level, supplier_name) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, inventory.getProductId());
            statement.setInt(2, inventory.getQuantity());
            statement.setInt(3, inventory.getReorderLevel());
            statement.setString(4, inventory.getSupplierName());

            statement.executeUpdate();

        } catch (SQLException e) {

            if (isDuplicateKey(e)) {
                throw new IllegalStateException(
                        "Inventory already exists for product "
                                + inventory.getProductId() + ".", e);
            }

            if (isConstraintViolation(e)) {
                throw new IllegalStateException(
                        "Product " + inventory.getProductId()
                                + " does not exist in the catalogue.", e);
            }

            throw new IllegalStateException(
                    "Could not add inventory for product "
                            + inventory.getProductId() + ": "
                            + e.getMessage(), e);
        }
    }

    public void updateInventory(String productId, Inventory updatedItem) {

        String sql = "UPDATE inventory SET "
                + "quantity = ?, reorder_level = ?, supplier_name = ? "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, updatedItem.getQuantity());
            statement.setInt(2, updatedItem.getReorderLevel());
            statement.setString(3, updatedItem.getSupplierName());
            statement.setString(4, productId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "No inventory record found for product "
                                + productId + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update inventory for product " + productId
                            + ": " + e.getMessage(), e);
        }
    }

    public boolean removeInventory(String productId) {

        String sql = "DELETE FROM inventory WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isConstraintViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove inventory for product " + productId
                            + ": " + e.getMessage(), e);
        }
    }

    public Inventory findProduct(String productId) {

        String sql = SELECT_COLUMNS + " WHERE i.product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read inventory for product " + productId
                            + ": " + e.getMessage(), e);
        }

        return null;
    }

    public ArrayList<Inventory> getInventoryList() {

        ArrayList<Inventory> items = new ArrayList<>();

        String sql = SELECT_COLUMNS + " ORDER BY i.product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                items.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the inventory list: " + e.getMessage(), e);
        }

        return items;
    }

    public ArrayList<Inventory> getLowStockItems() {

        ArrayList<Inventory> items = new ArrayList<>();

        String sql = SELECT_COLUMNS
                + " WHERE i.quantity <= i.reorder_level"
                + " ORDER BY (i.reorder_level - i.quantity) DESC,"
                + " i.product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                items.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read low-stock items: " + e.getMessage(), e);
        }

        return items;
    }

    // ================= STOCK MOVEMENTS =================

    public void addStock(String productId, int amount, String supplierName) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Stock-in amount must be greater than zero.");
        }

        if (supplierName == null || supplierName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Supplier name cannot be empty.");
        }

        if (findProduct(productId) == null) {
            throw new IllegalArgumentException(
                    "Product " + productId + " was not found.");
        }

        String sql = "UPDATE inventory SET "
                + "quantity = quantity + ?, supplier_name = ? "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amount);
            statement.setString(2, supplierName.trim());
            statement.setString(3, productId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not add stock for product " + productId
                            + ": " + e.getMessage(), e);
        }
    }

    public void removeStock(String productId, int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Stock-out amount must be greater than zero.");
        }

        String sql = "UPDATE inventory SET quantity = quantity - ? "
                + "WHERE product_id = ? AND quantity >= ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amount);
            statement.setString(2, productId);
            statement.setInt(3, amount);

            if (statement.executeUpdate() == 0) {

                Inventory item = findProduct(productId);

                if (item == null) {
                    throw new IllegalArgumentException(
                            "Product " + productId + " was not found.");
                }

                throw new IllegalArgumentException(
                        "Not enough stock for " + item.getProductName()
                                + " (available: " + item.getQuantity()
                                + ", requested: " + amount + ").");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not remove stock for product " + productId
                            + ": " + e.getMessage(), e);
        }
    }

    // ================= PRODUCT CHOICES =================

    /**
     * Lists every catalogue product so the UI can pick the product whose
     * stock is being managed.
     */
    public ArrayList<ProductChoice> getProductChoices() {

        ArrayList<ProductChoice> choices = new ArrayList<>();

        String sql = "SELECT product_id, name FROM products ORDER BY product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                choices.add(new ProductChoice(
                        resultSet.getString("product_id"),
                        resultSet.getString("name")));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read catalogue products: " + e.getMessage(), e);
        }

        return choices;
    }

    /**
     * A catalogue product as shown in the stock form.
     */
    public static final class ProductChoice {

        private final String productId;
        private final String productName;

        public ProductChoice(String productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        @Override
        public String toString() {
            return productId + " — " + productName;
        }
    }

    // ================= HELPERS =================

    private static Inventory mapRow(ResultSet resultSet)
            throws SQLException {

        return new Inventory(
                resultSet.getString("product_id"),
                resultSet.getString("product_name"),
                resultSet.getInt("quantity"),
                resultSet.getInt("reorder_level"),
                resultSet.getString("supplier_name")
        );
    }

    private static boolean isConstraintViolation(SQLException e) {
        return "23000".equals(e.getSQLState());
    }

    private static boolean isDuplicateKey(SQLException e) {
        return isConstraintViolation(e)
                && e.getMessage() != null
                && e.getMessage().toLowerCase().contains("duplicate");
    }
}
