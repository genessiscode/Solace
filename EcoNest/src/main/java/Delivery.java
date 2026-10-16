package econest;

import java.time.LocalDate;

/** Represents one order-to-agent delivery assignment. */
public class Delivery {
    private final String deliveryId;
    private final String orderId;
    private final String clientName;
    private final String agentId;
    private final String agentName;
    private final LocalDate scheduledDate;
    private final String status;

    public Delivery(String deliveryId, String orderId, String clientName,
                    String agentId, String agentName, LocalDate scheduledDate,
                    String status) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.clientName = clientName;
        this.agentId = agentId;
        this.agentName = agentName;
        this.scheduledDate = scheduledDate;
        this.status = status;
    }

    public String getDeliveryId() { return deliveryId; }
    public String getOrderId() { return orderId; }
    public String getClientName() { return clientName; }
    public String getAgentId() { return agentId; }
    public String getAgentName() { return agentName; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public String getStatus() { return status; }
}
