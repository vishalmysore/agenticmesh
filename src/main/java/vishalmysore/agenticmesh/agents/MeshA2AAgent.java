package vishalmysore.agenticmesh.agents;

import io.github.vishalmysore.a2a.client.A2AAgent;
import vishalmysore.agenticmesh.core.MeshParticipantAgent;
import vishalmysore.agenticmesh.core.AgentState;
import vishalmysore.agenticmesh.core.Message;

/**
 * Implementation of an agent that uses the A2A (Agent-to-Agent) protocol
 */
public class MeshA2AAgent extends A2AAgent implements MeshParticipantAgent {
    private final String id;
    private final String type;
    private AgentState state;

    public MeshA2AAgent(){
        this.id = "default-id";
        this.type = "a2a";
        this.state = new AgentState(id);
    }

    public MeshA2AAgent(String id, String type) {
        this.id = id;
        this.type = type;
        this.state = new AgentState(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void initialize() {
        state.setStatus(AgentState.Status.ACTIVE);
    }

    @Override
    public void processMessage(Message message) {
        if (!"A2A".equals(message.getProtocol())) {
            throw new IllegalArgumentException("A2AAgent can only process A2A protocol messages");
        }
        
        state.incrementMessageCount();
        state.updateLastActiveTimestamp();
        
        // Process the message based on type
        switch (message.getType()) {
            case COMMAND:
                handleCommand(message);
                break;
            case QUERY:
                handleQuery(message);
                break;
            case EVENT:
                handleEvent(message);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported message type: " + message.getType());
        }
    }

    @Override
    public AgentState getState() {
        return state;
    }

    @Override
    public void shutdown() {
        state.setStatus(AgentState.Status.SHUTDOWN);
    }

    protected void handleCommand(Message message) {
        // Implementation for handling commands
    }

    protected void handleQuery(Message message) {
        // Implementation for handling queries
    }

    protected void handleEvent(Message message) {
        // Implementation for handling events
    }
}
