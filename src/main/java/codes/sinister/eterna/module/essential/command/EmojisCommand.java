package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EmojisCommand {
    @Command(
            name = "emojis",
            description = "Get a list of emojis"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        List<RichCustomEmoji> emojis = guild.getEmojis();
        event.replyEmbeds(Constant.embed()
                .setTitle("Emojis (" + emojis.size() + ")")
                .setDescription("\u2022 " + String.join("\n\u2022 ", emojis.stream().map(IMentionable::getAsMention).toList()))
                .build()).queue();
    }
}
