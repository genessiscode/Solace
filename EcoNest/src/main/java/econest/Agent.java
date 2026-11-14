package econest;

/**
 * A delivery agent who carries EcoNest orders to clients.
 *
 * <p>Holds the attributes every agent has, and validates them on
 * construction so an invalid agent can never reach the database. Callers
 * catch {@link IllegalArgumentException} and report it to the user.</p>
 *
 * <p>Optional columns are normalised to an empty string rather than
 * {@code null}, the same way {@link Client} treats its phone number, so
 * getters can be used directly in the UI without null checks.</p>
 */
public class Agent {

    private final String agentId;

    private final String name;

    private final String phone;

    private final String email;

    private final AgentStatus status;

    /**
     * @param agentId the agent identifier, for example {@code A001}
     * @param name    the display name of the agent
     * @param phone   an optional phone number
     * @param email   an optional email address
     * @param status  the current availability of the agent
     * @throws IllegalArgumentException if {@code agentId} or {@code name}
     *                                  is blank, or {@code status} is
     *                                  {@code null}
     */
    public Agent(
            String agentId,
            String name,
            String phone,
            String email,
            AgentStatus status) {

        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Agent ID cannot be empty.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Agent name cannot be empty.");
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Agent status cannot be empty.");
        }

        this.agentId = agentId.trim();
        this.name = name.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.email = email == null ? "" : email.trim();
        this.status = status;
    }

    /**
     * @return the agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the phone number, or an empty string when none was given
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return the email address, or an empty string when none was given
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the current availability of the agent
     */
    public AgentStatus getStatus() {
        return status;
    }

    /**
     * A one-line label for dialogs and table cells.
     *
     * @return text of the form {@code A001 - Kasun Bandara (AVAILABLE)}
     */
    public String getDisplayInfo() {
        return agentId + " - " + name + " (" + status + ")";
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}
