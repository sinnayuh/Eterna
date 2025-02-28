package codes.sinister.eterna;

import codes.sinister.eterna.config.Config;
import codes.sinister.eterna.database.Mongo;
import gg.flyte.neptune.Neptune;
import gg.flyte.neptune.annotation.Instantiate;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final @NotNull List<Activity> ACTIVITIES = List.of(
            Activity.watching("%shards% shards | %guilds% servers")
    );

    private final ShardManager shardManager;
    private final ScheduledExecutorService scheduler;

    @Instantiate
    private final ShardManager instantiatedShardManager;
    
    private boolean neptuneInitialized = false;

    public Bot() throws InterruptedException {
        Mongo.connect(Config.getMongoUri());

        int shardsTotal = -1;

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getBotToken())
                .setActivity(Activity.playing("starting up..."))
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS)
                .disableCache(CacheFlag.ACTIVITY)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setShardsTotal(shardsTotal)
                .addEventListeners(new ShardListener(this));

        this.shardManager = builder.build();
        this.instantiatedShardManager = this.shardManager;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        setupActivities();
    }

    /**
     * Initialize Neptune when all shards are ready.
     * This is called from the ShardListener when the last shard is ready.
     */
    public void initializeNeptune() {
        if (neptuneInitialized) {
            return;
        }
        
        try {
            if (!shardManager.getShards().isEmpty()) {
                List<String> devGuildIds = getDevGuildIds();
                
                List<Guild> devGuilds = devGuildIds.stream()
                    .map(id -> shardManager.getGuildById(id))
                    .filter(guild -> guild != null)
                    .collect(Collectors.toList());
                
                if (devGuilds.isEmpty()) {
                    LOGGER.warn("No development guilds found! Commands will be registered globally.");
                } else {
                    LOGGER.info("Registering commands for {} development guilds: {}", 
                        devGuilds.size(), 
                        devGuilds.stream().map(Guild::getName).collect(Collectors.joining(", ")));
                }
                
                Neptune.Builder neptuneBuilder = new Neptune.Builder(shardManager.getShards().get(0), this)
                        .clearCommands(true)
                        .registerAllListeners(true);
                
                if (!devGuilds.isEmpty()) {
                    neptuneBuilder.addGuilds(devGuilds.toArray(new Guild[0]));
                }
                
                neptuneBuilder.create();
                neptuneInitialized = true;
                LOGGER.info("Neptune initialized successfully");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Neptune", e);
        }
    }
    
    /**
     * Get development guild IDs from configuration
     */
    private List<String> getDevGuildIds() {
        try {
            String devGuildsStr = Config.getDevGuildIds();
            return Arrays.asList(devGuildsStr.split(","));
        } catch (Exception e) {
            LOGGER.warn("Failed to get development guild IDs from config", e);
            return List.of();
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    private void setupActivities() {
        scheduler.scheduleWithFixedDelay(() -> {
            Activity activity = ACTIVITIES.get(ThreadLocalRandom.current().nextInt(ACTIVITIES.size()));
            String activityName = activity.getName()
                    .replace("%shards%", String.valueOf(shardManager.getShardsTotal()))
                    .replace("%guilds%", String.valueOf(shardManager.getGuildCache().size()));            
            shardManager.setActivity(Activity.of(activity.getType(), activityName));
        }, 0L, 30L, TimeUnit.SECONDS);
    }
    
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (shardManager != null) {
            shardManager.shutdown();
        }
    }
}
