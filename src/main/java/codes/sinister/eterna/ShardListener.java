package codes.sinister.eterna;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener for shard-related events
 */
public class ShardListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardListener.class);
    
    private final Bot bot;
    private final AtomicInteger readyShards = new AtomicInteger(0);
    
    /**
     * Constructor that accepts the Bot instance.
     * Note: We can't use Neptune's auto-injection here since we need this reference
     * during startup before Neptune is initialized.
     */
    public ShardListener(Bot bot) {
        this.bot = bot;
    }
    
    /**
     * No-args constructor for Neptune (not used in our case since we register manually).
     * Keeping it for completeness.
     */
    public ShardListener() {
        this.bot = null;
    }
    
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        int totalShards = event.getJDA().getShardInfo().getShardTotal();
        
        LOGGER.info("Shard {} of {} is ready, serving {} guilds", 
            shardId, 
            totalShards,
            event.getJDA().getGuildCache().size());
        
        int readyCount = readyShards.incrementAndGet();
        if (readyCount >= totalShards && bot != null) {
            LOGGER.info("All {} shards are ready! Initializing Neptune...", totalShards);
            bot.initializeNeptune();
        }
    }
}
