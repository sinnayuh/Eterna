package codes.sinister.eterna.database;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles user-specific data across all guilds
 */
public class UserProfile {
    private static final Map<String, UserProfile> CACHE = new ConcurrentHashMap<>();
    
    private final String userId;
    private final Map<String, Object> globalData;
    private final Map<String, Map<String, Object>> guildData;
    
    private UserProfile(String userId, Map<String, Object> globalData, Map<String, Map<String, Object>> guildData) {
        this.userId = userId;
        this.globalData = globalData;
        this.guildData = guildData;
    }
    
    /**
     * Gets a user profile, loading from cache if available
     */
    public static UserProfile forUser(@NotNull String userId) {
        return CACHE.computeIfAbsent(userId, id -> {
            Document doc = Mongo.USERS.collection().find(
                Filters.eq("user_id", id)
            ).first();
            
            if (doc == null) {
                Map<String, Object> globalData = new HashMap<>();
                Map<String, Map<String, Object>> guildData = new HashMap<>();
                
                Document newDoc = new Document("user_id", id)
                    .append("global", globalData)
                    .append("guilds", new Document());
                
                Mongo.USERS.collection().insertOne(newDoc);
                return new UserProfile(id, globalData, guildData);
            }
            
            Map<String, Object> globalData = (Map<String, Object>) doc.get("global", Document.class);
            
            Map<String, Map<String, Object>> guildData = new HashMap<>();
            Document guildsDoc = doc.get("guilds", Document.class);
            if (guildsDoc != null) {
                for (String guildId : guildsDoc.keySet()) {
                    Map<String, Object> guildSettings = (Map<String, Object>) guildsDoc.get(guildId, Document.class);
                    guildData.put(guildId, guildSettings);
                }
            }
            
            return new UserProfile(id, globalData, guildData);
        });
    }
    
    /**
     * Gets global user data
     */
    @SuppressWarnings("unchecked")
    public <T> T getGlobalData(String key, T defaultValue) {
        return globalData.containsKey(key) 
            ? (T) globalData.get(key) 
            : defaultValue;
    }
    
    /**
     * Updates global user data
     */
    public void updateGlobalData(String key, Object value) {
        globalData.put(key, value);
        
        // Update in database
        Bson filter = Filters.eq("user_id", userId);
        Bson update = Updates.set("global." + key, value);
        
        Mongo.USERS.collection().updateOne(filter, update);
    }
    
    /**
     * Gets guild-specific user data
     */
    @SuppressWarnings("unchecked")
    public <T> T getGuildData(String guildId, String key, T defaultValue) {
        Map<String, Object> guildMap = guildData.computeIfAbsent(guildId, k -> new HashMap<>());
        return guildMap.containsKey(key) 
            ? (T) guildMap.get(key) 
            : defaultValue;
    }
    
    /**
     * Updates guild-specific user data
     */
    public void updateGuildData(String guildId, String key, Object value) {
        Map<String, Object> guildMap = guildData.computeIfAbsent(guildId, k -> new HashMap<>());
        guildMap.put(key, value);
        
        // Update in database
        Bson filter = Filters.eq("user_id", userId);
        Bson update = Updates.set("guilds." + guildId + "." + key, value);
        
        Mongo.USERS.collection().updateOne(filter, update);
    }
    
    /**
     * Clear cache for a user
     */
    public static void clearCache(String userId) {
        CACHE.remove(userId);
    }
    
    /**
     * Clear entire cache (use during shutdown)
     */
    public static void clearAllCache() {
        CACHE.clear();
    }
}
