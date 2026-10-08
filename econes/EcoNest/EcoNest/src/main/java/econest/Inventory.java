package econest;

/**
 * One product's stock position.
 *
 * <p>After the products/inventory split this class no longer owns the product
 * name: {@code productName} is a display-only value filled in from the
 * {@code products} table by {@link InventoryManager}, so the user interface
 * keeps working unchanged.</p>
 */
public class Inventory {

    private String productId;
    private String productName;
    private int quantity;
    private int reorderLevel;
    private String supplierName;

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

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.supplierName = supplierName == null ? "" : supplierName;
    }

    // Get Product ID
    public String getProductId() {
        return productId;
    }

    // Get Product Name (display-only, from the products table)
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

    // Get Supplier Name
    public String getSupplierName() {
        return supplierName;
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
