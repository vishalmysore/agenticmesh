package vishalmysore.agenticmesh.mesh;

import vishalmysore.agenticmesh.core.Agent;
import vishalmysore.agenticmesh.core.Message;
import vishalmysore.agenticmesh.events.Event;
import vishalmysore.agenticmesh.events.EventBus;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of a Peer-to-Peer mesh pattern where agents communicate directly with each other
 */
public class P2PMesh implements Mesh {
    private final String id;
    private final Map<String, Agent> agents;
    private final Map<String, Set<String>> connections;
    private final MeshState state;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final Map<String, Integer> messageCounter;
    private final EventBus eventBus;

    public P2PMesh(String id) {
        this.id = id;
        this.agents = new ConcurrentHashMap<>();
        this.connections = new ConcurrentHashMap<>();
        this.state = new MeshState(id, "p2p");
        this.executorService = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.messageCounter = new ConcurrentHashMap<>();
        this.eventBus = EventBus.getInstance();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPattern() {
        return "p2p";
    }

    @Override
    public void addAgent(Agent agent) {
        agents.put(agent.getId(), agent);
        connections.putIfAbsent(agent.getId(), ConcurrentHashMap.newKeySet());
        state.incrementAgentCount();
    }

    @Override
    public void removeAgent(String agentId) {
        if (agents.remove(agentId) != null) {
            // Remove all connections to/from this agent
            connections.remove(agentId);
            connections.values().forEach(peers -> peers.remove(agentId));
            state.decrementAgentCount();
        }
    }

    @Override
    public List<Agent> getAgents() {
        return new ArrayList<>(agents.values());
    }

    @Override
    public void initialize() {
        state.setStatus(MeshState.Status.INITIALIZING);
        for (Agent agent : agents.values()) {
            agent.initialize();
        }
        startNetworkMonitoring();
        state.setStatus(MeshState.Status.INITIALIZED);
    }

    private void startNetworkMonitoring() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                checkNetworkHealth();
                rebalanceConnections();
            } catch (Exception e) {
                System.err.println("Error in network monitoring: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void checkNetworkHealth() {
        for (Map.Entry<String, Set<String>> entry : connections.entrySet()) {
            String agentId = entry.getKey();
            Set<String> peers = entry.getValue();
            
            // Check if agent has minimum required connections
            if (peers.size() < getMinConnections()) {
                findNewPeers(agentId);
            }
            
            // Check connection quality
            checkConnectionQuality(agentId, peers);
        }
    }

    private void findNewPeers(String agentId) {
        Set<String> currentPeers = connections.get(agentId);
        int needed = getMinConnections() - currentPeers.size();
        
        // Find potential new peers
        List<String> availablePeers = agents.keySet().stream()
            .filter(id -> !id.equals(agentId))
            .filter(id -> !currentPeers.contains(id))
            .collect(Collectors.toList());
        
        // Randomly select new peers
        Collections.shuffle(availablePeers);
        availablePeers.stream()
            .limit(needed)
            .forEach(peerId -> connect(agentId, peerId));
    }

    private void checkConnectionQuality(String agentId, Set<String> peers) {
        peers.forEach(peerId -> {
            int messageCount = messageCounter.getOrDefault(agentId + "-" + peerId, 0);
            if (messageCount == 0) {
                // No recent communication, consider removing connection
                disconnect(agentId, peerId);
            }
        });
    }

    private void rebalanceConnections() {
        // Find overloaded and underloaded agents
        Map<String, Integer> connectionCounts = new HashMap<>();
        connections.forEach((agentId, peers) -> 
            connectionCounts.put(agentId, peers.size()));
        
        double avgConnections = connectionCounts.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        connectionCounts.forEach((agentId, count) -> {
            if (count > avgConnections * 1.5) {
                // Too many connections, remove some
                removeExcessConnections(agentId, count - (int)avgConnections);
            } else if (count < avgConnections * 0.5) {
                // Too few connections, add some
                findNewPeers(agentId);
            }
        });
    }

    private void removeExcessConnections(String agentId, int excessCount) {
        Set<String> peers = connections.get(agentId);
        if (peers == null || peers.isEmpty()) return;

        // Remove least active connections
        peers.stream()
            .sorted((p1, p2) -> {
                int count1 = messageCounter.getOrDefault(agentId + "-" + p1, 0);
                int count2 = messageCounter.getOrDefault(agentId + "-" + p2, 0);
                return Integer.compare(count1, count2);
            })
            .limit(excessCount)
            .forEach(peerId -> disconnect(agentId, peerId));
    }

    public void connect(String agentId1, String agentId2) {
        if (!agents.containsKey(agentId1) || !agents.containsKey(agentId2)) {
            throw new IllegalArgumentException("One or both agents not found");
        }
        connections.get(agentId1).add(agentId2);
        connections.get(agentId2).add(agentId1);
    }

    public void disconnect(String agentId1, String agentId2) {
        if (connections.containsKey(agentId1)) {
            connections.get(agentId1).remove(agentId2);
        }
        if (connections.containsKey(agentId2)) {
            connections.get(agentId2).remove(agentId1);
        }
    }

    public void sendMessage(Message message) {
        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();

        if (!agents.containsKey(senderId) || !agents.containsKey(receiverId)) {
            throw new IllegalArgumentException("Invalid sender or receiver");
        }

        if (!connections.get(senderId).contains(receiverId)) {
            throw new IllegalStateException("No connection between agents");
        }

        executorService.submit(() -> {
            try {
                agents.get(receiverId).processMessage(message);
                // Update message counter
                String key = senderId + "-" + receiverId;
                messageCounter.merge(key, 1, Integer::sum);
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        });
    }

    private void processMessage(Message message) {
        if (message.getReceiverId() == null) {
            // Broadcast message to all spokes
            broadcastToSpokes(message);
            // Notify event bus of broadcast
            eventBus.publish(new Event("message.broadcast", message.getSenderId(), 
                Map.of("messageId", message.getId()), Event.EventPriority.MEDIUM));
        } else {
            // Route to specific agent
            Agent targetAgent = agents.get(message.getReceiverId());
            if (targetAgent != null) {
                sendToAgent(targetAgent, message);
                // Notify event bus of direct message
                eventBus.publish(new Event("message.direct", message.getSenderId(),
                    Map.of("messageId", message.getId(), 
                          "receiverId", message.getReceiverId()), 
                    Event.EventPriority.MEDIUM));
            }
        }
    }

    private void broadcastToSpokes(Message message) {
        agents.values().stream()
            .filter(agent -> !agent.getId().equals(message.getSenderId()))
            .forEach(agent -> 
                executorService.submit(() -> sendToAgent(agent, message))
            );
    }

    private void sendToAgent(Agent agent, Message message) {
        try {
            agent.processMessage(message);
            String key = message.getSenderId() + "-" + agent.getId();
            messageCounter.merge(key, 1, Integer::sum);
            // Notify successful message delivery
            eventBus.publish(new Event("message.delivered", message.getSenderId(),
                Map.of("messageId", message.getId(), 
                      "receiverId", agent.getId()),
                Event.EventPriority.LOW));
        } catch (Exception e) {
            System.err.println("Error sending message to agent " + agent.getId() + ": " + e.getMessage());
            // Notify message delivery failure
            eventBus.publish(new Event("message.failed", message.getSenderId(),
                Map.of("messageId", message.getId(), 
                      "receiverId", agent.getId(),
                      "error", e.getMessage()),
                Event.EventPriority.HIGH));
        }
    }

    public Set<String> getPeers(String agentId) {
        return new HashSet<>(connections.getOrDefault(agentId, Collections.emptySet()));
    }

    private int getMinConnections() {
        return Math.max(2, (int)(Math.log(agents.size()) / Math.log(2)));
    }

    @Override
    public void start() {
        state.setStatus(MeshState.Status.RUNNING);
    }

    @Override
    public void stop() {
        state.setStatus(MeshState.Status.STOPPING);
        executorService.shutdown();
        scheduledExecutor.shutdown();
        try {
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
        agents.values().forEach(Agent::shutdown);
        state.setStatus(MeshState.Status.STOPPED);
    }

    @Override
    public MeshState getState() {
        return state;
    }
}
