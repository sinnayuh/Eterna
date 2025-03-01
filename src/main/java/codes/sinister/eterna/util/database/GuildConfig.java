package codes.sinister.eterna.util.database;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles guild-specific configuration settings.
 * Uses a cache to reduce database reads.
 */
public class GuildConfig {
    private static final Map<String, GuildConfig> CACHE = new ConcurrentHashMap<>();
    
    private final String guildId;
    private final Map<String, Object> settings;
    
    private GuildConfig(String guildId, Map<String, Object> settings) {
        this.guildId = guildId;
        this.settings = settings;
    }
    
    /**
     * Gets a guild configuration, loading from cache if available
     */
    public static GuildConfig forGuild(@NotNull String guildId) {
        return CACHE.computeIfAbsent(guildId, id -> {
            Document doc = Mongo.GUILDS.collection().find(
                Filters.eq("guild_id", id)
            ).first();
            
            if (doc == null) {
                Map<String, Object> defaultSettings = new HashMap<>();
                defaultSettings.put("prefix", "!");
                defaultSettings.put("welcome_enabled", false);
                defaultSettings.put("log_channel", null);
                
                Document newDoc = new Document("guild_id", id)
                    .append("settings", defaultSettings);
                
                Mongo.GUILDS.collection().insertOne(newDoc);
                return new GuildConfig(id, defaultSettings);
            }
            
            Map<String, Object> settings = (Map<String, Object>) doc.get("settings", Document.class);
            return new GuildConfig(id, settings);
        });
    }
    
    /**
     * Gets a specific setting value
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        return settings.containsKey(key) 
            ? (T) settings.get(key) 
            : defaultValue;
    }
    
    /**
     * Updates a setting and saves to database
     */
    public void updateSetting(String key, Object value) {
        settings.put(key, value);
        
        // Update in database
        Bson filter = Filters.eq("guild_id", guildId);
        Bson update = Updates.set("settings." + key, value);
        
        Mongo.GUILDS.collection().updateOne(filter, update);
    }
    
    /**
     * Clear cache for a guild (use when guild is removed or bot is removed from guild)
     */
    public static void clearCache(String guildId) {
        CACHE.remove(guildId);
    }
    
    /**
     * Clear entire cache (use during shutdown)
     */
    public static void clearAllCache() {
        CACHE.clear();
    }
}
