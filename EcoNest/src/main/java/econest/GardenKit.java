package econest;

/**
 * A small-space gardening kit. Adds the difficulty rating only kits have.
 */
public class GardenKit extends Product {

    private final String difficulty;

    /**
     * @param difficulty the experience level required, for example {@code BEGINNER}
     */
    public GardenKit(
            String productId,
            String name,
            double price,
            int ecoRating,
            String description,
            String difficulty) {

        super(productId, name, price, ecoRating, description);

        this.difficulty = difficulty == null ? "" : difficulty.trim();
    }

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.KIT;
    }

    /**
     * @return the experience level required, or an empty string when unset
     */
    public String getDifficulty() {
        return difficulty;
    }
}
