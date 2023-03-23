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
}
