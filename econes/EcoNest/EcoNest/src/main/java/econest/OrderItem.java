package econest;

public class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double unitPrice;

    public OrderItem(String productId, String productName, int quantity, double unitPrice) {
        if (productId == null || productId.isEmpty()) throw new IllegalArgumentException("Product required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return unitPrice * quantity; }
    public double getSubtotal() { return getLineTotal(); }
}
