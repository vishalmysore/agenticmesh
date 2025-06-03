package vishalmysore.agenticmesh.core;

/**
 * State holder for agent properties and runtime information
 */
public class AgentState {
    private final String agentId;
    private Status status;
    private long lastActiveTimestamp;
    private int messageCount;
    private Object customState;

    public AgentState(String agentId) {
        this.agentId = agentId;
        this.status = Status.INITIALIZED;
        this.lastActiveTimestamp = System.currentTimeMillis();
        this.messageCount = 0;
    }

    // Getters and setters
    public String getAgentId() { return agentId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public long getLastActiveTimestamp() { return lastActiveTimestamp; }
    public void updateLastActiveTimestamp() { this.lastActiveTimestamp = System.currentTimeMillis(); }
    public int getMessageCount() { return messageCount; }
    public void incrementMessageCount() { this.messageCount++; }
    public Object getCustomState() { return customState; }
    public void setCustomState(Object customState) { this.customState = customState; }

    public enum Status {
        INITIALIZED,
        ACTIVE,
        BUSY,
        ERROR,
        SHUTDOWN
    }
}
