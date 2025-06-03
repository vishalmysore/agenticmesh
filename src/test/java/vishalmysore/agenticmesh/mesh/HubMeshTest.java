package vishalmysore.agenticmesh.mesh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vishalmysore.agenticmesh.core.Agent;
import vishalmysore.agenticmesh.core.AgentState;
import vishalmysore.agenticmesh.core.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HubMeshTest {
    
    private HubMesh hubMesh;
    
    @Mock
    private Agent mockHubAgent;
    
    @Mock
    private Agent mockSpokeAgent1;
    
    @Mock
    private Agent mockSpokeAgent2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        hubMesh = new HubMesh("test-hub");
        
        // Setup mock agents
        when(mockHubAgent.getId()).thenReturn("hub-agent");
        when(mockSpokeAgent1.getId()).thenReturn("spoke1");
        when(mockSpokeAgent2.getId()).thenReturn("spoke2");
        
        when(mockHubAgent.getState()).thenReturn(new AgentState("hub-agent"));
        when(mockSpokeAgent1.getState()).thenReturn(new AgentState("spoke1"));
        when(mockSpokeAgent2.getState()).thenReturn(new AgentState("spoke2"));
    }

    @Test
    void testHubMeshInitialization() {
        assertEquals("test-hub", hubMesh.getId());
        assertEquals("hub-spoke", hubMesh.getPattern());
        assertTrue(hubMesh.getAgents().isEmpty());
    }

    @Test
    void testAddingAgents() {
        hubMesh.addAgent(mockHubAgent);
        hubMesh.addAgent(mockSpokeAgent1);
        
        assertEquals(2, hubMesh.getAgents().size());
        assertTrue(hubMesh.getAgents().contains(mockHubAgent));
        assertTrue(hubMesh.getAgents().contains(mockSpokeAgent1));
    }

    @Test
    void testHubAgentAssignment() {
        hubMesh.addAgent(mockHubAgent);
        assertEquals(mockHubAgent, hubMesh.getHubAgent());
        
        // Adding spoke agents shouldn't change the hub
        hubMesh.addAgent(mockSpokeAgent1);
        assertEquals(mockHubAgent, hubMesh.getHubAgent());
    }

    @Test
    void testMessageRouting() {
        hubMesh.addAgent(mockHubAgent);
        hubMesh.addAgent(mockSpokeAgent1);
        hubMesh.addAgent(mockSpokeAgent2);
        hubMesh.initialize();
        hubMesh.start();

        Message testMessage = new Message(
            "test-msg", 
            "spoke1", 
            "spoke2", 
            "A2A", 
            "test payload", 
            Message.MessageType.COMMAND
        );

        hubMesh.routeMessage(testMessage);
        
        // Wait for async processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(mockSpokeAgent2).processMessage(testMessage);
    }

    @Test
    void testMeshShutdown() {
        hubMesh.addAgent(mockHubAgent);
        hubMesh.addAgent(mockSpokeAgent1);
        hubMesh.initialize();
        hubMesh.start();
        hubMesh.stop();

        verify(mockHubAgent).shutdown();
        verify(mockSpokeAgent1).shutdown();
        assertEquals(MeshState.Status.STOPPED, hubMesh.getState().getStatus());
    }
}
