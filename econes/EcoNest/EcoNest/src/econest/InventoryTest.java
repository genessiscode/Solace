package econest;

public class InventoryTest {

    public static void main(String[] args) {

        Inventory item = new Inventory(
                "P001",
                "Snake Plant",
                10,
                5
        );

        System.out.println("Product ID: "
                + item.getProductId());

        System.out.println("Product Name: "
                + item.getProductName());

        System.out.println("Current Stock: "
                + item.getQuantity());

        System.out.println("Reorder Level: "
                + item.getReorderLevel());

        // Add stock
        item.addStock(20);

        System.out.println(
                "After Stock-In: "
                        + item.getQuantity()
        );

        // Remove stock
        boolean removed =
                item.removeStock(10);

        System.out.println(
                "Stock Removed: "
                        + removed
        );

        System.out.println(
                "After Stock Removal: "
                        + item.getQuantity()
        );

        // Check low stock
        if (item.isLowStock()) {

            System.out.println(
                    "LOW STOCK!"
            );

        } else {

            System.out.println(
                    "Stock is OK."
            );
        }

        // Try to remove more stock than available
        boolean invalidRemoval =
                item.removeStock(100);

        System.out.println(
                "Invalid Stock Removal: "
                        + invalidRemoval
        );

        System.out.println(
                "Final Stock: "
                        + item.getQuantity()
        );
    }
}