package codes.sinister.eterna.module.moderation.command;

import codes.sinister.eterna.module.moderation.ModLogConfig;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public final class ModLogCommand {

    @Command(name = "modlog", description = "Configure the moderation log channel", permissions = Permission.ADMINISTRATOR)
    public void onModLog(@NotNull SlashCommandInteractionEvent event,
                        @Option(description = "What action to perform with the mod log", autocomplete = {"SETUP", "DISABLE", "STATUS"}) @NotNull String action,
                        @Option(description = "The channel to use for moderation logs", required = false) TextChannel channel) {
        
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        ModLogConfig config = ModLogConfig.forGuild(guildId);
        
        try {
            String actionUpperCase = action.toUpperCase();
            
            if ("SETUP".equals(actionUpperCase)) {
                if (channel == null) {
                    event.reply("You need to specify a channel to set up the mod log.").setEphemeral(true).queue();
                    return;
                }
                
                if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)) {
                    event.reply("I need permissions to send messages and embeds in " + channel.getAsMention()).setEphemeral(true).queue();
                    return;
                }
                
                config.setLogChannel(channel.getId());
                event.reply("Moderation logs will now be sent to " + channel.getAsMention()).setEphemeral(true).queue();
                
            } else if ("DISABLE".equals(actionUpperCase)) {
                config.disableLogging();
                event.reply("Moderation logging has been disabled.").setEphemeral(true).queue();
                
            } else if ("STATUS".equals(actionUpperCase)) {
                if (config.isLoggingEnabled()) {
                    TextChannel logChannel = event.getGuild().getTextChannelById(config.getLogChannelId());
                    String channelMention = logChannel != null ? logChannel.getAsMention() : "Unknown Channel (deleted?)";
                    event.reply("Moderation logging is enabled. Logs are sent to " + channelMention).setEphemeral(true).queue();
                } else {
                    event.reply("Moderation logging is disabled.").setEphemeral(true).queue();
                }
                
            } else {
                event.reply("Invalid action. Use SETUP, DISABLE, or STATUS.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("An error occurred while processing the command. Please try again later.").setEphemeral(true).queue();
        }
    }
}
