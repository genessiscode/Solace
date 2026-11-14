package econest;

/**
 * The kinds of availability a delivery agent can be in. The constant names
 * match the values stored in the {@code agents.status} column.
 */
public enum AgentStatus {
    /** The agent is free to be assigned a delivery. */
    AVAILABLE,
    /** The agent is currently out on a delivery. */
    ON_DELIVERY,
    /** The agent no longer takes deliveries. */
    INACTIVE
}
