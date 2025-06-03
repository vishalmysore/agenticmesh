package vishalmysore.agenticmesh.security;

/**
 * Message authentication and encryption wrapper
 */
public class SecureMessage {
    private final byte[] encryptedContent;
    private final byte[] signature;
    private final String senderId;
    private final String receiverId;
    private final long timestamp;

    public SecureMessage(byte[] encryptedContent, byte[] signature, 
                        String senderId, String receiverId) {
        this.encryptedContent = encryptedContent;
        this.signature = signature;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = System.currentTimeMillis();
    }

    public byte[] getEncryptedContent() { return encryptedContent; }
    public byte[] getSignature() { return signature; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public long getTimestamp() { return timestamp; }
}
