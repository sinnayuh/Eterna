package codes.sinister.eterna.module.essential.command;

import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BannerCommand {
    @Command(
            name = "banner",
            description = "View your or another member's banner"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event, @Option(required = false) @Nullable User user) {
        if (user == null) user = event.getUser();

        User.Profile profile = user.retrieveProfile().complete();

        if (profile.getBannerUrl() == null) {
            event.reply("This user does not have a banner.").setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(Constant.embed()
                .setTitle(user.getGlobalName() + "'s Banner", profile.getBannerUrl() + "?size=4096")
                .setImage(profile.getBannerUrl() + "?size=4096")
                .build()
        ).queue();
    }
}
