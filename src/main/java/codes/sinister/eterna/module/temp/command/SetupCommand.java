package codes.sinister.eterna.module.temp.command;

import codes.sinister.eterna.module.temp.TempChannelConfig;
import codes.sinister.eterna.util.Constant;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetupCommand {
    @Command(
            name = "tempchannel",
            description = "Setup temporary voice channels",
            permissions = {Permission.ADMINISTRATOR}
    )
    public void onSetup(
            SlashCommandInteractionEvent event,
            @Option(description = "The 'Join to Create' voice channel") String joinChannelId,
            @Option(description = "The category for temporary channels") String categoryId
    ) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        VoiceChannel joinChannel = guild.getVoiceChannelById(joinChannelId);
        Category category = guild.getCategoryById(categoryId);

        if (joinChannel == null) {
            event.reply("Invalid voice channel provided. Please select a valid voice channel.").setEphemeral(true).queue();
            return;
        }

        if (category == null) {
            event.reply("Invalid category provided. Please select a valid category.").setEphemeral(true).queue();
            return;
        }

        TempChannelConfig config = TempChannelConfig.forGuild(guild.getId());
        config.setConfig(joinChannelId, categoryId);

        event.replyEmbeds(Constant.embed()
                .setTitle("✅ Temporary Channels Setup")
                .setDescription("Temporary voice channels have been configured successfully!")
                .addField("Join Channel", joinChannel.getAsMention(), true)
                .addField("Category", category.getName(), true)
                .addField("How to Use", "Users can join " + joinChannel.getAsMention() + " to create their own temporary voice channel.", false)
                .build()
        ).queue();
    }

    @Command(
            name = "tempchannel-status",
            description = "Check temporary voice channel configuration",
            permissions = {Permission.ADMINISTRATOR}
    )
    public void onStatus(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        TempChannelConfig config = TempChannelConfig.forGuild(guild.getId());

        if (!config.isConfigured()) {
            event.replyEmbeds(Constant.embed()
                    .setTitle("❌ Not Configured")
                    .setDescription("Temporary voice channels are not configured in this server. Use `/tempchannel` to set them up.")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        VoiceChannel joinChannel = guild.getVoiceChannelById(config.getJoinChannelId());
        Category category = guild.getCategoryById(config.getCategoryId());

        event.replyEmbeds(Constant.embed()
                .setTitle("Temporary Channels Configuration")
                .addField("Join Channel", joinChannel != null ? joinChannel.getAsMention() : "Missing (ID: " + config.getJoinChannelId() + ")", true)
                .addField("Category", category != null ? category.getName() : "Missing (ID: " + config.getCategoryId() + ")", true)
                .build()
        ).setEphemeral(true).queue();
    }

    @Command(
            name = "tempchannel-reset",
            description = "Reset temporary voice channel configuration",
            permissions = {Permission.ADMINISTRATOR}
    )
    public void onReset(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        TempChannelConfig config = TempChannelConfig.forGuild(guild.getId());
        config.setConfig(null, null);

        event.replyEmbeds(Constant.embed()
                .setTitle("✅ Configuration Reset")
                .setDescription("Temporary voice channel configuration has been reset for this server.")
                .build()
        ).setEphemeral(true).queue();
    }
}