package econest;

/**
 * A container or pot. Adds the two attributes only planters have.
 */
public class Planter extends Product {

    private final String material;

    private final int diameterCm;

    /**
     * @param material   the construction material, for example {@code TERRACOTTA}
     * @param diameterCm the diameter in centimetres; must not be negative
     * @throws IllegalArgumentException if {@code diameterCm} is negative
     */
    public Planter(
            String productId,
            String name,
            double price,
            int ecoRating,
            String description,
            String material,
            int diameterCm) {

        super(productId, name, price, ecoRating, description);

        if (diameterCm < 0) {
            throw new IllegalArgumentException(
                    "Planter diameter cannot be negative, was "
                            + diameterCm + ".");
        }

        this.material = material == null ? "" : material.trim();
        this.diameterCm = diameterCm;
    }

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.PLANTER;
    }

    /**
     * @return the construction material, or an empty string when unset
     */
    public String getMaterial() {
        return material;
    }

    /**
     * @return the diameter in centimetres
     */
    public int getDiameterCm() {
        return diameterCm;
    }
}
