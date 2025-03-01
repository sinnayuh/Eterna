package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AvatarCommand {
    @Command(
            name = "avatar",
            description = "View your or another member's avatar"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event, @Option(required = false) @Nullable User user) {
        if (user == null) user = event.getUser();
        event.replyEmbeds(Constant.embed()
                .setTitle(user.getGlobalName() + "'s Avatar", user.getAvatarUrl() + "?size=256")
                .setImage(user.getAvatarUrl() + "?size=256")
                .build()
        ).queue();
    }
}
