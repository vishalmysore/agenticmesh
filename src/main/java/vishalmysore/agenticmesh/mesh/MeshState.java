package vishalmysore.agenticmesh.mesh;

/**
 * State holder for mesh properties and runtime information
 */
public class MeshState {
    private final String meshId;
    private final String pattern;
    private Status status;
    private int agentCount;
    private long startTime;
    private long lastUpdateTime;

    public MeshState(String meshId, String pattern) {
        this.meshId = meshId;
        this.pattern = pattern;
        this.status = Status.CREATED;
        this.agentCount = 0;
        this.startTime = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // Getters and setters
    public String getMeshId() { return meshId; }
    public String getPattern() { return pattern; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status; 
        this.lastUpdateTime = System.currentTimeMillis();
        if (status == Status.RUNNING && startTime == 0) {
            this.startTime = System.currentTimeMillis();
        }
    }
    public int getAgentCount() { return agentCount; }
    public void incrementAgentCount() { this.agentCount++; }
    public void decrementAgentCount() { this.agentCount--; }
    public long getStartTime() { return startTime; }
    public long getLastUpdateTime() { return lastUpdateTime; }

    public enum Status {
        CREATED,
        INITIALIZING,
        INITIALIZED,
        RUNNING,
        STOPPING,
        STOPPED,
        ERROR
    }
}
