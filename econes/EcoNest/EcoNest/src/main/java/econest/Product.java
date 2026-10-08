package econest;

import java.util.Locale;

/**
 * Base class for anything EcoNest sells: plants, planters and gardening kits.
 *
 * <p>Holds the attributes every product has, and validates them on
 * construction so an invalid product can never reach the database. Callers
 * catch {@link IllegalArgumentException} and report it to the user.</p>
 */
public abstract class Product {

    private final String productId;

    private final String name;

    private final double price;

    private final int ecoRating;

    private final String description;

    /**
     * @param productId   the catalogue identifier, for example {@code P001}
     * @param name        the display name
     * @param price       the selling price in LKR; must be finite and not negative
     * @param ecoRating   the sustainability rating, 1 to 5
     * @param description an optional one-line description
     * @throws IllegalArgumentException if any argument is out of range
     */
    protected Product(
            String productId,
            String name,
            double price,
            int ecoRating,
            String description) {

        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product ID cannot be empty.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }

        if (Double.isNaN(price) || Double.isInfinite(price)) {
            throw new IllegalArgumentException(
                    "Price must be a real number, was " + price + ".");
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative, was " + price + ".");
        }

        if (ecoRating < 1 || ecoRating > 5) {
            throw new IllegalArgumentException(
                    "Eco rating must be between 1 and 5, was "
                            + ecoRating + ".");
        }

        this.productId = productId.trim();
        this.name = name.trim();
        this.price = price;
        this.ecoRating = ecoRating;
        this.description = description == null ? "" : description.trim();
    }

    /**
     * @return which kind of product this is
     */
    public abstract ProductCategory getCategory();

    /**
     * @return the catalogue identifier
     */
    public final String getProductId() {
        return productId;
    }

    /**
     * @return the display name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the selling price in LKR
     */
    public final double getPrice() {
        return price;
    }

    /**
     * @return the sustainability rating, 1 to 5
     */
    public final int getEcoRating() {
        return ecoRating;
    }

    /**
     * @return the description, or an empty string when none was given
     */
    public final String getDescription() {
        return description;
    }

    /**
     * A one-line label for table cells and combo boxes.
     *
     * @return text of the form {@code P001 - Snake Plant (Rs. 2500.00)}
     */
    public String getDisplayInfo() {

        return String.format(
                Locale.ROOT,
                "%s - %s (Rs. %.2f)",
                productId,
                name,
                price
        );
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}
