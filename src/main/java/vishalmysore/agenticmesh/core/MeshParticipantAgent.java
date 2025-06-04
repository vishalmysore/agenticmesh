package vishalmysore.agenticmesh.core;

/**
 * Core interface for all agents in the AgenticMesh ecosystem
 */
public interface MeshParticipantAgent {
    /**
     * Gets the unique identifier for this agent
     */
    String getId();
    
    /**
     * Gets the type/role of this agent
     */
    String getType();
    
    /**
     * Initializes the agent with configuration
     */
    void initialize();
    
    /**
     * Processes a message received by this agent
     */
    void processMessage(Message message);
    
    /**
     * Gets the current state of the agent
     */
    AgentState getState();
    
    /**
     * Shuts down the agent gracefully
     */
    void shutdown();
}
