package codes.sinister.eterna.commands;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Inject;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;

public class BasicCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCommands.class);

    @Inject
    private ShardManager instantiatedShardManager;

    @Command(
            name = "ping",
            description = "Check the bot's latency"
    )
    public void onPing(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        
        event.deferReply().queue(hook -> {
            long ping = System.currentTimeMillis() - event.getTimeCreated().toInstant().toEpochMilli();
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🏓 Pong!")
                    .setColor(Color.GREEN)
                    .addField("Gateway Ping", gatewayPing + "ms", true)
                    .addField("REST Ping", ping + "ms", true)
                    .setFooter("Requested by " + event.getUser().getAsTag())
                    .setTimestamp(Instant.now());
                    
            hook.sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Command(
            name = "info",
            description = "Get information about a user"
    )
    public void onInfo(SlashCommandInteractionEvent event,
        @Option(description = "The user to get info about", required = false) User user) {
        
        User targetUser = user != null ? user : event.getUser();
        Member member = event.getGuild().getMember(targetUser);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("User Information")
                .setColor(member != null ? member.getColor() : Color.BLUE)
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .addField("Username", targetUser.getName(), true)
                .addField("ID", targetUser.getId(), true)
                .addField("Account Created", "<t:" + targetUser.getTimeCreated().toEpochSecond() + ":R>", true);
                
        if (member != null) {
            embed.addField("Joined Server", 
                    member.getTimeJoined() != null ? 
                    "<t:" + member.getTimeJoined().toEpochSecond() + ":R>" : 
                    "Unknown", true);
            
            if (!member.getRoles().isEmpty()) {
                embed.addField("Roles", 
                        member.getRoles().stream()
                              .map(role -> role.getAsMention())
                              .limit(10)
                              .reduce((a, b) -> a + " " + b)
                              .orElse("None"), 
                        false);
            }
        }
        
        embed.setFooter("Requested by " + event.getUser().getName())
             .setTimestamp(Instant.now());
             
        event.replyEmbeds(embed.build()).queue();
    }

    @Command(
            name = "stats",
            description = "Get statistics about the bot"
    )
    public void onStats(SlashCommandInteractionEvent event) {
        ShardManager shardManager = instantiatedShardManager;
        
        if (shardManager == null) {
            event.reply("⚠️ Bot statistics are currently unavailable. The ShardManager could not be accessed.").setEphemeral(true).queue();
            LOGGER.error("ShardManager was null in stats command");
            return;
        }
        
        long serverCount = shardManager.getGuildCache().size();
        int userCount = (int) shardManager.getGuildCache().stream().mapToInt(guild -> guild.getMemberCount()).sum();
        int shardCount = shardManager.getShardsTotal();
        
        JDA jda = event.getJDA();
        int currentShardId = jda.getShardInfo().getShardId();
        long currentShardGuilds = jda.getGuildCache().size();
                
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .setColor(Color.BLUE)
                .addField("Servers", String.valueOf(serverCount), true)
                .addField("Users", String.valueOf(userCount), true)
                .addField("Total Shards", String.valueOf(shardCount), true)
                .addField("Current Shard", "Shard " + currentShardId + " (" + currentShardGuilds + " servers)", true)
                .addField("Memory Usage", usedMemory + "MB / " + totalMemory + "MB", true)
                .addField("Java Version", System.getProperty("java.version"), true)
                .setFooter("Requested by " + event.getUser().getName())
                .setTimestamp(Instant.now());
                
        event.replyEmbeds(embed.build()).queue();
    }
}
