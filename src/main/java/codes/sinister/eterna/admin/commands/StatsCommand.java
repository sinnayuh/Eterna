package codes.sinister.eterna.admin.commands;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Inject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.awt.*;
import java.time.Instant;

public class StatsCommand {

    @Inject
    private ShardManager instantiatedShardManager;

    @Command(
            name = "stats",
            description = "Get statistics about the bot"
    )
    public void onStats(SlashCommandInteractionEvent event) {
        ShardManager shardManager = instantiatedShardManager;

        if (shardManager == null) {
            event.reply("⚠️ Bot statistics are currently unavailable. The ShardManager could not be accessed.").setEphemeral(true).queue();
            return;
        }

        long serverCount = shardManager.getGuildCache().size();
        int userCount = shardManager.getGuildCache().stream().mapToInt(guild -> guild.getMemberCount()).sum();
        int shardCount = shardManager.getShardsTotal();

        JDA jda = event.getJDA();
        int currentShardId = jda.getShardInfo().getShardId();
        long currentShardGuilds = jda.getGuildCache().size();

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .setColor(Color.decode("#fc9a9a"))
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
