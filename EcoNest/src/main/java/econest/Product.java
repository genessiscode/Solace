package econest;

import java.util.Locale;

public abstract class Product {

    private final String productId;

    private final String name;

    private final double price;

    private final int ecoRating;

    private final String description;

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

    public abstract ProductCategory getCategory();

    public final String getProductId() {
        return productId;
    }

    public final String getName() {
        return name;
    }

    public final double getPrice() {
        return price;
    }

    public final int getEcoRating() {
        return ecoRating;
    }

    public final String getDescription() {
        return description;
    }

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
