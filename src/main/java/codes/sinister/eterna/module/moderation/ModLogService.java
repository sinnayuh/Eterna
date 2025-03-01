package codes.sinister.eterna.module.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Instant;

public class ModLogService {
    
    // Different action types for moderation logs
    public enum ModAction {
        BAN(Color.RED, "User Banned"),
        UNBAN(Color.GREEN, "User Unbanned"),
        KICK(Color.ORANGE, "User Kicked"),
        MUTE(Color.YELLOW, "User Muted"),
        UNMUTE(Color.CYAN, "User Unmuted"),
        WARN(Color.YELLOW, "User Warned");

        private final Color color;
        private final String title;

        ModAction(Color color, String title) {
            this.color = color;
            this.title = title;
        }

        public Color getColor() {
            return color;
        }

        public String getTitle() {
            return title;
        }
    }

    /**
     * Log a moderation action to the configured log channel
     *
     * @param guild The guild where the action took place
     * @param moderator The user who performed the action
     * @param target The user who was the target of the action
     * @param action The type of moderation action
     * @param reason The reason for the action (nullable)
     */
    public static void logAction(@NotNull Guild guild, @NotNull User moderator, 
                                @NotNull User target, @NotNull ModAction action, 
                                @Nullable String reason) {
        
        String guildId = guild.getId();
        ModLogConfig config = ModLogConfig.forGuild(guildId);
        
        // Check if logging is enabled and a channel is set
        if (!config.isLoggingEnabled()) {
            return;
        }
        
        String logChannelId = config.getLogChannelId();
        TextChannel logChannel = guild.getTextChannelById(logChannelId);
        
        if (logChannel == null) {
            // Channel no longer exists, disable logging
            config.disableLogging();
            return;
        }
        
        logChannel.sendMessageEmbeds(createLogEmbed(moderator, target, action, reason))
            .queue();
    }
    
    /**
     * Creates the embed for the log message
     */
    private static MessageEmbed createLogEmbed(@NotNull User moderator, @NotNull User target, 
                                            @NotNull ModAction action, @Nullable String reason) {
        
        return new EmbedBuilder()
            .setTitle(action.getTitle())
            .setColor(action.getColor())
            .addField("Moderator", moderator.getAsMention() + " (" + moderator.getId() + ")", false)
            .addField("User", target.getAsMention() + " (" + target.getId() + ")", false)
            .addField("Reason", reason != null ? reason : "No reason provided", false)
            .setThumbnail(target.getEffectiveAvatarUrl())
            .setFooter("User ID: " + target.getId())
            .setTimestamp(Instant.now())
            .build();
    }
}
