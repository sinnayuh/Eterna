package codes.sinister.eterna.module.moderation.command;

import codes.sinister.eterna.module.moderation.ModLogService;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class TimeoutCommand {

    @Command(name = "timeout", description = "Timeout a user", permissions = Permission.MODERATE_MEMBERS)
    public void onTimeout(@NotNull SlashCommandInteractionEvent event,
                         @Option(description = "The user to timeout") @NotNull User user,
                         @Option(description = "Minutes to timeout for") int minutes,
                         @Option(description = "Reason for the timeout", required = false) String reason) {
        
        String finalReason = (reason != null) ? reason : "No reason provided";
        
        // Simple validation 
        if (minutes <= 0 || minutes > 40320) { // 28 days max
            event.reply("Timeout duration must be between 1 minute and 28 days!").setEphemeral(true).queue();
            return;
        }
        
        if (user.isBot()) {
            event.reply("You cannot timeout a bot.").setEphemeral(true).queue();
            return;
        }
        
        // Use guild.timeoutFor(userId) instead of retrieving the member first
        event.getGuild().timeoutFor(user, Duration.ofMinutes(minutes))
            .reason(finalReason)
            .queue(
                success -> {
                    // Log the action
                    ModLogService.logAction(
                        event.getGuild(),
                        event.getUser(),
                        user,
                        ModLogService.ModAction.MUTE,
                        finalReason + " (Duration: " + minutes + " minutes)"
                    );
                    
                    // Send confirmation
                    event.reply("✅ " + user.getAsMention() + " has been timed out for " + minutes + " minutes.").setEphemeral(true).queue();
                },
                error -> {
                    if (error.getMessage().contains("Unknown User") || error.getMessage().contains("Unknown Member")) {
                        event.reply("❌ This user is not in the server.").setEphemeral(true).queue();
                    } else if (error.getMessage().contains("hierarchy")) {
                        event.reply("❌ I cannot timeout this user due to role hierarchy.").setEphemeral(true).queue();
                    } else {
                        event.reply("❌ Failed to timeout user: " + error.getMessage()).setEphemeral(true).queue();
                    }
                }
            );
    }
    
    @Command(name = "untimeout", description = "Remove timeout from a user", permissions = Permission.MODERATE_MEMBERS)
    public void onUntimeout(@NotNull SlashCommandInteractionEvent event,
                           @Option(description = "The user to remove timeout from") @NotNull User user,
                           @Option(description = "Reason for removing timeout", required = false) String reason) {
        
        String finalReason = (reason != null) ? reason : "No reason provided";
        
        if (user.isBot()) {
            event.reply("Bots cannot be timed out.").setEphemeral(true).queue();
            return;
        }
        
        // Use guild.removeTimeout(userId) instead of retrieving member first
        event.getGuild().removeTimeout(user)
            .reason(finalReason)
            .queue(
                success -> {
                    // Log the action
                    ModLogService.logAction(
                        event.getGuild(),
                        event.getUser(),
                        user,
                        ModLogService.ModAction.UNMUTE,
                        finalReason
                    );
                    
                    // Send confirmation
                    event.reply("✅ Timeout removed from " + user.getAsMention()).setEphemeral(true).queue();
                },
                error -> {
                    if (error.getMessage().contains("Unknown User") || error.getMessage().contains("Unknown Member")) {
                        event.reply("❌ This user is not in the server.").setEphemeral(true).queue();
                    } else {
                        event.reply("❌ Failed to remove timeout: " + error.getMessage()).setEphemeral(true).queue();
                    }
                }
            );
    }
}
