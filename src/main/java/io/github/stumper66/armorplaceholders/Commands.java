package io.github.stumper66.armorplaceholders;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    public Commands(final @NotNull ArmorPlaceholders main){
        this.main = main;
    }

    private final ArmorPlaceholders main;
    private CommandSender sender;
    private String[] args;

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, final @NotNull Command command, final @NotNull String label, final String @NotNull [] args) {
        this.sender = sender;
        this.args = args;
        if (args.length == 0) return false;

        if ("info".equalsIgnoreCase(args[0])){
            showInfo();
        }
        else if ("calculate".equalsIgnoreCase(args[0])){
            doCalculate();
        }
        else if ("reload".equalsIgnoreCase(args[0])){
            if (!hasPermissions("reload")) return true;
            ConfigLoader.loadConfig();
            main.showConfigInfo();
            sender.sendMessage("Reload complete");
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasPermissions(final @Nullable String perm){
        if (sender.hasPermission("armorplaceholders" + (perm == null ? "" : "." + perm)))
            return true;
        else {
            sender.sendMessage("You don't have permissions for this command");
            return false;
        }
    }

    private void doCalculate(){
        if (!hasPermissions("calculate")) return;

        if (args.length < 2 && !(sender instanceof Player)){
            sender.sendMessage("Must specify a player name");
            return;
        }

        Player player;
        if (args.length >= 2){
            player = Bukkit.getPlayer(args[1]);
            if (player == null){
                sender.sendMessage("The selected player either doesn't exist or is offline.");
                return;
            }
        }
        else
            player = (Player) sender;

        final CalculateResult result = main.calculator.calculatePlayerNumber(player, true);
        sender.sendMessage("\n" + result.info);
    }

    private void showInfo(){
        if (!hasPermissions("info")) return;

        final String msg = "&9&lArmorPlaceholders " + main.getDescription().getVersion() + "&r\n" +
                "&l-----------------------------\n" +
                "Plugin created by &9&oStumper66\n" +
                "&7Contributors: &9&oUltimaOath";

        sender.sendMessage(Utils.colorizeStandardCodes(msg));
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args){
        this.sender = sender;
        if (!hasPermissions(null)) return null;

        if (args.length <= 1)
            return List.of("info", "calculate", "reload");
        else if (args.length == 2 && "calculate".equalsIgnoreCase(args[0]) && hasPermissions("calculate")){
            return Bukkit.getOnlinePlayers().stream()
                    .sorted(Comparator.comparing((Player n) -> n.getName()))
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        return List.of("");
    }
}
