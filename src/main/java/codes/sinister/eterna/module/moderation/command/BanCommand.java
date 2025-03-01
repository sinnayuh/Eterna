package codes.sinister.eterna.module.moderation.command;

import org.jetbrains.annotations.NotNull;

import codes.sinister.eterna.module.moderation.ModLogService;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.time.Duration;
import java.util.Collections;

public final class BanCommand {

    @Command(name = "ban", description = "Ban a user from the server", permissions = Permission.BAN_MEMBERS)
    public void onBan(@NotNull SlashCommandInteractionEvent event,
            @Option(description = "The user you wish to ban") @NotNull User user,
            @Option(description = "Reason for the ban if applicable", required = false) String reason) {

        if (user.isBot()) {
            event.reply("You can't ban a bot!").setEphemeral(true).queue();
            return;
        }

        event.getGuild().ban(Collections.singleton(user), Duration.ZERO)
                .reason(reason == null ? "No reason provided" : reason)
                .queue(
                        success -> {
                            ModLogService.logAction(
                                event.getGuild(), 
                                event.getUser(), 
                                user, 
                                ModLogService.ModAction.BAN, 
                                reason
                            );
                            event.reply("Successfully banned " + user.getAsMention()).setEphemeral(true).queue();
                        },
                        failure -> event.reply("Failed to ban " + user.getAsMention()).setEphemeral(true).queue()
                );
    }

    @Command(name = "unban", description = "Unban a user from the server", permissions = Permission.BAN_MEMBERS)
    public void onUnBan(@NotNull SlashCommandInteractionEvent event,
            @Option(description = "The user you wish to unban") @NotNull User user,
            @Option(description = "Reason for the unban if applicable", required = false) String reason) {

        if (user.isBot()) {
            event.reply("Why is bro banned anyway?!").setEphemeral(true).queue();
            return;
        }

        event.getGuild().unban(user)
                .reason(reason == null ? "No reason provided" : reason)
                .queue(
                        success -> {
                            ModLogService.logAction(
                                event.getGuild(),
                                event.getUser(),
                                user,
                                ModLogService.ModAction.UNBAN,
                                reason
                            );
                            event.reply("Successfully unbanned " + user.getAsMention()).setEphemeral(true).queue();
                        },
                        failure -> event.reply("Failed to unban " + user.getAsMention()).setEphemeral(true).queue()
                );
    }
}
