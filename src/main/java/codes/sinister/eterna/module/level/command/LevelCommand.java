package codes.sinister.eterna.module.level.command;

import codes.sinister.eterna.module.level.LevelRegistry;
import codes.sinister.eterna.module.level.UserLevelData;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static codes.sinister.eterna.util.Constant.embed;

public final class LevelCommand {

    private static final int BAR_LENGTH = 10;
    private static final String FILLED = "█";
    private static final String EMPTY = "░";

    @Command(
            name = "level",
            description = "View a member's level"
    )
    public void onCommand(@NotNull SlashCommandInteractionEvent event, @Option(required = false) @Nullable User user) {
        if (user == null) user = event.getUser();
        if (user.isBot()) {
            event.reply("Wouldn't fit on the screen.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        UserLevelData levelData = LevelRegistry.getLevelData(user.getId(), guildId);

        int currentXp = levelData.getXp();
        int requiredXp = levelData.getXpRequiredForLevel(levelData.getLevel() + 1);

        String progressBar = createProgressBar(currentXp, requiredXp, BAR_LENGTH);
        double percentage = (double) currentXp / requiredXp * 100;

        event.replyEmbeds(embed()
                .setTitle(user.getName() + "'s Level")
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("Level", String.valueOf(levelData.getLevel()), true)
                .addField("XP", currentXp + "/" + requiredXp, true)
                .addField("Progress", progressBar + " " + String.format("%.1f%%", percentage), false)
                .build()
        ).queue();
    }

    /**
     * Creates a text-based progress bar
     *
     * @param current Current value
     * @param max Maximum value
     * @param length Length of the progress bar
     * @return A string representation of the progress bar
     */
    private static String createProgressBar(int current, int max, int length) {
        float percentage = (float) current / max;
        int filledBars = Math.round(percentage * length);

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append(i < filledBars ? FILLED : EMPTY);
        }

        return builder.toString();
    }
}