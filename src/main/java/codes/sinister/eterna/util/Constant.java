package codes.sinister.eterna.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public final class Constant {
    private Constant() {}

    public static @NotNull EmbedBuilder embed() {
        return new EmbedBuilder().setColor(Color.decode("#fc9a9a"));
    }

    public static @NotNull String getUptime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long uptimeSeconds = uptime / 1000;
        long days = uptimeSeconds / (24 * 60 * 60);
        long hours = (uptimeSeconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = (uptimeSeconds % (60 * 60)) / 60;
        long seconds = uptimeSeconds % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
