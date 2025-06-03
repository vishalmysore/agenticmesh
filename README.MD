# AgenticMesh - JVM Agent Mesh Framework

## Overview

AgenticMesh is a powerful JVM-based framework for building distributed agent-based systems. It provides implementations of various mesh patterns and protocols, enabling the creation of flexible and scalable agent networks.  
An agentic mesh is a distributed system where autonomous software agents collaborate to achieve complex tasks. These agents, which can be implemented in different languages, work together through standardized protocols, creating a flexible and scalable network. Think of it as a well-coordinated team where each member specializes in different tasks but works towards common goals. 
## Features

- Multiple mesh patterns (Hub-and-Spoke, Pipeline, P2P)
- Protocol support (A2A, MCP)
- Built-in security features
- Event system
- Metrics collection
- State persistence
- Configuration management

## Getting Started

### Prerequisites
- JDK 11 or higher
- Maven 3.6+

### Installation

Add the dependency to your pom.xml:
```xml
<dependency>
    <groupId>vishalmysore</groupId>
    <artifactId>agenticmesh</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage

Here's a simple example creating a hub-and-spoke mesh:

```java
// Create the mesh
HubMesh mesh = new HubMesh("my-mesh");

// Create agents
A2AAgent hubAgent = new A2AAgent("hub", "coordinator");
A2AAgent spokeAgent1 = new A2AAgent("spoke1", "worker");
A2AAgent spokeAgent2 = new A2AAgent("spoke2", "worker");

// Add agents to mesh
mesh.addAgent(hubAgent);  // First agent becomes hub
mesh.addAgent(spokeAgent1);
mesh.addAgent(spokeAgent2);

// Initialize and start
mesh.initialize();
mesh.start();

// Send messages
Message message = new Message(
    "msg1",
    hubAgent.getId(),
    spokeAgent1.getId(),
    "A2A",
    "work-item",
    Message.MessageType.COMMAND
);
mesh.routeMessage(message);
```

## Mesh Patterns

### Hub-and-Spoke Pattern
- Central hub agent coordinates with peripheral agents
- Good for centralized control and monitoring
- Example: IoT sensor networks

### Pipeline Pattern
- Agents process data in sequence
- Good for workflow automation
- Example: Manufacturing processes

### Peer-to-Peer Pattern
- Agents communicate directly
- Good for distributed processing
- Example: Distributed computation

## Security

AgenticMesh provides built-in security features:

```java
SecurityManager security = SecurityManager.getInstance();

// Register agents
security.registerAgent(agentId, publicKey);

// Manage permissions
security.grantPermission(agentId, "read.data");
security.grantPermission(agentId, "write.data");

// Encrypt/decrypt messages
byte[] encrypted = security.encryptMessage(senderId, receiverId, data);
byte[] decrypted = security.decryptMessage(receiverId, encrypted);

// Sign/verify data
byte[] signature = security.sign(data);
boolean isValid = security.verify(data, signature);
```

## Event System

The event system enables asynchronous communication:

```java
EventBus eventBus = EventBus.getInstance();

// Subscribe to events
eventBus.subscribe(subscriberId, event -> {
    System.out.println("Received event: " + event.getType());
});

// Publish events
eventBus.publish(new Event("data.ready", sourceId, data, Event.EventPriority.HIGH));
```

## Metrics

Track system performance:

```java
MetricsCollector metrics = MetricsCollector.getInstance();

// Record metrics
metrics.incrementCounter("messages.processed");
metrics.setGauge("agents.active", activeCount);
metrics.updateAverage("response.time", duration);

// Get metrics
Map<String, Long> counters = metrics.getCounters();
Map<String, Long> gauges = metrics.getGauges();
Map<String, Double> averages = metrics.getAverages();
```

## Best Practices

1. **Error Handling**
   - Implement proper error handling in agents
   - Use circuit breakers for fault tolerance
   - Monitor agent health

2. **Security**
   - Always use encryption for sensitive data
   - Implement proper permission management
   - Regularly rotate encryption keys

3. **Performance**
   - Monitor message queue sizes
   - Balance agent workloads
   - Use appropriate mesh patterns

4. **Testing**
   - Unit test agent behaviors
   - Integration test mesh patterns
   - Load test with realistic scenarios

## Examples

Check out the examples package for complete implementations:
- IoT Sensor Network
- Manufacturing Pipeline
- Distributed Computing Network

## Contributing

We welcome contributions! Please see CONTRIBUTING.md for guidelines.

## License

This project is licensed under the MIT License - see LICENSE file for details.

