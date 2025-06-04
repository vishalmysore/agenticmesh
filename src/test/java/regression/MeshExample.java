package regression;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.a2a.client.A2AAgent;
import io.github.vishalmysore.a2a.domain.Task;
import io.github.vishalmysore.common.CommonClientResponse;
import lombok.extern.java.Log;
import org.checkerframework.checker.index.qual.LengthOf;
import vishalmysore.agenticmesh.agents.MeshA2AAgent;
import vishalmysore.agenticmesh.agents.MeshMCPAgent;
import vishalmysore.agenticmesh.core.Message;
import vishalmysore.agenticmesh.mesh.HubMesh;
import vishalmysore.agenticmesh.mesh.P2PMesh;

@Log
public class MeshExample {
    private static String CAR_BOOKING_AGENT= "https://vishalmysore-a2amcpdemo.hf.space/";
    private static String LAUDRY_AGENT = "https://vishalmysore-a2amcpmongo.hf.space/";
    private static String CUSTOMER_SERVICE_AGENT = "https://vishalmysore-a2amcpspring.hf.space/";

    public static void createHubSpokeMesh() {
        log.info("====== Starting Hub-Spoke Mesh Test ======");
        
        // Create hub mesh with customer service as the hub
        HubMesh serviceMesh = new HubMesh("service-mesh");
        
        // Create agents
        MeshA2AAgent customerServiceHub = new MeshA2AAgent();
        customerServiceHub.connect(CUSTOMER_SERVICE_AGENT);
        log.info("Connected hub agent to: " + CUSTOMER_SERVICE_AGENT);
        
        MeshMCPAgent carBookingSpoke = new MeshMCPAgent();
        carBookingSpoke.connect(CAR_BOOKING_AGENT);
        log.info("Connected car booking spoke to: " + CAR_BOOKING_AGENT);
        
        MeshMCPAgent laundrySpoke = new MeshMCPAgent();
        laundrySpoke.connect(LAUDRY_AGENT);
        log.info("Connected laundry spoke to: " + LAUDRY_AGENT);
        
        // Add agents to mesh (first agent becomes hub)
        serviceMesh.addAgent(customerServiceHub);
        serviceMesh.addAgent(carBookingSpoke);
        serviceMesh.addAgent(laundrySpoke);
        log.info("Added all agents to hub-spoke mesh. Total agents: " + serviceMesh.getAgents().size());
        
        // Initialize and start mesh
        serviceMesh.initialize();
        log.info("Hub-Spoke mesh initialized. Starting message processor...");
        serviceMesh.start();
        log.info("Hub-Spoke mesh started - Message routing through hub enabled");
        
        try {
            // Give time for message processor to start
            Thread.sleep(1000);
            
            // Spoke agents send messages through hub
            Message carRequest = new Message(
                "car-req",
                carBookingSpoke.getId(),
                customerServiceHub.getId(),
                "MCP",
                "get me list of all the available car",
                Message.MessageType.COMMAND
            );
            
            log.info("Routing message from " + carBookingSpoke.getId() + " through hub " + customerServiceHub.getId());
            serviceMesh.routeMessage(carRequest);
            log.info("Message queued for routing. Waiting for processing...");
            
            // Wait for message processing
            Thread.sleep(1000);
            
            Message laundryRequest = new Message(
                "laundry-req", 
                laundrySpoke.getId(),
                customerServiceHub.getId(),
                "MCP",
                "I need to get my cricket cloth washed",
                Message.MessageType.COMMAND
            );
            
            log.info("Routing message from " + laundrySpoke.getId() + " through hub " + customerServiceHub.getId());
            serviceMesh.routeMessage(laundryRequest);
            log.info("Message queued for routing. Waiting for processing...");
            
            // Wait for message processing
            Thread.sleep(1000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warning("Message processing interrupted: " + e.getMessage());
        } finally {
            serviceMesh.stop();
            log.info("====== Hub-Spoke Mesh Test Complete ======");
        }
    }

    public static void createP2PMesh() {
        log.info("====== Starting P2P Mesh Test ======");
        
        // Create P2P mesh
        P2PMesh serviceMesh = new P2PMesh("service-mesh");
        
        // Create agents
        MeshA2AAgent customerService = new MeshA2AAgent();
        customerService.connect(CUSTOMER_SERVICE_AGENT);
        log.info("Connected customer service agent to: " + CUSTOMER_SERVICE_AGENT);
        
        MeshMCPAgent carBooking = new MeshMCPAgent();
        carBooking.connect(CAR_BOOKING_AGENT);
        log.info("Connected car booking agent to: " + CAR_BOOKING_AGENT);
        
        MeshMCPAgent laundry = new MeshMCPAgent();
        laundry.connect(LAUDRY_AGENT);
        log.info("Connected laundry agent to: " + LAUDRY_AGENT);
        
        // Add agents to mesh
        serviceMesh.addAgent(customerService);
        serviceMesh.addAgent(carBooking);
        serviceMesh.addAgent(laundry);
        log.info("Added all agents to P2P mesh. Total agents: " + serviceMesh.getAgents().size());
        
        // Create direct connections only between compatible agents
        serviceMesh.connect(carBooking.getId(), laundry.getId());
        log.info("Created P2P connection between car booking and laundry agents");
        
        // Initialize and start mesh
        serviceMesh.initialize();
        log.info("P2P Mesh initialized. Starting network monitoring...");
        serviceMesh.start();
        log.info("P2P mesh started - Direct communication enabled");
        
        try {
            // Give time for network monitoring to start
            Thread.sleep(1000);
            
            // Direct peer-to-peer communication between MCP agents
            Message carRequest = new Message(
                "car-req",
                carBooking.getId(),
                laundry.getId(),
                "MCP",
                "get me list of all the available car",
                Message.MessageType.COMMAND
            );
            
            log.info("Sending direct message from " + carBooking.getId() + " to " + laundry.getId());
            serviceMesh.sendMessage(carRequest);
            log.info("Message sent successfully. Waiting for processing...");
            
            // Wait for message processing
            Thread.sleep(1000);
            
            // Direct communication between compatible agents
            Message laundryRequest = new Message(
                "laundry-req",
                laundry.getId(),
                carBooking.getId(),
                "MCP",
                "I need to get my cricket cloth washed",
                Message.MessageType.COMMAND
            );
            
            log.info("Sending direct message from " + laundry.getId() + " to " + carBooking.getId());
            serviceMesh.sendMessage(laundryRequest);
            log.info("Message sent successfully. Waiting for processing...");
            
            // Wait for message processing
            Thread.sleep(1000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warning("Message processing interrupted: " + e.getMessage());
        } finally {
            serviceMesh.stop();
            log.info("====== P2P Mesh Test Complete ======");
        }
    }

    public static void createMesh(String[] args) {

        MeshMCPAgent carBookingAgent = new MeshMCPAgent();
        carBookingAgent.connect(CAR_BOOKING_AGENT);
        CommonClientResponse response = carBookingAgent.remoteMethodCall("listCarTypes","get me list of all the aviabale car");
        log.info("Car Booking Response: " + response);
        MeshMCPAgent laundryMCPAgent = new MeshMCPAgent();
        laundryMCPAgent.connect(LAUDRY_AGENT);
        CommonClientResponse laundryResponse = laundryMCPAgent.remoteMethodCall("getQuoteForLaundry","I need to get my cricket cloth washed");
        log.info("laundryResponse Response: " + laundryResponse);
        MeshA2AAgent customerServiceA2AAgent = new MeshA2AAgent();
        customerServiceA2AAgent.connect(CUSTOMER_SERVICE_AGENT);
        CommonClientResponse customerServiceResponse = customerServiceA2AAgent.remoteMethodCall("my comptuer is not working, can you help me with it?");
        log.info("Customer Service Response: " + customerServiceResponse);



    }

    public static void main(String[] args) {
      //  String jsonStr = "{\"jsonrpc\":\"2.0\", \"id\":\"06c0f388-d39b-46cf-b52a-5f8a7cb86bc7\", \"result\":{\"id\":\"06c0f388-d39b-46cf-b52a-5f8a7cb86bc7\", \"sessionId\":\"a0cbb2c0-2726-49cc-ad64-449ac41f479e\", \"status\":{\"state\":\"COMPLETED\", \"message\":{\"role\":\"agent\", \"parts\":[{\"type\":\"text\", \"text\":\" Your Task with id 06c0f388-d39b-46cf-b52a-5f8a7cb86bc7 is submitted\"}, {\"type\":\"text\", \"text\":\"ticket 111 raised for User\"}], \"timestamp\":\"2025-06-04T13:56:46.246209324Z\"}, \"history\":[{\"role\":null, \"parts\":[{\"type\":\"text\", \"text\":\"my comptuer is not working, can you help me with it?\"}]}]}}}";

       // Task task = parseJsonRpcResponse(jsonStr);
       // log.info("Parsed Task: " + task);
        
        // Try both mesh patterns
        createHubSpokeMesh();
        createP2PMesh();
        
        // Create original mesh for backward compatibility
        createMesh(args);
    }


    public static Task parseJsonRpcResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Parse the JSON-RPC response
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode resultNode = rootNode.get("result");

            // Map the result node directly to Task object
            Task task = mapper.treeToValue(resultNode, Task.class);

            // Set any additional fields if needed
            if (rootNode.has("id")) {
                task.setId(rootNode.get("id").asText());
            }

            return task;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON-RPC response: " + e.getMessage(), e);
        }
    }
}
