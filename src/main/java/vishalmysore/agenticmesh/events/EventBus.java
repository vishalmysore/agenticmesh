package vishalmysore.agenticmesh.events;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event bus for publishing and subscribing to events in the AgenticMesh system
 */
public class EventBus {
    private static EventBus instance;
    private final SubmissionPublisher<Event> publisher;
    private final Map<String, Flow.Subscriber<Event>> subscribers;

    private EventBus() {
        this.publisher = new SubmissionPublisher<>();
        this.subscribers = new ConcurrentHashMap<>();
    }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void publish(Event event) {
        publisher.submit(event);
    }

    public void subscribe(String subscriberId, Flow.Subscriber<Event> subscriber) {
        subscribers.put(subscriberId, subscriber);
        publisher.subscribe(subscriber);
    }

    public void unsubscribe(String subscriberId) {
        Flow.Subscriber<Event> subscriber = subscribers.remove(subscriberId);
        if (subscriber != null) {
            // Cleanup subscription
        }
    }

    public void shutdown() {
        publisher.close();
    }
}
