package codes.sinister.eterna.module.temp;

import codes.sinister.eterna.util.database.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempChannelConfig {
    private static final Map<String, TempChannelConfig> CACHE = new ConcurrentHashMap<>();

    private final String guildId;
    private String joinChannelId;
    private String categoryId;

    private TempChannelConfig(String guildId) {
        this.guildId = guildId;
        loadFromDatabase();
    }

    /**
     * Get configuration for a specific guild
     */
    public static @NotNull TempChannelConfig forGuild(@NotNull String guildId) {
        return CACHE.computeIfAbsent(guildId, TempChannelConfig::new);
    }

    /**
     * Get join channel ID for this guild
     */
    public @Nullable String getJoinChannelId() {
        return joinChannelId;
    }

    /**
     * Get category ID for this guild
     */
    public @Nullable String getCategoryId() {
        return categoryId;
    }

    /**
     * Set configuration values
     */
    public void setConfig(String joinChannelId, String categoryId) {
        this.joinChannelId = joinChannelId;
        this.categoryId = categoryId;
        saveToDatabase();
    }

    /**
     * Check if configuration is set
     */
    public boolean isConfigured() {
        return joinChannelId != null && categoryId != null;
    }

    /**
     * Load configuration from database
     */
    private void loadFromDatabase() {
        Document doc = Mongo.GUILDS.collection().find(Filters.eq("guild_id", guildId)).first();
        if (doc != null && doc.containsKey("temp_channels")) {
            Document tempConfig = doc.get("temp_channels", Document.class);
            this.joinChannelId = tempConfig.getString("join_channel_id");
            this.categoryId = tempConfig.getString("category_id");
        }
    }

    /**
     * Save configuration to database
     */
    private void saveToDatabase() {
        Document tempConfig = new Document()
                .append("join_channel_id", joinChannelId)
                .append("category_id", categoryId);

        Mongo.GUILDS.collection().updateOne(
                Filters.eq("guild_id", guildId),
                Updates.set("temp_channels", tempConfig),
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