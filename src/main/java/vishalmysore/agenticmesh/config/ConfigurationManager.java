package vishalmysore.agenticmesh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Configuration manager for AgenticMesh system
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private final Map<String, Object> config;
    private final ObjectMapper objectMapper;
    private final Path configFile;

    private ConfigurationManager() {
        this.config = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.configFile = Paths.get(System.getProperty("user.home"), ".agenticmesh", "config.json");
        loadConfiguration();
    }

    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        try {
            if (Files.exists(configFile)) {
                Map<String, Object> loaded = objectMapper.readValue(configFile.toFile(), Map.class);
                config.putAll(loaded);
            } else {
                // Load defaults
                config.put("maxAgents", 100);
                config.put("messageQueueSize", 1000);
                config.put("healthCheckInterval", 30);
                config.put("persistenceEnabled", true);
                config.put("metricsEnabled", true);
                saveConfiguration();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private void saveConfiguration() {
        try {
            Files.createDirectories(configFile.getParent());
            objectMapper.writeValue(configFile.toFile(), config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    public void setValue(String key, Object value) {
        config.put(key, value);
        saveConfiguration();
    }

    public <T> T getValue(String key, Class<T> type) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public <T> T getValue(String key, Class<T> type, T defaultValue) {
        T value = getValue(key, type);
        return value != null ? value : defaultValue;
    }

    public Map<String, Object> getAllSettings() {
        return new HashMap<>(config);
    }

    public void reset() {
        config.clear();
        loadConfiguration();
    }
}
