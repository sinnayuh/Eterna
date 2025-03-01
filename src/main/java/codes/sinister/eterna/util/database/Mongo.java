package codes.sinister.eterna.util.database;

import java.util.Optional;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.lang.Nullable;

import codes.sinister.eterna.util.config.Config;

public enum Mongo {
    GUILDS,      // Stores guild-specific configuration
    USERS,       // Stores user profiles and data
    ;

    private static @Nullable MongoClient client;
    private static @Nullable MongoDatabase database;

    public static void connect(@NotNull String URI) {
        client = MongoClients.create(URI);
        database = client.getDatabase(Config.getMongoDatabaseName());
        
        for (Mongo collection : values()) {
            if (!collectionExists(collection.name().toLowerCase())) {
                database.createCollection(collection.name().toLowerCase());
            }
        }
        
        IndexOptions uniqueOptions = new IndexOptions().unique(true);
        
        database.getCollection(GUILDS.name().toLowerCase())
                .createIndex(Indexes.ascending("guild_id"), uniqueOptions);
                
        database.getCollection(USERS.name().toLowerCase())
                .createIndex(Indexes.ascending("user_id"), uniqueOptions);
    }
    
    private static boolean collectionExists(String collectionName) {
        if (database == null) {
            throw new IllegalStateException("Database connection is not established");
        }
        
        for (String name : database.listCollectionNames()) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public @NotNull MongoCollection<Document> collection() {
        return Optional.ofNullable(database)
            .map(db -> db.getCollection(name().toLowerCase()))
            .orElseThrow(() -> new IllegalStateException("MongoDB is not connected"));
    }
}