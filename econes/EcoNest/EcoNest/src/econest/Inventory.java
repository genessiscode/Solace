package econest;

public class Inventory {

    private String productId;
    private String productName;
    private int quantity;
    private int reorderLevel;

    public Inventory(
            String productId,
            String productName,
            int quantity,
            int reorderLevel) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    // Get Product ID
    public String getProductId() {
        return productId;
    }

    // Get Product Name
    public String getProductName() {
        return productName;
    }

    // Get Quantity
    public int getQuantity() {
        return quantity;
    }

    // Get Reorder Level
    public int getReorderLevel() {
        return reorderLevel;
    }

    // Add stock
    public void addStock(int amount) {

        if (amount > 0) {
            quantity = quantity + amount;
        }
    }

    // Remove stock
    public boolean removeStock(int amount) {

        if (amount <= 0) {
            return false;
        }

        if (amount > quantity) {
            return false;
        }

        quantity = quantity - amount;

        return true;
    }

    // Check low stock
    public boolean isLowStock() {

        return quantity <= reorderLevel;
    }
}