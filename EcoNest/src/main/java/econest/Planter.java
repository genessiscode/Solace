package econest;

public class Planter extends Product {

    private final String material;

    private final int diameterCm;

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

    public String getMaterial() {
        return material;
    }

    public int getDiameterCm() {
        return diameterCm;
    }
}
