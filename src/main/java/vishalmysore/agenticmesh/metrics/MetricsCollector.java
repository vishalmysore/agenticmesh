package vishalmysore.agenticmesh.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics collector for monitoring agent and mesh performance
 */
public class MetricsCollector {
    private static MetricsCollector instance;
    private final Map<String, AtomicLong> counters;
    private final Map<String, AtomicLong> gauges;
    private final Map<String, Double> averages;

    private MetricsCollector() {
        this.counters = new ConcurrentHashMap<>();
        this.gauges = new ConcurrentHashMap<>();
        this.averages = new ConcurrentHashMap<>();
    }

    public static synchronized MetricsCollector getInstance() {
        if (instance == null) {
            instance = new MetricsCollector();
        }
        return instance;
    }

    public void incrementCounter(String metric) {
        counters.computeIfAbsent(metric, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void setGauge(String metric, long value) {
        gauges.computeIfAbsent(metric, k -> new AtomicLong(0)).set(value);
    }

    public void updateAverage(String metric, double value) {
        averages.compute(metric, (k, v) -> v == null ? value : (v + value) / 2);
    }

    public Map<String, Long> getCounters() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        counters.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getGauges() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        gauges.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Double> getAverages() {
        return new ConcurrentHashMap<>(averages);
    }

    public void reset() {
        counters.clear();
        gauges.clear();
        averages.clear();
    }
}
