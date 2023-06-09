package io.github.stumper66.armorplaceholders;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorPlaceholders extends JavaPlugin {
    public ArmorPlaceholders(){
        instance = this;
    }

    private static ArmorPlaceholders instance;
    Calculator calculator;

    @Override
    public void onEnable() {
        calculator = new Calculator();
        registerCommands();
        ConfigLoader.loadConfig();
        showConfigInfo();
        (new PAPIIntegration()).register();
        Utils.logger.info("ArmorPlaceholders finished startup");
    }

    private void registerCommands(){
        final PluginCommand cmd = getCommand("armorplaceholders");
        if (cmd == null)
            Utils.logger.warning("ArmorPlaceHolders: Command &b/armorplaceholders&7 is unavailable, is it not registered in plugin.yml?");
        else
            cmd.setExecutor(new Commands(this));
    }

    void showConfigInfo(){
        Utils.logger.info(String.format(
                "ArmorPlaceHolders: materials defined: %s, enchantments defined: %s",
                calculator.itemsMap.size(), calculator.enchantmentsMap.size()
        ));
    }

    @Override
    public void onDisable() {

    }

    public static ArmorPlaceholders getInstance(){
        return instance;
    }
}
