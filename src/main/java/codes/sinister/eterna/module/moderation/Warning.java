package codes.sinister.eterna.module.moderation;

import java.time.Instant;
import java.util.UUID;

public class Warning {
    private final String id;
    private final String userId;
    private final String guildId;
    private final String moderatorId;
    private final String reason;
    private final Instant timestamp;

    public Warning(String userId, String guildId, String moderatorId, String reason) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.guildId = guildId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.timestamp = Instant.now();
    }

    public Warning(String id, String userId, String guildId, String moderatorId, String reason, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.guildId = guildId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getModeratorId() {
        return moderatorId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
