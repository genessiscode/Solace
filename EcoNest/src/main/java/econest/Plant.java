package econest;

/**
 * A living plant. Adds the two attributes only plants have.
 */
public class Plant extends Product {

    private final String sunlight;

    private final boolean petSafe;

    /**
     * @param sunlight the light requirement, for example {@code LOW}, {@code MEDIUM}, {@code HIGH}
     * @param petSafe  whether the plant is safe around pets
     */
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

    /**
     * @return the light requirement, or an empty string when unset
     */
    public String getSunlight() {
        return sunlight;
    }

    /**
     * @return {@code true} if the plant is safe around pets
     */
    public boolean isPetSafe() {
        return petSafe;
    }
}
