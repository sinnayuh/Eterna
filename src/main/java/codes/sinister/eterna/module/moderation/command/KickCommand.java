package codes.sinister.eterna.module.moderation.command;

import codes.sinister.eterna.module.moderation.ModLogService;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

public final class KickCommand {

    @Command(name = "kick", description = "Kick a user from the server", permissions = Permission.KICK_MEMBERS)
    public void onKick(@NotNull SlashCommandInteractionEvent event,
                      @Option(description = "The user to kick") @NotNull User user,
                      @Option(description = "Reason for the kick", required = false) String reason) {
        
        String finalReason = (reason != null) ? reason : "No reason provided";
        
        if (user.isBot()) {
            event.reply("You cannot kick a bot.").setEphemeral(true).queue();
            return;
        }
        
        if (user.getId().equals(event.getUser().getId())) {
            event.reply("You cannot kick yourself.").setEphemeral(true).queue();
            return;
        }
        
        // Kick the user directly from the guild
        event.getGuild().kick(user).reason(finalReason).queue(
            success -> {
                // Log action first
                ModLogService.logAction(
                    event.getGuild(),
                    event.getUser(),
                    user,
                    ModLogService.ModAction.KICK,
                    finalReason
                );
                
                // Send confirmation
                event.reply("✅ " + user.getAsMention() + " has been kicked.").setEphemeral(true).queue();
            },
            error -> {
                if (error.getMessage().contains("Unknown User") || error.getMessage().contains("Unknown Member")) {
                    event.reply("❌ This user is not in the server.").setEphemeral(true).queue();
                } else if (error.getMessage().contains("hierarchy")) {
                    event.reply("❌ I cannot kick this user due to role hierarchy.").setEphemeral(true).queue();
                } else {
                    event.reply("❌ Failed to kick user: " + error.getMessage()).setEphemeral(true).queue();
                }
            }
        );
    }
}
