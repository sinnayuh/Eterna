package codes.sinister.eterna.module.moderation.command;

import codes.sinister.eterna.module.moderation.ModLogService;
import codes.sinister.eterna.module.moderation.Warning;
import codes.sinister.eterna.module.moderation.WarningService;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class WarnCommand {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    @Command(name = "warn", description = "Warn a user", permissions = Permission.MODERATE_MEMBERS)
    public void onWarn(@NotNull SlashCommandInteractionEvent event,
                      @Option(description = "The user to warn") @NotNull User user,
                      @Option(description = "Reason for the warning") @NotNull String reason) {

        if (user.isBot()) {
            event.reply("You can't warn a bot!").setEphemeral(true).queue();
            return;
        }

        if (user.getId().equals(event.getUser().getId())) {
            event.reply("You can't warn yourself!").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        String moderatorId = event.getUser().getId();

        Warning warning = new Warning(user.getId(), guildId, moderatorId, reason);
        int warnCount = WarningService.addWarning(warning);

        ModLogService.logAction(
            event.getGuild(),
            event.getUser(),
            user,
            ModLogService.ModAction.WARN,
            reason
        );

        event.reply("Successfully warned " + user.getAsMention() + ". They now have " + warnCount + " warning(s).").setEphemeral(true).queue();

        user.openPrivateChannel().queue(privateChannel ->
            privateChannel.sendMessageEmbeds(
                new EmbedBuilder()
                    .setTitle("Warning Received")
                    .setDescription("You have received a warning in " + event.getGuild().getName())
                    .setColor(Color.YELLOW)
                    .addField("Reason", reason, false)
                    .addField("Moderator", event.getUser().getAsMention(), false)
                    .addField("Warning Count", String.valueOf(warnCount), false)
                    .setFooter(event.getGuild().getName(), event.getGuild().getIconUrl())
                    .setTimestamp(warning.getTimestamp())
                    .build()
            ).queue(null, error -> {/* Ignore if user has DMs disabled */})
        );
    }

    @Command(name = "warnings", description = "View warnings for a user", permissions = Permission.MODERATE_MEMBERS)
    public void onWarnings(@NotNull SlashCommandInteractionEvent event,
                          @Option(description = "The user to check warnings for") @NotNull User user) {

        if (user.isBot()) {
            event.reply("Bots cannot have warnings.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        List<Warning> warnings = WarningService.getWarnings(user.getId(), guildId);

        if (warnings.isEmpty()) {
            event.reply(user.getAsMention() + " has no warnings.").setEphemeral(true).queue();
            return;
        }

        StringBuilder description = new StringBuilder();
        description.append(user.getAsMention()).append(" has ").append(warnings.size()).append(" warning(s):\n\n");

        for (int i = 0; i < warnings.size(); i++) {
            Warning warning = warnings.get(i);
            description.append("**Warning #").append(i + 1).append("** (ID: `").append(warning.getId()).append("`)\n");
            description.append("**Reason:** ").append(warning.getReason()).append("\n");
            description.append("**Moderator:** <@").append(warning.getModeratorId()).append(">\n");
            description.append("**Date:** ").append(warning.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(DATE_FORMATTER)).append("\n\n");
        }

        event.replyEmbeds(
            new EmbedBuilder()
                .setTitle("Warnings for " + user.getAsMention())
                .setDescription(description.toString())
                .setColor(Color.YELLOW)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Use /clearwarning <user> <warning_id> to remove a specific warning")
                .build()
        ).setEphemeral(true).queue();
    }

    @Command(name = "clearwarning", description = "Remove a specific warning from a user", permissions = Permission.MODERATE_MEMBERS)
    public void onClearWarning(@NotNull SlashCommandInteractionEvent event,
                              @Option(description = "The user to remove a warning from") @NotNull User user,
                              @Option(description = "ID of the warning to remove") @NotNull String warningId) {

        if (user.isBot()) {
            event.reply("Bots cannot have warnings.").setEphemeral(true).queue();
            return;
        }

        boolean removed = WarningService.removeWarning(user.getId(), warningId);

        if (removed) {
            event.reply("Successfully removed warning `" + warningId + "` from " + user.getAsMention()).setEphemeral(true).queue();
        } else {
            event.reply("Could not find warning with ID `" + warningId + "` for " + user.getAsMention()).setEphemeral(true).queue();
        }
    }

    @Command(name = "clearwarnings", description = "Clear all warnings for a user", permissions = Permission.MODERATE_MEMBERS)
    public void onClearWarnings(@NotNull SlashCommandInteractionEvent event,
                               @Option(description = "The user to clear warnings for") @NotNull User user) {

        if (user.isBot()) {
            event.reply("Bots cannot have warnings.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        int count = WarningService.clearWarnings(user.getId(), guildId);

        if (count > 0) {
            event.reply("Successfully cleared " + count + " warning(s) for " + user.getAsMention()).setEphemeral(true).queue();
        } else {
            event.reply(user.getAsMention() + " has no warnings to clear.").setEphemeral(true).queue();
        }
    }
}
