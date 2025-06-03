package vishalmysore.agenticmesh.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an event in the AgenticMesh system
 */
public class Event {
    private final String id;
    private final String type;
    private final String sourceAgentId;
    private final Instant timestamp;
    private final Map<String, Object> data;
    private final EventPriority priority;

    public Event(String type, String sourceAgentId, Map<String, Object> data, EventPriority priority) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.sourceAgentId = sourceAgentId;
        this.timestamp = Instant.now();
        this.data = data;
        this.priority = priority;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getSourceAgentId() { return sourceAgentId; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getData() { return data; }
    public EventPriority getPriority() { return priority; }

    public enum EventPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
