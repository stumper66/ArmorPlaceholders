package io.github.stumper66.armorplaceholders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class Utils {
    public final static Logger logger = Bukkit.getLogger();

    public static @NotNull String colorizeStandardCodes(final @NotNull String msg) {
        return Bukkit.getName().equalsIgnoreCase("CraftBukkit") ?
                ChatColor.translateAlternateColorCodes('&', msg) :
                net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static double roundToTwo(final double amount){
        return Math.round(amount * 100.0) / 100.0;
    }

    public static @NotNull String capitalize(@NotNull final String str) {
        final StringBuilder builder = new StringBuilder();
        final String[] words = str.toLowerCase().split(" "); // each word separated from str
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            if (word.isEmpty()) {
                continue;
            }

            builder.append(String.valueOf(word.charAt(0)).toUpperCase()); // capitalize first letter
            if (word.length() > 1) {
                builder.append(word.substring(1)); // append the rest of the word
            }

            // if there is another word to capitalize, then add a space
            if (i < words.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }
}
