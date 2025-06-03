package vishalmysore.agenticmesh.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistence manager for storing agent and mesh state
 */
public class PersistenceManager {
    private static PersistenceManager instance;
    private final ObjectMapper objectMapper;
    private final Path storageDir;
    private final Map<String, Object> memoryCache;

    private PersistenceManager() {
        this.objectMapper = new ObjectMapper();
        this.storageDir = Paths.get(System.getProperty("user.home"), ".agenticmesh");
        this.memoryCache = new ConcurrentHashMap<>();
        initializeStorage();
    }

    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage", e);
        }
    }

    public void store(String key, Object value) {
        try {
            Path file = storageDir.resolve(key + ".json");
            objectMapper.writeValue(file.toFile(), value);
            memoryCache.put(key, value);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store value for key: " + key, e);
        }
    }

    public <T> T load(String key, Class<T> type) {
        // Check memory cache first
        if (memoryCache.containsKey(key)) {
            return type.cast(memoryCache.get(key));
        }

        try {
            Path file = storageDir.resolve(key + ".json");
            if (!Files.exists(file)) {
                return null;
            }
            T value = objectMapper.readValue(file.toFile(), type);
            memoryCache.put(key, value);
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    public void delete(String key) {
        try {
            Path file = storageDir.resolve(key + ".json");
            Files.deleteIfExists(file);
            memoryCache.remove(key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete value for key: " + key, e);
        }
    }

    public void clear() {
        try {
            Files.walk(storageDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + file, e);
                    }
                });
            memoryCache.clear();
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear storage", e);
        }
    }
}
