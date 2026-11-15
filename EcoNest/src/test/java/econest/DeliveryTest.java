package econest;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryTest {

    @Test
    void storesAllDeliveryAssignmentDetails() {
        LocalDate scheduledDate = LocalDate.of(2026, 11, 20);

        Delivery delivery = new Delivery(
                "D001", "O001", "Nimal Perera",
                "A001", "Kamal Silva", scheduledDate, "ASSIGNED");

        assertEquals("D001", delivery.getDeliveryId());
        assertEquals("O001", delivery.getOrderId());
        assertEquals("Nimal Perera", delivery.getClientName());
        assertEquals("A001", delivery.getAgentId());
        assertEquals("Kamal Silva", delivery.getAgentName());
        assertEquals(scheduledDate, delivery.getScheduledDate());
        assertEquals("ASSIGNED", delivery.getStatus());
    }
}
