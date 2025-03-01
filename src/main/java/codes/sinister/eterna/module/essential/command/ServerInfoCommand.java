package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;

public final class ServerInfoCommand {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");

    @Command(
            name = "server",
            description = "View information about the server"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(Constant.embed()
                .setAuthor(guild.getName(), null, guild.getIconUrl())
                .setThumbnail(guild.getIconUrl())
                .addField("Owner", guild.getOwner() == null ? "how" : guild.getOwner().getUser().getAsMention(), true)
                .addField("Verification Level", guild.getVerificationLevel().name(), true)
                .addField("Content Filter", guild.getExplicitContentLevel().getDescription(), true)
                .addField("Category Channels", String.valueOf(guild.getCategories().size()), true)
                .addField("Text Channels", String.valueOf(guild.getTextChannels().size()), true)
                .addField("Voice Channels", String.valueOf(guild.getVoiceChannels().size()), true)
                .addField("Members", String.valueOf(guild.getMembers().size()), true)
                .addField("Roles", String.valueOf(guild.getRoles().size()), true)
                .addField("Boosters", String.valueOf(guild.getBoosters().size()), true)
                .addField("Stickers", String.valueOf(guild.getStickers().size()), true)
                .addField("Emojis", String.valueOf(guild.getEmojis().size()), true)
                .addField("Punishments", "0", true)
                .setFooter("ID: " + guild.getId() + " | Server created " + guild.getTimeCreated().format(dtf))
                .build()
        ).queue();
    }
}
