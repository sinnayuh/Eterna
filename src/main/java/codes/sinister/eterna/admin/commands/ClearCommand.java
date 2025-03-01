package codes.sinister.eterna.admin.commands;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;

public class ClearCommand {

    @Command(
            name = "clearcommands",
            description = "Clears bot commands (Owner only)",
            permissions = Permission.ADMINISTRATOR
    )
    public void onCommand(
            SlashCommandInteractionEvent event,
            @Option(description = "Clear global commands", required = false) Boolean global,
            @Option(description = "Clear guild commands", required = false) Boolean guild
    ) {
        if (!event.getUser().getId().equals("204608845325008906")) {
            MessageEmbed errorEmbed = new EmbedBuilder()
                    .setColor(0xED4245)
                    .setTitle("❌ Access Denied")
                    .setDescription("This command can only be used by the bot owner.")
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        StringBuilder responseBuilder = new StringBuilder();

        try {
            boolean clearGlobal = global == null ? guild == null : global;
            boolean clearGuild = guild == null ? global == null : guild;

            if (clearGlobal) {
                event.getJDA().updateCommands().queue(success -> {
                    responseBuilder.append("✅ Successfully cleared global commands\n");
                });
            }

            if (clearGuild) {
                Guild guildId = event.getGuild();
                if (guildId != null) {
                    guildId.updateCommands().queue(success -> {
                        responseBuilder.append("✅ Successfully cleared commands in Guild " + event.getGuild().getName() + "\n");
                    });
                } else {
                    responseBuilder.append("❌ Cannot clear guild commands in DMs\n");
                }
            }

            if (!clearGlobal && !clearGuild) {
                responseBuilder.append("❌ No command scope selected to clear\n");
            } else {
                responseBuilder.append("\n**Note:** Restart the bot to re-register commands.");
            }

            MessageEmbed successEmbed = new EmbedBuilder()
                    .setColor(responseBuilder.toString().contains("❌") ? 0xED4245 : 0x57F287)
                    .setTitle("Command Clearance Status")
                    .setDescription(responseBuilder.toString())
                    .setTimestamp(Instant.now())
                    .setFooter("Requested by " + event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl())
                    .build();

            event.getHook().sendMessageEmbeds(successEmbed).setEphemeral(true).queue();
        } catch (Exception e) {
            MessageEmbed errorEmbed = new EmbedBuilder()
                    .setColor(0xED4245)
                    .setTitle("❌ Command Clearance Failed")
                    .setDescription("An error occurred while clearing commands:\n```" + e.getMessage() + "```")
                    .setTimestamp(Instant.now())
                    .setFooter("Requested by " + event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl())
                    .build();

            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
        }
    }
}
