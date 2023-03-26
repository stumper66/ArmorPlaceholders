package io.github.stumper66.armorplaceholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIIntegration extends PlaceholderExpansion {
    public PAPIIntegration(){
        this.main = ArmorPlaceholders.getInstance();
    }

    private final ArmorPlaceholders main;
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return main.getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return main.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(final Player player, final @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if ("calculate".equalsIgnoreCase(identifier) || "calc".equalsIgnoreCase(identifier)) {
            return main.calculator.calculatePlayerNumber(player, false).result + "";
        }

        return null;
    }
}
