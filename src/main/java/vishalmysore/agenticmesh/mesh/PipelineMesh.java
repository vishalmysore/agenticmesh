package vishalmysore.agenticmesh.mesh;

import vishalmysore.agenticmesh.core.MeshParticipantAgent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a pipeline mesh pattern where agents process data sequentially
 */
public class PipelineMesh implements Mesh {
    private final String id;
    private final Map<String, MeshParticipantAgent> agents;
    private MeshState state;
    private List<String> pipelineOrder;

    public PipelineMesh(String id) {
        this.id = id;
        this.agents = new LinkedHashMap<>();
        this.state = new MeshState(id, "pipeline");
        this.pipelineOrder = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPattern() {
        return "pipeline";
    }

    @Override
    public void addAgent(MeshParticipantAgent agent) {
        agents.put(agent.getId(), agent);
        pipelineOrder.add(agent.getId());
        state.incrementAgentCount();
    }

    @Override
    public void removeAgent(String agentId) {
        if (agents.remove(agentId) != null) {
            pipelineOrder.remove(agentId);
            state.decrementAgentCount();
        }
    }

    @Override
    public List<MeshParticipantAgent> getAgents() {
        List<MeshParticipantAgent> orderedAgents = new ArrayList<>();
        for (String agentId : pipelineOrder) {
            orderedAgents.add(agents.get(agentId));
        }
        return orderedAgents;
    }

    @Override
    public void initialize() {
        state.setStatus(MeshState.Status.INITIALIZING);
        for (MeshParticipantAgent agent : agents.values()) {
            agent.initialize();
        }
        state.setStatus(MeshState.Status.INITIALIZED);
    }

    @Override
    public void start() {
        state.setStatus(MeshState.Status.RUNNING);
        // Start pipeline processing
    }

    @Override
    public void stop() {
        state.setStatus(MeshState.Status.STOPPING);
        for (MeshParticipantAgent agent : agents.values()) {
            agent.shutdown();
        }
        state.setStatus(MeshState.Status.STOPPED);
    }

    @Override
    public MeshState getState() {
        return state;
    }

    /**
     * Sets the order of agents in the pipeline
     */
    public void setPipelineOrder(List<String> order) {
        if (!agents.keySet().containsAll(order) || order.size() != agents.size()) {
            throw new IllegalArgumentException("Invalid pipeline order");
        }
        this.pipelineOrder = new ArrayList<>(order);
    }

    /**
     * Gets the next agent in the pipeline
     */
    public MeshParticipantAgent getNextAgent(String currentAgentId) {
        int currentIndex = pipelineOrder.indexOf(currentAgentId);
        if (currentIndex < 0 || currentIndex >= pipelineOrder.size() - 1) {
            return null;
        }
        return agents.get(pipelineOrder.get(currentIndex + 1));
    }
}
