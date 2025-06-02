---
title: Building a Dual-Protocol AI Agent: Combining A2A and MCP with Playwright
published: true
description: How I built a versatile web automation server that speaks both A2A and MCP protocols, powered by Playwright
tags: webdev, automation, java, ai
cover_image: https://vishalmysore-a2apw.hf.space/static/homepage_screenshot.png
canonical_url: https://dev.to/vishalmysore/building-a-web-automation-agent-with-playwright-and-ai-protocols
---

Hey devs! 👋 Today I'm excited to share something unique - I built a dual-protocol AI agent that can speak both A2A (Agent-to-Agent) and MCP (Model Context Protocol) while automating web browsers. Think of it as a polyglot web automation server that can seamlessly integrate with any AI system, regardless of which protocol they're using!

## 🎯 The Project: Dual-Protocol Web Automation Agent

What makes this project special is its ability to handle both protocols simultaneously:

- 🤝 **A2A Protocol Support**
  - Google's Agent-to-Agent protocol
  - Standard JSON-RPC communication
  - Task-based interaction model

- 🔄 **MCP Protocol Support**
  - Model Context Protocol integration
  - Direct LLM communication
  - Context-aware interactions

Plus these awesome capabilities:
- 🤖 Control web browsers using natural language
- 📸 Take screenshots and extract text from web pages
- 🔌 Process requests via both A2A and MCP endpoints
- 🌐 Seamless protocol translation

Want to try it out? Check it here: https://vishalmysore-a2apw.hf.space/

## 🛠️ The Tech Stack

Here's what I used to build this dual-protocol server:

```
Backend ➡️ Java Spring Framework
Web Automation ➡️ Playwright
Protocol Support ➡️ a2ajava framework
Dual API ➡️ A2A + MCP handlers
Deployment ➡️ Docker + HuggingFace Spaces
```

## 🎢 The Journey: Challenges & Solutions

### 1. Protocol Integration Hell 😅

**Challenge**: Not just making one protocol work, but getting TWO different protocols to coexist harmoniously! A2A and MCP have different philosophies and communication patterns.

**Solution**: I created a unified protocol handling layer that abstracts away the differences. Here's a simplified view:

```java
// Unified protocol handling
public interface ProtocolHandler {
    Response processRequest(Request request);
    void validateMessage(Message message);
}

// Protocol-specific implementations
@Component
public class A2AHandler implements ProtocolHandler {
    // A2A-specific handling
}

@Component
public class MCPHandler implements ProtocolHandler {
    // MCP-specific handling
}
```

### 2. Browser Automation Reliability 🎭

**Challenge**: Web pages are like wild animals - unpredictable and constantly changing.

**Solution**: Implemented robust waiting mechanisms and retry logic. Here's a peek at the pattern:

```java
await page.waitForSelector('.dynamic-content', {
    state: 'visible',
    timeout: 5000
});
```

### 3. Resource Management 🔄

**Challenge**: Browsers love to eat RAM for breakfast!

**Solution**: Implemented browser recycling and parallel execution management:
- Pool of managed browser instances
- Automatic cleanup of unused resources
- Smart request queuing

## 💡 Key Learnings About Dual-Protocol Design

1. **Protocol Abstraction is Key**
   - Create a common interface for both protocols
   - Handle protocol-specific quirks in separate layers
   - Maintain protocol-agnostic business logic

2. **Error Handling Across Protocols**
   - Implement protocol-specific error mapping
   - Maintain consistent error responses
   - Log protocol-specific debugging info

3. **Performance Considerations**
   - Handle protocol-specific rate limiting
   - Optimize for different message formats
   - Manage resources across both protocols

## 🚀 Quick Start Examples

### A2A Protocol Example
```bash
curl -X POST \
-H "Content-Type: application/json" \
-d '{
    "method": "tools/call",
    "params": {
        "name": "browseWebAndReturnText",
        "arguments": {
            "provideAllValuesInPlainEnglish": "Go to Google.com, search for \"a2ajava\""
        }
    },
    "jsonrpc": "2.0",
    "id": 17
}' \
http://localhost:7860/a2a
```

### MCP Protocol Example
```bash
curl -X POST \
-H "Content-Type: application/json" \
-d '{
    "name": "browseWebAndReturnText",
    "input": {
        "text": "Go to Google.com, search for \"a2ajava\""
    }
}' \
http://localhost:7860/mcp
```

## 🎓 Tips for Your Own Project

1. **Architecture First**
   - Plan your protocol handling strategy
   - Design with extensibility in mind
   - Keep business logic separate from protocol handling

2. **Testing is Crucial**
   - Test with different types of websites
   - Verify protocol compliance
   - Check resource management

3. **Security Matters**
   - Validate URLs
   - Implement rate limiting
   - Handle browser security settings

## 🔮 What's Next?

I'm working on some exciting improvements:
- Enhanced natural language processing
- Better error reporting
- Advanced caching mechanisms
- Multi-browser parallel execution

## 🤝 Why Two Protocols?

The decision to support both A2A and MCP wasn't just a technical challenge - it's about interoperability. By speaking both protocols, this agent can:

1. **Bridge Different AI Ecosystems**
   - Work with Google's AI agents (A2A)
   - Integrate with MCP-based language models
   - Enable cross-protocol communication

2. **Future-Proof Design**
   - Support emerging AI protocols
   - Adapt to ecosystem changes
   - Maintain backward compatibility

3. **Maximum Flexibility**
   - Choose the best protocol for each use case
   - No vendor lock-in
   - Seamless integration options

## 🤝 Get Involved!

The project is open source and welcomes contributions! Whether you're into web automation or AI protocols, there's something for everyone.

Check out the [agent card](https://vishalmysore-a2apw.hf.space/.well-known/agent.json) for more details.

---

Have you worked with multiple AI protocols? What challenges did you face? Let me know in the comments! 👇

*Built with a2ajava - Bridging AI protocols for the next generation of agents* ✨