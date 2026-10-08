package econest;

import java.sql.*;
import java.util.ArrayList;

public class InventoryManager {

    // ================= ADD PRODUCT =================

    public void addInventory(Inventory inventory) {

        String sql =
                "INSERT INTO inventory " +
                        "(product_id, product_name, quantity, reorder_level, supplier_name) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    inventory.getProductId()
            );

            statement.setString(
                    2,
                    inventory.getProductName()
            );

            statement.setInt(
                    3,
                    inventory.getQuantity()
            );

            statement.setInt(
                    4,
                    inventory.getReorderLevel()
            );

            statement.setString(
                    5,
                    ""
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // ================= GET ALL PRODUCTS =================

    public ArrayList<Inventory> getInventoryList() {

        ArrayList<Inventory> inventoryList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM inventory";

        try (Connection connection =
                     DBConnection.getConnection();
             Statement statement =
                     connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery(sql)) {

            while (resultSet.next()) {

                Inventory item =
                        new Inventory(
                                resultSet.getString(
                                        "product_id"
                                ),
                                resultSet.getString(
                                        "product_name"
                                ),
                                resultSet.getInt(
                                        "quantity"
                                ),
                                resultSet.getInt(
                                        "reorder_level"
                                )
                        );

                inventoryList.add(item);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return inventoryList;
    }

    // ================= FIND PRODUCT =================

    public Inventory findProduct(
            String productId) {

        String sql =
                "SELECT * FROM inventory " +
                        "WHERE product_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    productId
            );

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return new Inventory(
                        resultSet.getString(
                                "product_id"
                        ),
                        resultSet.getString(
                                "product_name"
                        ),
                        resultSet.getInt(
                                "quantity"
                        ),
                        resultSet.getInt(
                                "reorder_level"
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    // ================= UPDATE PRODUCT =================

    public void updateInventory(
            String productId,
            Inventory updatedItem) {

        String sql =
                "UPDATE inventory SET " +
                        "product_name = ?, " +
                        "quantity = ?, " +
                        "reorder_level = ? " +
                        "WHERE product_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    updatedItem.getProductName()
            );

            statement.setInt(
                    2,
                    updatedItem.getQuantity()
            );

            statement.setInt(
                    3,
                    updatedItem.getReorderLevel()
            );

            statement.setString(
                    4,
                    productId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // ================= REMOVE PRODUCT =================

    public void removeInventory(
            String productId) {

        String sql =
                "DELETE FROM inventory " +
                        "WHERE product_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    productId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // ================= STOCK-IN =================

    public void addStock(
            String productId,
            int amount,
            String supplierName) {

        String sql =
                "UPDATE inventory SET " +
                        "quantity = quantity + ?, " +
                        "supplier_name = ? " +
                        "WHERE product_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    amount
            );

            statement.setString(
                    2,
                    supplierName
            );

            statement.setString(
                    3,
                    productId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // ================= REMOVE STOCK =================

    public void removeStock(
            String productId,
            int amount) {

        String sql =
                "UPDATE inventory " +
                        "SET quantity = quantity - ? " +
                        "WHERE product_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    amount
            );

            statement.setString(
                    2,
                    productId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}