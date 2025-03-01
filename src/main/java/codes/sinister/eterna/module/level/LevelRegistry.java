package codes.sinister.eterna.module.level;

import codes.sinister.eterna.util.database.UserProfile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LevelRegistry {
    private static final Map<String, UserLevelData> CACHE = new ConcurrentHashMap<>();

    private LevelRegistry() {
    }

    /**
     * Get level data for a user in a specific guild
     * @param userId Discord user ID
     * @param guildId Discord guild ID
     * @return UserLevelData for the user
     */
    public static @NotNull UserLevelData getLevelData(@NotNull String userId, @NotNull String guildId) {
        String key = buildCacheKey(userId, guildId);
        return CACHE.computeIfAbsent(key, k -> {
            UserProfile profile = UserProfile.forUser(userId);
            return new UserLevelData(profile, guildId);
        });
    }

    /**
     * Clears cache for a specific user in a guild
     */
    public static void clearCache(@NotNull String userId, @NotNull String guildId) {
        CACHE.remove(buildCacheKey(userId, guildId));
    }

    /**
     * Clears all level data cache
     */
    public static void clearAllCache() {
        CACHE.clear();
    }

    private static String buildCacheKey(String userId, String guildId) {
        return userId + ":" + guildId;
    }
}