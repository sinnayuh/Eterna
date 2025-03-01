package codes.sinister.eterna.module.moderation;

import codes.sinister.eterna.util.database.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WarningService {

    /**
     * Add a warning to a user
     *
     * @param warning The warning to add
     * @return The updated warning count for the user in this guild
     */
    public static int addWarning(@NotNull Warning warning) {
        // Get the user's document or create a new one
        Document userDoc = Mongo.USERS.collection().find(
                Filters.eq("user_id", warning.getUserId())
        ).first();

        if (userDoc == null) {
            userDoc = new Document("user_id", warning.getUserId())
                    .append("warnings", new ArrayList<Document>());
            Mongo.USERS.collection().insertOne(userDoc);
        }

        // Create warning document
        Document warningDoc = new Document("id", warning.getId())
                .append("guild_id", warning.getGuildId())
                .append("moderator_id", warning.getModeratorId())
                .append("reason", warning.getReason())
                .append("timestamp", warning.getTimestamp().toString());

        // Update user document with new warning
        Mongo.USERS.collection().updateOne(
                Filters.eq("user_id", warning.getUserId()),
                Updates.push("warnings", warningDoc)
        );

        // Count and return warnings for this guild
        return getWarningCount(warning.getUserId(), warning.getGuildId());
    }

    /**
     * Get all warnings for a user in a specific guild
     */
    public static List<Warning> getWarnings(@NotNull String userId, @NotNull String guildId) {
        Document userDoc = Mongo.USERS.collection().find(
                Filters.eq("user_id", userId)
        ).first();

        List<Warning> warnings = new ArrayList<>();
        
        if (userDoc != null && userDoc.containsKey("warnings")) {
            List<Document> warningDocs = userDoc.getList("warnings", Document.class);
            
            for (Document doc : warningDocs) {
                if (doc.getString("guild_id").equals(guildId)) {
                    warnings.add(new Warning(
                            doc.getString("id"),
                            userId,
                            guildId,
                            doc.getString("moderator_id"),
                            doc.getString("reason"),
                            Instant.parse(doc.getString("timestamp"))
                    ));
                }
            }
        }
        
        return warnings;
    }

    /**
     * Get warning count for a user in a guild
     */
    public static int getWarningCount(@NotNull String userId, @NotNull String guildId) {
        return getWarnings(userId, guildId).size();
    }

    /**
     * Remove a specific warning by ID
     *
     * @return true if successfully removed
     */
    public static boolean removeWarning(@NotNull String userId, @NotNull String warningId) {
        Bson filter = Filters.eq("user_id", userId);
        Bson update = Updates.pull("warnings", Filters.eq("id", warningId));
        
        return Mongo.USERS.collection().updateOne(filter, update).getModifiedCount() > 0;
    }

    /**
     * Clear all warnings for a user in a guild
     *
     * @return The number of warnings cleared
     */
    public static int clearWarnings(@NotNull String userId, @NotNull String guildId) {
        int count = getWarningCount(userId, guildId);
        
        Bson filter = Filters.eq("user_id", userId);
        Bson update = Updates.pull("warnings", Filters.eq("guild_id", guildId));
        
        Mongo.USERS.collection().updateOne(filter, update);
        
        return count;
    }
}
