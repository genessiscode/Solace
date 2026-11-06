package econest;

public class GardenKit extends Product {

    private final String difficulty;

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

    public String getDifficulty() {
        return difficulty;
    }
}
