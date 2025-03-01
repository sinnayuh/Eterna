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
                      @Option(description = "The user you wish to kick") @NotNull User user,
                      @Option(description = "Reason for the kick if applicable", required = false) String reason) {

        if (user.isBot()) {
            event.reply("You can't kick a bot!").setEphemeral(true).queue();
            return;
        }
        
        event.getGuild().retrieveMember(user).queue(
            member -> {
                if (!event.getGuild().getSelfMember().canInteract(member)) {
                    event.reply("I can't kick this user due to role hierarchy!").setEphemeral(true).queue();
                    return;
                }

                if (!event.getMember().canInteract(member)) {
                    event.reply("You can't kick someone with a higher role than you!").setEphemeral(true).queue();
                    return;
                }

                String kickReason = reason == null ? "No reason provided" : reason;
                
                event.getGuild().kick(member)
                    .reason(kickReason)
                    .queue(
                        success -> {
                            ModLogService.logAction(
                                event.getGuild(),
                                event.getUser(),
                                user,
                                ModLogService.ModAction.KICK,
                                kickReason
                            );
                            event.reply("Successfully kicked " + user.getAsMention()).setEphemeral(true).queue();
                        },
                        failure -> event.reply("Failed to kick " + user.getAsMention()).setEphemeral(true).queue()
                    );
            },
            error -> event.reply("Error: The user is not in this server.").setEphemeral(true).queue()
        );
    }
}
