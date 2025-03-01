package codes.sinister.eterna.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class Constant {
    private Constant() {}

    public static @NotNull EmbedBuilder embed() {
        return new EmbedBuilder().setColor(Color.decode("#fc9a9a"));
    }
}
