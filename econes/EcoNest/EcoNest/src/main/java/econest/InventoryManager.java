package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access for stock levels.
 *
 * <p>Product names live in the {@code products} table, so every read joins
 * across. Writes touch only the {@code inventory} table.</p>
 */
public class InventoryManager {

    private static final String SELECT_WITH_PRODUCT =
            "SELECT i.product_id, p.name AS product_name, i.quantity,"
                    + " i.reorder_level, i.supplier_name "
                    + "FROM inventory i "
                    + "JOIN products p ON p.product_id = i.product_id";

    /**
     * Inserts a stock row.
     *
     * <p>TEMPORARY BRIDGE - see the Foundation design, section 9.2. The
     * {@code inventory} table has a foreign key to {@code products}, but the
     * Product Catalogue screen does not exist yet, so this method first
     * inserts a minimal placeholder product row. Spec 2 removes this bridge
     * and requires products to be created through the catalogue.</p>
     *
     * @param inventory the stock row to insert
     */
    // TODO(Spec 2): delete the placeholder product insert once the
    //               Product Catalogue screen exists.
    public void addInventory(Inventory inventory) {

        String insertProduct = "INSERT IGNORE INTO products "
                + "(product_id, name, category, price, eco_rating, description) "
                + "VALUES (?, ?, 'PLANT', 0.00, 3, '')";

        String insertStock = "INSERT INTO inventory "
                + "(product_id, quantity, reorder_level, supplier_name) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            try (PreparedStatement statement =
                         connection.prepareStatement(insertProduct)) {

                statement.setString(1, inventory.getProductId());
                statement.setString(2, inventory.getProductName());

                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(insertStock)) {

                statement.setString(1, inventory.getProductId());
                statement.setInt(2, inventory.getQuantity());
                statement.setInt(3, inventory.getReorderLevel());
                statement.setString(4, inventory.getSupplierName());

                statement.executeUpdate();
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not add stock row for "
                            + inventory.getProductId() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Reads every stock row, with the product name joined in.
     *
     * @return all stock rows; never {@code null}
     */
    public ArrayList<Inventory> getInventoryList() {

        ArrayList<Inventory> inventoryList = new ArrayList<>();

        String sql = SELECT_WITH_PRODUCT + " ORDER BY i.product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                inventoryList.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read stock levels: " + e.getMessage(),
                    e
            );
        }

        return inventoryList;
    }

    /**
     * Finds one stock row by product id.
     *
     * @param productId the product to look up
     * @return the stock row, or {@code null} when the product has none
     */
    public Inventory findProduct(String productId) {

        String sql = SELECT_WITH_PRODUCT + " WHERE i.product_id = ?";

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
                    "Could not read stock for " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    /**
     * Updates the stock-side columns of an existing row. The product name is
     * not updated here: it lives in the {@code products} table.
     *
     * @param productId   the row to update
     * @param updatedItem the new quantity and reorder level
     */
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

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update stock for " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Deletes a stock row. The product itself is left in the catalogue.
     *
     * @param productId the row to delete
     */
    public void removeInventory(String productId) {

        String sql = "DELETE FROM inventory WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not remove stock row for " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Records a supplier delivery by adding to the quantity on hand.
     *
     * @param productId    the product being stocked in
     * @param amount       how many units arrived; ignored when not positive
     * @param supplierName the supplier the stock came from
     */
    public void addStock(String productId, int amount, String supplierName) {

        String sql = "UPDATE inventory SET "
                + "quantity = quantity + ?, supplier_name = ? "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amount);
            statement.setString(2, supplierName);
            statement.setString(3, productId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not stock in " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Removes units from the quantity on hand.
     *
     * @param productId the product to draw down
     * @param amount    how many units to remove
     */
    public void removeStock(String productId, int amount) {

        String sql = "UPDATE inventory SET quantity = quantity - ? "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, amount);
            statement.setString(2, productId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not remove stock from " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * @return how many stock rows exist
     */
    public int countLowStockItems() {

        String sql = "SELECT COUNT(*) AS total FROM inventory "
                + "WHERE quantity <= reorder_level";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not count low stock items: " + e.getMessage(),
                    e
            );
        }

        return 0;
    }

    /** Builds one {@link Inventory} from a joined row. */
    private static Inventory mapRow(ResultSet resultSet) throws SQLException {

        return new Inventory(
                resultSet.getString("product_id"),
                resultSet.getString("product_name"),
                resultSet.getInt("quantity"),
                resultSet.getInt("reorder_level"),
                resultSet.getString("supplier_name")
        );
    }
}
