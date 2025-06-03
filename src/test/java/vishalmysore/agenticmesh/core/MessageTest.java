package vishalmysore.agenticmesh.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    void testMessageCreation() {
        Message message = new Message("msg-1", "agent1", "agent2", "A2A", "test payload", Message.MessageType.COMMAND);
        
        assertEquals("msg-1", message.getId());
        assertEquals("agent1", message.getSenderId());
        assertEquals("agent2", message.getReceiverId());
        assertEquals("A2A", message.getProtocol());
        assertEquals("test payload", message.getPayload());
        assertEquals(Message.MessageType.COMMAND, message.getType());
    }

   // @Test
    void testNullProtocolHandling() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Message("msg-1", "agent1", "agent2", null, "payload", Message.MessageType.COMMAND)
        );
    }

    //@Test
    void testNullMessageType() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Message("msg-1", "agent1", "agent2", "A2A", "payload", null)
        );
    }
}
