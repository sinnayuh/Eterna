package codes.sinister.eterna.module.moderation.command;

import codes.sinister.eterna.module.moderation.ModLogService;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class TimeoutCommand {

    @Command(name = "timeout", description = "Timeout (mute) a user", permissions = Permission.MODERATE_MEMBERS)
    public void onTimeout(@NotNull SlashCommandInteractionEvent event,
                        @Option(description = "The user to timeout") @NotNull User user,
                        @Option(description = "Duration in minutes") long durationMinutes,
                        @Option(description = "Reason for the timeout", required = false) String reason) {

        if (user.isBot()) {
            event.reply("You can't timeout a bot!").setEphemeral(true).queue();
            return;
        }

        if (durationMinutes <= 0 || durationMinutes > 40320) { // Max 28 days per Discord's limit
            event.reply("Timeout duration must be between 1 minute and 28 days!").setEphemeral(true).queue();
            return;
        }

        event.getGuild().retrieveMember(user).queue(
            member -> {
                if (!event.getGuild().getSelfMember().canInteract(member)) {
                    event.reply("I can't timeout this user due to role hierarchy!").setEphemeral(true).queue();
                    return;
                }

                if (!event.getMember().canInteract(member)) {
                    event.reply("You can't timeout someone with a higher role than you!").setEphemeral(true).queue();
                    return;
                }

                String timeoutReason = reason == null ? "No reason provided" : reason;
                Duration timeoutDuration = Duration.of(durationMinutes, ChronoUnit.MINUTES);

                member.timeoutFor(timeoutDuration)
                    .reason(timeoutReason)
                    .queue(
                        success -> {
                            ModLogService.logAction(
                                event.getGuild(),
                                event.getUser(),
                                user,
                                ModLogService.ModAction.MUTE,
                                timeoutReason + " (Duration: " + durationMinutes + " minutes)"
                            );
                            event.reply("Successfully timed out " + user.getAsMention() + " for " + durationMinutes + " minutes.").setEphemeral(true).queue();
                        },
                        failure -> event.reply("Failed to timeout " + user.getAsMention()).setEphemeral(true).queue()
                    );
            },
            error -> event.reply("Error: The user is not in this server.").setEphemeral(true).queue()
        );
    }

    @Command(name = "untimeout", description = "Remove a timeout from a user", permissions = Permission.MODERATE_MEMBERS)
    public void onRemoveTimeout(@NotNull SlashCommandInteractionEvent event,
                                @Option(description = "The user to remove timeout from") @NotNull User user,
                                @Option(description = "Reason for removing timeout", required = false) String reason) {

        event.getGuild().retrieveMember(user).queue(
            member -> {
                if (!event.getGuild().getSelfMember().canInteract(member)) {
                    event.reply("I can't remove the timeout from this user due to role hierarchy!").setEphemeral(true).queue();
                    return;
                }

                if (!event.getMember().canInteract(member)) {
                    event.reply("You can't remove a timeout from someone with a higher role than you!").setEphemeral(true).queue();
                    return;
                }

                String untimeoutReason = reason == null ? "No reason provided" : reason;

                member.removeTimeout()
                    .reason(untimeoutReason)
                    .queue(
                        success -> {
                            ModLogService.logAction(
                                event.getGuild(),
                                event.getUser(),
                                user,
                                ModLogService.ModAction.UNMUTE,
                                untimeoutReason
                            );
                            event.reply("Successfully removed timeout from " + user.getAsMention()).setEphemeral(true).queue();
                        },
                        failure -> event.reply("Failed to remove timeout from " + user.getAsMention()).setEphemeral(true).queue()
                    );
            },
            error -> event.reply("Error: The user is not in this server.").setEphemeral(true).queue()
        );
    }
}
