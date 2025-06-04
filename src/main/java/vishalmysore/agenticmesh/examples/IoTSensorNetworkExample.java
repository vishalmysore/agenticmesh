package vishalmysore.agenticmesh.examples;

import vishalmysore.agenticmesh.agents.MeshA2AAgent;
import vishalmysore.agenticmesh.core.Message;
import vishalmysore.agenticmesh.mesh.HubMesh;
import vishalmysore.agenticmesh.security.SecurityManager;

/**
 * Example demonstrating a secure IoT sensor network using Hub-and-Spoke pattern
 */
public class IoTSensorNetworkExample {

    public static void main(String[] args) throws Exception {
        // Initialize security
        SecurityManager securityManager = SecurityManager.getInstance();

        // Create the mesh
        HubMesh sensorNetwork = new HubMesh("iot-sensor-network");

        // Create gateway agent (hub)
        MeshA2AAgent gatewayAgent = new MeshA2AAgent("gateway", "iot-gateway") {
            @Override
            protected void handleCommand(Message message) {
                System.out.println("Gateway received command: " + message.getPayload());
                // Process sensor commands
            }
        };

        // Create sensor agents (spokes)
        MeshA2AAgent temperatureSensor = new MeshA2AAgent("temp-sensor", "temperature") {
            @Override
            protected void handleCommand(Message message) {
                // Simulate temperature reading
                double temperature = 20 + Math.random() * 10;
                Message response = new Message(
                    "temp-reading",
                    getId(),
                    "gateway",
                    "A2A",
                    temperature,
                    Message.MessageType.RESPONSE
                );
                // Send reading to gateway
                try {
                    byte[] encrypted = securityManager.encryptMessage(getId(), "gateway", 
                        response.toString().getBytes());
                    response = new Message(response.getId(), response.getSenderId(), 
                        response.getReceiverId(), response.getProtocol(), encrypted, 
                        response.getType());
                    processMessage(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        MeshA2AAgent humiditySensor = new MeshA2AAgent("humidity-sensor", "humidity") {
            @Override
            protected void handleCommand(Message message) {
                // Simulate humidity reading
                double humidity = 40 + Math.random() * 20;
                Message response = new Message(
                    "humidity-reading",
                    getId(),
                    "gateway",
                    "A2A",
                    humidity,
                    Message.MessageType.RESPONSE
                );
                try {
                    byte[] encrypted = securityManager.encryptMessage(getId(), "gateway", 
                        response.toString().getBytes());
                    response = new Message(response.getId(), response.getSenderId(), 
                        response.getReceiverId(), response.getProtocol(), encrypted, 
                        response.getType());
                    processMessage(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Register agents with security manager
        securityManager.registerAgent(gatewayAgent.getId(), null);  // In real implementation, use proper keys
        securityManager.registerAgent(temperatureSensor.getId(), null);
        securityManager.registerAgent(humiditySensor.getId(), null);

        // Grant permissions
        securityManager.grantPermission(gatewayAgent.getId(), "command.send");
        securityManager.grantPermission(temperatureSensor.getId(), "data.send");
        securityManager.grantPermission(humiditySensor.getId(), "data.send");

        // Add agents to mesh
        sensorNetwork.addAgent(gatewayAgent);  // First agent becomes hub
        sensorNetwork.addAgent(temperatureSensor);
        sensorNetwork.addAgent(humiditySensor);

        // Initialize and start the network
        sensorNetwork.initialize();
        sensorNetwork.start();

        // Simulate sensor readings
        for (int i = 0; i < 5; i++) {
            Message readCommand = new Message(
                "read-command-" + i,
                gatewayAgent.getId(),
                null,  // broadcast to all sensors
                "A2A",
                "READ",
                Message.MessageType.COMMAND
            );
            sensorNetwork.routeMessage(readCommand);
            Thread.sleep(1000);
        }

        // Cleanup
        sensorNetwork.stop();
    }
}
