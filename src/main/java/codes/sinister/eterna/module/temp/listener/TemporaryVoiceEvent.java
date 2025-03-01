package codes.sinister.eterna.module.temp;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TemporaryVoiceEvent extends ListenerAdapter {
    private final Map<String, Map<String, String>> userChannelMap = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        var member = event.getMember();
        var guild = event.getGuild();
        var joinedChannel = event.getChannelJoined();
        var guildId = guild.getId();

        if (joinedChannel != null) {
            // Get configuration for this guild
            TempChannelConfig config = TempChannelConfig.forGuild(guildId);

            // If config doesn't exist or channel doesn't match, ignore
            if (!config.isConfigured() || !joinedChannel.getId().equals(config.getJoinChannelId())) {
                return;
            }

            String memberId = member.getId();
            Map<String, String> guildUsers = userChannelMap.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());

            if (guildUsers.containsKey(memberId)) {
                guild.kickVoiceMember(member).queue();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("You already have a voice channel and cannot create another one.").queue();
                });
                return;
            }

            ChannelAction<VoiceChannel> action = guild.createVoiceChannel(member.getEffectiveName() + "'s channel",
                    guild.getCategoryById(config.getCategoryId()));
            action.addPermissionOverride(member, EnumSet.of(Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS), null)
                    .queue(newChannel -> {
                        guildUsers.put(memberId, newChannel.getId());
                        if (member.getVoiceState().inAudioChannel()) {
                            guild.moveVoiceMember(member, newChannel).queue();
                        }
                    });
        }

        handleChannelLeft(event);
    }

    private void handleChannelLeft(GuildVoiceUpdateEvent event) {
        AudioChannelUnion leftChannelUnion = event.getChannelLeft();

        if (leftChannelUnion == null || !(leftChannelUnion instanceof VoiceChannel)) {
            return;
        }

        VoiceChannel leftChannel = (VoiceChannel) leftChannelUnion;
        String guildId = event.getGuild().getId();
        String channelId = leftChannel.getId();

        // Get configuration for this guild
        TempChannelConfig config = TempChannelConfig.forGuild(guildId);
        if (!config.isConfigured()) {
            return;
        }

        // If this is the "join to create" channel, ignore it
        if (channelId.equals(config.getJoinChannelId())) {
            return;
        }

        // If channel is empty and in the correct category
        if (leftChannel.getMembers().isEmpty() && leftChannel.getParentCategoryId() != null
                && leftChannel.getParentCategoryId().equals(config.getCategoryId())) {

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                VoiceChannel refreshedChannel = event.getGuild().getVoiceChannelById(leftChannel.getId());
                if (refreshedChannel != null && refreshedChannel.getMembers().isEmpty()) {
                    refreshedChannel.delete().queue();
                    scheduledTasks.remove(channelId);

                    // Remove user-channel mapping
                    Map<String, String> guildUsers = userChannelMap.get(guildId);
                    if (guildUsers != null) {
                        guildUsers.values().removeIf(value -> value.equals(channelId));
                    }
                }
            }, 3, TimeUnit.MINUTES);

            scheduledTasks.put(channelId, future);
        } else {
            ScheduledFuture<?> task = scheduledTasks.remove(channelId);
            if (task != null) {
                task.cancel(false);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate in the specified time.");
                List<Runnable> droppedTasks = scheduler.shutdownNow();
                System.err.println("Scheduler was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
            }
        } catch (InterruptedException e) {
            System.err.println("Current thread was interrupted while waiting for executor to terminate.");
            Thread.currentThread().interrupt();
        }
    }
}