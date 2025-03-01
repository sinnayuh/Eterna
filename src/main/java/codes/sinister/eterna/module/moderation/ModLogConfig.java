package codes.sinister.eterna.module.moderation;

import codes.sinister.eterna.util.database.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModLogConfig {
    private static final Map<String, ModLogConfig> CACHE = new ConcurrentHashMap<>();

    private final String guildId;
    private String logChannelId;
    private boolean loggingEnabled;

    private ModLogConfig(String guildId) {
        this.guildId = guildId;
        loadFromDatabase();
    }

    /**
     * Get configuration for a specific guild
     */
    public static @NotNull ModLogConfig forGuild(@NotNull String guildId) {
        return CACHE.computeIfAbsent(guildId, ModLogConfig::new);
    }

    /**
     * Get the log channel ID for this guild
     */
    public @Nullable String getLogChannelId() {
        return logChannelId;
    }

    /**
     * Check if logging is enabled for this guild
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled && logChannelId != null;
    }

    /**
     * Set the log channel and enable logging
     */
    public void setLogChannel(String channelId) {
        this.logChannelId = channelId;
        this.loggingEnabled = true;
        saveToDatabase();
    }

    /**
     * Disable logging for this guild
     */
    public void disableLogging() {
        this.loggingEnabled = false;
        saveToDatabase();
    }

    /**
     * Load configuration from database
     */
    private void loadFromDatabase() {
        Document doc = Mongo.GUILDS.collection().find(Filters.eq("guild_id", guildId)).first();
        if (doc != null && doc.containsKey("mod_log")) {
            Document modLogConfig = doc.get("mod_log", Document.class);
            this.logChannelId = modLogConfig.getString("channel_id");
            this.loggingEnabled = modLogConfig.getBoolean("enabled", false);
        } else {
            this.logChannelId = null;
            this.loggingEnabled = false;
        }
    }

    /**
     * Save configuration to database
     */
    private void saveToDatabase() {
        Document modLogConfig = new Document()
                .append("channel_id", logChannelId)
                .append("enabled", loggingEnabled);

        Mongo.GUILDS.collection().updateOne(
                Filters.eq("guild_id", guildId),
                Updates.set("mod_log", modLogConfig),
                new UpdateOptions().upsert(true)
        );
    }

    /**
     * Clear cache for a specific guild
     */
    public static void clearCache(String guildId) {
        CACHE.remove(guildId);
    }

    /**
     * Clear all cached configurations
     */
    public static void clearAllCache() {
        CACHE.clear();
    }
}
