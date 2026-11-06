package econest;

public class Plant extends Product {

    private final String sunlight;

    private final boolean petSafe;

    public Plant(
            String productId,
            String name,
            double price,
            int ecoRating,
            String description,
            String sunlight,
            boolean petSafe) {

        super(productId, name, price, ecoRating, description);

        this.sunlight = sunlight == null ? "" : sunlight.trim();
        this.petSafe = petSafe;
    }

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.PLANT;
    }

    public String getSunlight() {
        return sunlight;
    }

    public boolean isPetSafe() {
        return petSafe;
    }
}
