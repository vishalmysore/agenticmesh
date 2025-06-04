package vishalmysore.agenticmesh.mesh;

import vishalmysore.agenticmesh.core.MeshParticipantAgent;
import java.util.List;

/**
 * Core interface for all mesh patterns in the AgenticMesh ecosystem
 */
public interface Mesh {
    /**
     * Gets the unique identifier for this mesh
     */
    String getId();
    
    /**
     * Gets the type of mesh pattern
     */
    String getPattern();
    
    /**
     * Adds an agent to the mesh
     */
    void addAgent(MeshParticipantAgent agent);
    
    /**
     * Removes an agent from the mesh
     */
    void removeAgent(String agentId);
    
    /**
     * Gets all agents in the mesh
     */
    List<MeshParticipantAgent> getAgents();
    
    /**
     * Initializes the mesh pattern
     */
    void initialize();
    
    /**
     * Starts the mesh pattern execution
     */
    void start();
    
    /**
     * Stops the mesh pattern execution
     */
    void stop();
    
    /**
     * Gets the current state of the mesh
     */
    MeshState getState();
}
