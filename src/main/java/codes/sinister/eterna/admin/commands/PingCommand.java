package codes.sinister.eterna.admin.commands;

import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

public class PingCommand {
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
                    .setColor(Color.decode("#fc9a9a"))
                    .addField("Gateway Ping", gatewayPing + "ms", true)
                    .addField("REST Ping", ping + "ms", true)
                    .setFooter("Requested by " + event.getUser().getAsTag())
                    .setTimestamp(Instant.now());

            hook.sendMessageEmbeds(embed.build()).queue();
        });
    }
}
