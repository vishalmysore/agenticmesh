package vishalmysore.agenticmesh.mesh;

import vishalmysore.agenticmesh.core.MeshParticipantAgent;
import vishalmysore.agenticmesh.core.AgentState;
import vishalmysore.agenticmesh.core.Message;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of a hub-and-spoke mesh pattern where a central agent coordinates with peripheral agents
 */
public class HubMesh implements Mesh {
    private final String id;
    private final Map<String, MeshParticipantAgent> agents;
    private MeshParticipantAgent hubAgent;
    private MeshState state;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final Map<String, Integer> agentLoadCount;
    private final Queue<Message> messageQueue;
    private final Set<String> activeAgents;
    private LoadBalancingStrategy loadBalancingStrategy;
    
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_LOADED,
        RANDOM
    }

    public HubMesh(String id) {
        this.id = id;
        this.agents = new HashMap<>();
        this.state = new MeshState(id, "hub-spoke");
        this.executorService = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.agentLoadCount = new ConcurrentHashMap<>();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.activeAgents = ConcurrentHashMap.newKeySet();
        this.loadBalancingStrategy = LoadBalancingStrategy.ROUND_ROBIN;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPattern() {
        return "hub-spoke";
    }

    @Override
    public void addAgent(MeshParticipantAgent agent) {
        if (hubAgent == null) {
            hubAgent = agent; // First agent becomes the hub
        }
        agents.put(agent.getId(), agent);
        state.incrementAgentCount();
    }

    @Override
    public void removeAgent(String agentId) {
        if (hubAgent != null && hubAgent.getId().equals(agentId)) {
            hubAgent = null;
        }
        if (agents.remove(agentId) != null) {
            state.decrementAgentCount();
            activeAgents.remove(agentId);
            agentLoadCount.remove(agentId);
        }
    }

    @Override
    public List<MeshParticipantAgent> getAgents() {
        return new ArrayList<>(agents.values());
    }

    @Override
    public void initialize() {
        if (hubAgent == null) {
            throw new IllegalStateException("Hub agent not set");
        }
        
        state.setStatus(MeshState.Status.INITIALIZING);
        for (MeshParticipantAgent agent : agents.values()) {
            agent.initialize();
        }
        state.setStatus(MeshState.Status.INITIALIZED);
    }

    @Override
    public void start() {
        state.setStatus(MeshState.Status.RUNNING);
        startMessageProcessor();
        startHealthCheck();
    }

    private void startMessageProcessor() {
        executorService.submit(() -> {
            while (state.getStatus() == MeshState.Status.RUNNING) {
                try {
                    Message message = messageQueue.poll();
                    if (message != null) {
                        processMessage(message);
                    }
                    TimeUnit.MILLISECONDS.sleep(100); // Prevent busy waiting
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Log error and continue processing
                    System.err.println("Error processing message: " + e.getMessage());
                }
            }
        });
    }

    private void startHealthCheck() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                checkAgentHealth();
            } catch (Exception e) {
                System.err.println("Error in health check: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void checkAgentHealth() {
        for (MeshParticipantAgent agent : agents.values()) {
            AgentState agentState = agent.getState();
            if (agentState.getStatus() == AgentState.Status.ACTIVE) {
                activeAgents.add(agent.getId());
            } else {
                activeAgents.remove(agent.getId());
            }
        }
    }

    /**
     * Routes a message through the hub to appropriate spoke agents
     */
    public void routeMessage(Message message) {
        if (!state.getStatus().equals(MeshState.Status.RUNNING)) {
            throw new IllegalStateException("Mesh is not running");
        }
        messageQueue.offer(message);
    }

    private void processMessage(Message message) {
        if (message.getReceiverId() == null) {
            // Broadcast message to all spokes
            broadcastToSpokes(message);
        } else {
            // Route to specific agent
            MeshParticipantAgent targetAgent = agents.get(message.getReceiverId());
            if (targetAgent != null) {
                sendToAgent(targetAgent, message);
            }
        }
    }

    private void broadcastToSpokes(Message message) {
        getSpokeAgents().forEach(agent -> 
            executorService.submit(() -> sendToAgent(agent, message))
        );
    }

    private void sendToAgent(MeshParticipantAgent agent, Message message) {
        try {
            agent.processMessage(message);
            agentLoadCount.merge(agent.getId(), 1, Integer::sum);
        } catch (Exception e) {
            System.err.println("Error sending message to agent " + agent.getId() + ": " + e.getMessage());
            // Optionally set agent state to ERROR
            agent.getState().setStatus(AgentState.Status.ERROR);
        }
    }

    /**
     * Gets the next agent based on the current load balancing strategy
     */
    public MeshParticipantAgent getNextAgent(String currentAgentId) {
        List<MeshParticipantAgent> spokes = getSpokeAgents();
        if (spokes.isEmpty()) return null;

        switch (loadBalancingStrategy) {
            case ROUND_ROBIN:
                int currentIndex = -1;
                for (int i = 0; i < spokes.size(); i++) {
                    if (spokes.get(i).getId().equals(currentAgentId)) {
                        currentIndex = i;
                        break;
                    }
                }
                return spokes.get((currentIndex + 1) % spokes.size());

            case LEAST_LOADED:
                return spokes.stream()
                    .min(Comparator.comparingInt(a -> agentLoadCount.getOrDefault(a.getId(), 0)))
                    .orElse(null);

            case RANDOM:
                return spokes.get(new Random().nextInt(spokes.size()));

            default:
                return spokes.get(0);
        }
    }

    /**
     * Sets the load balancing strategy
     */
    public void setLoadBalancingStrategy(LoadBalancingStrategy strategy) {
        this.loadBalancingStrategy = strategy;
    }

    /**
     * Finds agents matching the given predicate
     */
    public List<MeshParticipantAgent> findAgents(Predicate<MeshParticipantAgent> predicate) {
        return agents.values().stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    /**
     * Gets statistics about the mesh
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAgents", agents.size());
        stats.put("activeAgents", activeAgents.size());
        stats.put("messageQueueSize", messageQueue.size());
        stats.put("agentLoads", new HashMap<>(agentLoadCount));
        stats.put("hubAgentId", hubAgent != null ? hubAgent.getId() : null);
        stats.put("loadBalancingStrategy", loadBalancingStrategy);
        return stats;
    }

    @Override
    public MeshState getState() {
        return state;
    }

    @Override
    public void stop() {
        state.setStatus(MeshState.Status.STOPPING);
        
        // Shutdown executors
        executorService.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            // Wait for tasks to complete
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Shutdown agents
        for (MeshParticipantAgent agent : agents.values()) {
            try {
                agent.shutdown();
            } catch (Exception e) {
                System.err.println("Error shutting down agent " + agent.getId() + ": " + e.getMessage());
            }
        }

        state.setStatus(MeshState.Status.STOPPED);
    }

    /**
     * Gets the hub agent
     */
    public MeshParticipantAgent getHubAgent() {
        return hubAgent;
    }

    /**
     * Sets the hub agent
     */
    public void setHubAgent(String agentId) {
        MeshParticipantAgent newHub = agents.get(agentId);
        if (newHub == null) {
            throw new IllegalArgumentException("Agent not found: " + agentId);
        }
        this.hubAgent = newHub;
    }

    /**
     * Gets all spoke agents
     */
    public List<MeshParticipantAgent> getSpokeAgents() {
        List<MeshParticipantAgent> spokes = new ArrayList<>(agents.values());
        spokes.remove(hubAgent);
        return spokes;
    }
}
