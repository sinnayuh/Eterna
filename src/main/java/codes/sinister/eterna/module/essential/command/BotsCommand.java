package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BotsCommand {
    @Command(
            name = "bots",
            description = "List all bots"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        List<Member> bots = guild.getMembers().stream().filter(member -> member.getUser().isBot()).toList();

        event.replyEmbeds(Constant.embed()
                .setTitle("Bots (" + bots.size() + ")")
                .setDescription("\u2022 " + String.join("\n\u2022 ", bots.stream().map(IMentionable::getAsMention).toList()))
                .build()).queue();
    }
}
