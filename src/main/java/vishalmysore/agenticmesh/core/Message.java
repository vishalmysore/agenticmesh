package vishalmysore.agenticmesh.core;

/**
 * Message class for inter-agent communication
 */
public class Message {
    private final String id;
    private final String senderId;
    private final String receiverId;
    private final String protocol; // "A2A" or "MCP"
    private final Object payload;
    private final MessageType type;

    public Message(String id, String senderId, String receiverId, String protocol, Object payload, MessageType type) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.protocol = protocol;
        this.payload = payload;
        this.type = type;
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getProtocol() { return protocol; }
    public Object getPayload() { return payload; }
    public MessageType getType() { return type; }

    public enum MessageType {
        COMMAND,
        EVENT,
        QUERY,
        RESPONSE
    }
}
