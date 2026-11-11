package econest;

/**
 * One row of stock held for a catalogue product.
 *
 * <p>Quantities and reorder levels are validated on construction, and the
 * optional supplier is normalised to an empty string so callers never need
 * to null-check it.</p>
 */
public class Inventory {

    private final String productId;
    private final String productName;
    private final int reorderLevel;
    private final String supplierName;

    private int quantity;

    public Inventory(
            String productId,
            String productName,
            int quantity,
            int reorderLevel) {

        this(productId, productName, quantity, reorderLevel, "");
    }

    public Inventory(
            String productId,
            String productName,
            int quantity,
            int reorderLevel,
            String supplierName) {

        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product ID cannot be empty.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative, was " + quantity + ".");
        }

        if (reorderLevel < 0) {
            throw new IllegalArgumentException(
                    "Reorder level cannot be negative, was "
                            + reorderLevel + ".");
        }

        this.productId = productId.trim();
        this.productName = productName.trim();
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.supplierName = supplierName == null ? "" : supplierName.trim();
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public String getSupplierName() {
        return supplierName;
    }

    /**
     * @return how many units are missing before the reorder level is reached
     */
    public int getStockGap() {
        return Math.max(0, reorderLevel - quantity);
    }

    public void addStock(int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Stock-in amount must be greater than zero.");
        }

        quantity += amount;
    }

    public boolean removeStock(int amount) {

        if (amount <= 0 || amount > quantity) {
            return false;
        }

        quantity -= amount;
        return true;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    @Override
    public String toString() {
        return productId + " - " + productName + " (" + quantity + " in stock)";
    }
}
