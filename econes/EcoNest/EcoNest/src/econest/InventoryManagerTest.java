package econest;

public class InventoryManagerTest {

    public static void main(String[] args) {

        InventoryManager manager = new InventoryManager();

        Inventory item1 =
                new Inventory("P001", "Snake Plant", 10, 5);

        Inventory item2 =
                new Inventory("P002", "Aloe Vera", 20, 5);

        manager.addInventory(item1);
        manager.addInventory(item2);

        System.out.println("Total Products: "
                + manager.getInventoryList().size());

        Inventory found = manager.findProduct("P002");

        if (found != null) {
            System.out.println("Product Found: "
                    + found.getProductName());
        }
    }
}