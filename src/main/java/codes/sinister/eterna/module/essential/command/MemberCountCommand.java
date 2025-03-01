package codes.sinister.eterna.module.essential.command;

import gg.flyte.neptune.annotation.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import codes.sinister.eterna.util.Constant;

public final class MemberCountCommand {
    @Command(
            name = "membercount",
            description = "View the member count of the server"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("No guild wtf bro").setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(Constant.embed()
                .setDescription("**Members**" + "\n" + guild.getMembers().size())
                .build()
        ).queue();
    }
}
