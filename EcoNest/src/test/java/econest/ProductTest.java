package econest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void plantReportsItsCategoryAndExtraFields() {
        Plant plant = new Plant("P001", "Snake Plant", 2500.00, 5,
                "Hardy indoor air purifier", "LOW", false);

        assertEquals(ProductCategory.PLANT, plant.getCategory());
        assertEquals("LOW", plant.getSunlight());
        assertFalse(plant.isPetSafe());
        assertEquals("P001", plant.getProductId());
        assertEquals(2500.00, plant.getPrice(), 0.001);
    }

    @Test
    void planterReportsItsCategoryAndExtraFields() {
        Planter planter = new Planter("P005", "Terracotta Pot", 1450.00, 5,
                "Breathable natural clay", "TERRACOTTA", 20);

        assertEquals(ProductCategory.PLANTER, planter.getCategory());
        assertEquals("TERRACOTTA", planter.getMaterial());
        assertEquals(20, planter.getDiameterCm());
    }

    @Test
    void gardenKitReportsItsCategoryAndDifficulty() {
        GardenKit kit = new GardenKit("P008", "Balcony Herb Kit", 4800.00, 5,
                "Basil, mint and coriander starter", "BEGINNER");

        assertEquals(ProductCategory.KIT, kit.getCategory());
        assertEquals("BEGINNER", kit.getDifficulty());
    }

    @Test
    void subtypesAreUsableThroughTheBaseType() {
        Product product = new Plant("P002", "Aloe Vera", 1200.00, 4,
                "Succulent", "HIGH", false);

        assertEquals(ProductCategory.PLANT, product.getCategory());
        assertTrue(product.getDisplayInfo().contains("Aloe Vera"));
        assertTrue(product.getDisplayInfo().contains("P002"));
    }

    @Test
    void blankProductIdIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Plant("  ", "Snake Plant", 100.00, 3, "", "LOW", false));

        assertTrue(error.getMessage().toLowerCase().contains("product id"),
                "was: " + error.getMessage());
    }

    @Test
    void blankNameIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Plant("P001", "   ", 100.00, 3, "", "LOW", false));

        assertTrue(error.getMessage().toLowerCase().contains("name"),
                "was: " + error.getMessage());
    }

    @Test
    void negativePriceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Planter("P005", "Pot", -1.00, 3, "", "CLAY", 10));
    }

    @Test
    void nonFinitePriceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Planter("P005", "Pot", Double.NaN, 3, "", "CLAY", 10));
        assertThrows(IllegalArgumentException.class,
                () -> new Planter("P005", "Pot", Double.POSITIVE_INFINITY, 3, "", "CLAY", 10));
    }

    @Test
    void ecoRatingOutsideOneToFiveIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Plant("P001", "Snake Plant", 100.00, 0, "", "LOW", false));
        assertThrows(IllegalArgumentException.class,
                () -> new Plant("P001", "Snake Plant", 100.00, 6, "", "LOW", false));
    }

    @Test
    void ecoRatingBoundariesAreAccepted() {
        assertDoesNotThrow(() -> new Plant("P001", "A", 0.00, 1, "", "LOW", false));
        assertDoesNotThrow(() -> new Plant("P002", "B", 0.00, 5, "", "LOW", false));
    }

    @Test
    void negativeDiameterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Planter("P005", "Pot", 100.00, 3, "", "CLAY", -5));
    }
}
