package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BoostersCommand {
    @Command(
            name = "boosters",
            description = "Get a list of all server boosters"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        List<Member> members = guild.getBoosters();

        if (members.isEmpty()) {
            event.replyEmbeds(Constant.embed()
                    .setTitle("Boosters (" + members.size() + ")")
                    .setDescription("There are no boosters")
                    .build()).queue();
            return;
        }

        event.replyEmbeds(Constant.embed()
                .setTitle("Boosters (" + members.size() + ")")
                .setDescription("\u2022 " + String.join("\n\u2022 ", members.stream().map(IMentionable::getAsMention).toList()))
                .build()).queue();
    }
}
