package codes.sinister.eterna.util.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;

public final class Config {
    private static final Dotenv dotenv = Dotenv.configure().load();

    private Config() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static @NotNull String get(@NotNull String key) {
        return dotenv.get(key);
    }

    public static @NotNull String getBotToken() {
        return get("BOT_TOKEN");
    }

    public static @NotNull String getMongoUri() {
        return get("MONGO_URI");
    }

    public static @NotNull String getMongoDatabaseName() {
        return get("MONGO_DB_NAME");
    }
    
    /**
     * Get development guild IDs where commands should be registered
     * @return Comma-separated list of guild IDs
     */
    public static @NotNull String getDevGuildIds() {
        return get("DEV_GUILD_IDS");
    }
}
