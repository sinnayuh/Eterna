package codes.sinister.eterna.module.essential.command;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

public class InfoCommand {
    @Command(
            name = "info",
            description = "Get information about a user"
    )
    public void onInfo(SlashCommandInteractionEvent event,
                       @Option(description = "The user to get info about", required = false) User user) {

        User targetUser = user != null ? user : event.getUser();
        Member member = event.getGuild().getMember(targetUser);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("User Information")
                .setColor(member != null ? member.getColor() : Color.BLUE)
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .addField("Username", targetUser.getName(), true)
                .addField("ID", targetUser.getId(), true)
                .addField("Account Created", "<t:" + targetUser.getTimeCreated().toEpochSecond() + ":R>", true);

        if (member != null) {
            embed.addField("Joined Server",
                    member.getTimeJoined() != null ?
                            "<t:" + member.getTimeJoined().toEpochSecond() + ":R>" :
                            "Unknown", true);

            if (!member.getRoles().isEmpty()) {
                embed.addField("Roles",
                        member.getRoles().stream()
                                .map(role -> role.getAsMention())
                                .limit(10)
                                .reduce((a, b) -> a + " " + b)
                                .orElse("None"),
                        false);
            }
        }

        embed.setFooter("Requested by " + event.getUser().getName())
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }
}
