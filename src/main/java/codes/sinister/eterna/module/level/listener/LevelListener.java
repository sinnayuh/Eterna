package codes.sinister.eterna.module.level.listener;

import codes.sinister.eterna.module.level.LevelRegistry;
import codes.sinister.eterna.module.level.UserLevelData;
import codes.sinister.eterna.util.Constant;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LevelListener extends ListenerAdapter {
    private static final Random RANDOM = new Random();
    private static final String LAST_MESSAGE_KEY = "last_message";

    private static final int MIN_XP = 15;
    private static final int MAX_XP = 25;
    private static final long COOLDOWN_MS = TimeUnit.MINUTES.toMillis(1);

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }

        User user = event.getAuthor();
        String userId = user.getId();
        String guildId = event.getGuild().getId();

        UserLevelData levelData = LevelRegistry.getLevelData(userId, guildId);

        long lastMessageTime = levelData.getProfile().getGuildData(guildId, LAST_MESSAGE_KEY, 0L);
        long now = System.currentTimeMillis();

        if (lastMessageTime > 0 && now - lastMessageTime < COOLDOWN_MS) {
            return;
        }

        levelData.getProfile().updateGuildData(guildId, LAST_MESSAGE_KEY, now);

        int xpAmount = RANDOM.nextInt(MAX_XP - MIN_XP + 1) + MIN_XP;

        boolean leveledUp = levelData.addXp(xpAmount);

        if (leveledUp && event.isFromType(ChannelType.TEXT)) {
            int newLevel = levelData.getLevel();
            event.getChannel().sendMessageEmbeds(Constant.embed()
                    .setTitle("Level Up!")
                    .setDescription(user.getAsMention() + " has reached level " + newLevel + "!")
                    .build()
            ).queue();
        }
    }
}