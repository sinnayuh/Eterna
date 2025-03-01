package codes.sinister.eterna.admin.commands;

import codes.sinister.eterna.util.Constant;
import com.sun.management.OperatingSystemMXBean;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Inject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class StatsCommand {

    @Inject
    private ShardManager instantiatedShardManager;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");

    @Command(
            name = "stats",
            description = "Get statistics about the bot"
    )
    public void onStats(SlashCommandInteractionEvent event) {
        ShardManager shardManager = instantiatedShardManager;
        SelfUser bot = Objects.requireNonNull(event.getGuild()).getJDA().getSelfUser();
        String uptime = Constant.getUptime();

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

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double processCpuLoad = osBean.getProcessCpuLoad();
        String cpuLoad = String.format("%.0f%%", processCpuLoad * 100);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .setColor(Color.decode("#fc9a9a"))
                .setThumbnail(bot.getEffectiveAvatarUrl())
                .addField("Name", bot.getEffectiveName(), true)
                .addField("ID", bot.getId(), true)
                .addField("Created by", "<@204608845325008906>", true)
                .addField("Total Shards", String.valueOf(shardCount), true)
                .addField("Current Shard", "Shard " + currentShardId + " (" + currentShardGuilds + " servers)", true)
                .addField("Uptime", uptime, true)
                .addField("Servers", String.valueOf(serverCount), true)
                .addField("Users", String.valueOf(userCount), true)
                .addField("Language ", "Java", true)
                .addField("Memory Usage", usedMemory + "MB / " + totalMemory + "MB", true)
                .addField("CPU Usage", cpuLoad + " / 100%", true)
                .addField("Java Version", System.getProperty("java.version"), true)
                .setFooter("Joined Discord on " + bot.getTimeCreated().format(dtf))
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }
}
