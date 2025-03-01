package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;

public final class RoleInfoCommand {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");

    @Command(
            name = "roleinfo",
            description = "View information about a role"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event, @NotNull Role role) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = Constant.embed()
                .addField("ID", role.getId(), true)
                .addField("Name", role.getName(), true)
                .addField("Color", role.getColor() == null ? "Default" : String.format("#%02X%02X%02X", role.getColor().getRed(), role.getColor().getGreen(), role.getColor().getBlue()), true)
                .addField("Mention", "`<@&" + role.getId() + ">`", true)
                .addField("Hoisted", String.valueOf(role.isHoisted()), true)
                .addField("Position", String.valueOf(role.getPosition()), true)
                .addField("Mentionable", String.valueOf(role.isMentionable()), true)
                .addField("Managed", String.valueOf(role.isManaged()), true)
                .addBlankField(true)
                .setFooter("Role created on " + role.getTimeCreated().format(dtf));

        if (role.getIcon() != null) embed.setThumbnail(role.getIcon().getIconUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}
