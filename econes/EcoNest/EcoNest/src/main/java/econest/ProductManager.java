package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access for the product catalogue.
 *
 * <p>This class owns the only mapping from the {@code products.category}
 * column to a concrete {@link Product} subclass. Adding a new product type
 * later therefore means changing {@link #mapRow(ResultSet)} and adding the
 * subclass - nothing else in the application needs to know.</p>
 */
public class ProductManager {

    private static final String SELECT_COLUMNS =
            "SELECT product_id, name, category, price, eco_rating,"
                    + " description, sunlight, pet_safe, material,"
                    + " diameter_cm, difficulty FROM products";

    /**
     * Inserts a new product.
     *
     * @param product the product to insert
     * @throws IllegalStateException if the id already exists or the insert fails
     */
    public void addProduct(Product product) {

        String sql = "INSERT INTO products "
                + "(product_id, name, category, price, eco_rating, description,"
                + " sunlight, pet_safe, material, diameter_cm, difficulty) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindProduct(statement, product);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not add product " + product.getProductId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Updates every mutable column of an existing product. The product id is
     * the key and is not itself changed.
     *
     * @param product the product carrying the new values
     * @throws IllegalStateException if no row was updated or the update fails
     */
    public void updateProduct(Product product) {

        String sql = "UPDATE products SET "
                + "name = ?, category = ?, price = ?, eco_rating = ?,"
                + " description = ?, sunlight = ?, pet_safe = ?,"
                + " material = ?, diameter_cm = ?, difficulty = ? "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory().name());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getEcoRating());
            statement.setString(5, product.getDescription());
            bindSubtypeColumns(statement, 6, product);
            statement.setString(11, product.getProductId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "No product found with ID "
                                + product.getProductId() + ".");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not update product " + product.getProductId()
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Deletes a product.
     *
     * <p>A product that still has an inventory row cannot be deleted: the
     * foreign key is {@code ON DELETE RESTRICT}. That is reported as
     * {@code false} rather than as an exception, because it is an ordinary
     * outcome the user needs to be told about in plain words.</p>
     *
     * @param productId the product to delete
     * @return {@code true} if a row was deleted, {@code false} if no such
     *         product exists or stock still references it
     */
    public boolean removeProduct(String productId) {

        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isForeignKeyViolation(e)) {
                return false;
            }

            throw new IllegalStateException(
                    "Could not remove product " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Finds one product by id.
     *
     * @param productId the id to look up
     * @return the product, or {@code null} when no such product exists
     */
    public Product findProduct(String productId) {

        String sql = SELECT_COLUMNS + " WHERE product_id = ?";

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
                    "Could not read product " + productId
                            + ": " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    /**
     * Reads the whole catalogue, ordered by product id.
     *
     * @return every product; never {@code null}
     */
    public ArrayList<Product> getProductList() {

        ArrayList<Product> products = new ArrayList<>();

        String sql = SELECT_COLUMNS + " ORDER BY product_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the product catalogue: " + e.getMessage(),
                    e
            );
        }

        return products;
    }

    /**
     * @return how many products the catalogue holds
     */
    public int countProducts() {

        String sql = "SELECT COUNT(*) AS total FROM products";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not count products: " + e.getMessage(),
                    e
            );
        }

        return 0;
    }

    // ================= HELPERS =================

    /**
     * Builds the right subclass for one {@code products} row. This is the only
     * place the category-to-class mapping exists.
     */
    private static Product mapRow(ResultSet resultSet) throws SQLException {

        String productId = resultSet.getString("product_id");
        String name = resultSet.getString("name");
        double price = resultSet.getDouble("price");
        int ecoRating = resultSet.getInt("eco_rating");
        String description = resultSet.getString("description");

        ProductCategory category =
                ProductCategory.valueOf(resultSet.getString("category"));

        switch (category) {

            case PLANT:
                return new Plant(
                        productId, name, price, ecoRating, description,
                        resultSet.getString("sunlight"),
                        resultSet.getBoolean("pet_safe"));

            case PLANTER:
                return new Planter(
                        productId, name, price, ecoRating, description,
                        resultSet.getString("material"),
                        resultSet.getInt("diameter_cm"));

            case KIT:
            default:
                return new GardenKit(
                        productId, name, price, ecoRating, description,
                        resultSet.getString("difficulty"));
        }
    }

    /** Binds all eleven INSERT parameters, in column order. */
    private static void bindProduct(
            PreparedStatement statement,
            Product product) throws SQLException {

        statement.setString(1, product.getProductId());
        statement.setString(2, product.getName());
        statement.setString(3, product.getCategory().name());
        statement.setDouble(4, product.getPrice());
        statement.setInt(5, product.getEcoRating());
        statement.setString(6, product.getDescription());
        bindSubtypeColumns(statement, 7, product);
    }

    /**
     * Binds the five subtype columns starting at {@code firstIndex}:
     * sunlight, pet_safe, material, diameter_cm, difficulty. Columns that do
     * not apply to this product are written as SQL NULL.
     */
    private static void bindSubtypeColumns(
            PreparedStatement statement,
            int firstIndex,
            Product product) throws SQLException {

        String sunlight = null;
        Boolean petSafe = null;
        String material = null;
        Integer diameterCm = null;
        String difficulty = null;

        if (product instanceof Plant) {
            Plant plant = (Plant) product;
            sunlight = plant.getSunlight();
            petSafe = plant.isPetSafe();
        } else if (product instanceof Planter) {
            Planter planter = (Planter) product;
            material = planter.getMaterial();
            diameterCm = planter.getDiameterCm();
        } else if (product instanceof GardenKit) {
            difficulty = ((GardenKit) product).getDifficulty();
        }

        statement.setString(firstIndex, sunlight);

        if (petSafe == null) {
            statement.setNull(firstIndex + 1, java.sql.Types.BOOLEAN);
        } else {
            statement.setBoolean(firstIndex + 1, petSafe);
        }

        statement.setString(firstIndex + 2, material);

        if (diameterCm == null) {
            statement.setNull(firstIndex + 3, java.sql.Types.INTEGER);
        } else {
            statement.setInt(firstIndex + 3, diameterCm);
        }

        statement.setString(firstIndex + 4, difficulty);
    }

    /** MariaDB reports a constraint failure with SQLState 23000. */
    private static boolean isForeignKeyViolation(SQLException e) {
        return "23000".equals(e.getSQLState());
    }
}
