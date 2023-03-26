package io.github.stumper66.armorplaceholders;

import io.github.stumper66.armorplaceholders.definitions.EnchantmentInfo;
import io.github.stumper66.armorplaceholders.definitions.ItemInfo;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigLoader {
    static void loadConfig(){
        final ArmorPlaceholders main = ArmorPlaceholders.getInstance();
        final File file = new File(main.getDataFolder(), "config.yml");

        if (!file.exists())
            main.saveResource(file.getName(), false);

        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.options().copyDefaults(true);
        parseConfig(cfg);
    }

    private static void parseConfig(final @NotNull YamlConfiguration cfg){
        final MiscOptions opts = new MiscOptions();
        opts.itemDefaultValue = (float) cfg.getDouble("item-default-value", 1.0);
        opts.finalScale = (float) cfg.getDouble("final-scale", 1.0);
        opts.enchantmentDefaultValue = (float) cfg.getDouble("enchantment-default-value", 1.0);
        opts.enchantmentLevelScale = (float) cfg.getDouble("enchantment-level-scale", 1.0);
        opts.checkMainHand = cfg.getBoolean("check-main-hand", true);
        opts.checkOffHand = cfg.getBoolean("check-offhand-hand", true);
        opts.checkArmor = cfg.getBoolean("check-armor", true);
        opts.onlyIncludeDefinedItems = cfg.getBoolean("only-include-defined-items", true);
        double temp = cfg.getDouble("final-score-cap", Double.MIN_VALUE);
        opts.finalScoreCap = temp > Double.MIN_VALUE ? (float) temp : null;

        final Map<Enchantment, EnchantmentInfo> enchantmentsMap = parseEnchantments(cfg);
        final Map<Material, ItemInfo> itemsMap = parseMaterials(cfg);

        final ArmorPlaceholders main = ArmorPlaceholders.getInstance();
        main.calculator.miscOptions = opts;
        main.calculator.itemsMap = itemsMap;
        main.calculator.enchantmentsMap = enchantmentsMap;
    }

    private static @NotNull Map<Enchantment, EnchantmentInfo> parseEnchantments(final @NotNull YamlConfiguration cfg){
        final Map<Enchantment, EnchantmentInfo> enchantmentsMap = new HashMap<>();
        final ConfigurationSection cs = cfg.getConfigurationSection("enchantments");
        if (cs == null) return enchantmentsMap;

        for (final String enchantmentName : cs.getKeys(false)){
            Object test = cs.get(enchantmentName);
            final EnchantmentInfo ei = new EnchantmentInfo();
            double levelScale = cs.getDouble("enchantment-level-scale", Double.MIN_VALUE);
            double value;

            if (test instanceof MemorySection){
                MemorySection ms = (MemorySection) test;
                levelScale = ms.getDouble("scale", levelScale);
                Object valueSectionObj = ((MemorySection) test).get("value");
                value = ms.getDouble("value", Double.MIN_VALUE);

                if (valueSectionObj instanceof MemorySection){
                    final MemorySection valueSection = (MemorySection) valueSectionObj;
                    for (final String key : valueSection.getKeys(false)){
                        int keyInt;
                        float assignment;
                        try{
                            keyInt = Integer.parseInt(key);
                        }
                        catch (Exception ignored){
                            Utils.logger.info(String.format("Invalid level %s for %s", key, enchantmentName));
                            continue;
                        }
                        try{
                            double tempD = valueSection.getDouble(key, Double.MIN_VALUE);
                            if (tempD == Double.MIN_VALUE){
                                Utils.logger.info(String.format("No level assignment for %s:%s", enchantmentName, key));
                                continue;
                            }
                            ei.levelAssignments.put(keyInt, tempD);
                        }
                        catch (Exception ignored){
                            Utils.logger.info(String.format("Invalid level assignment for %s:%s", enchantmentName, key));
                        }
                    }
                }
            }
            else {
                value = cs.getDouble(enchantmentName, Double.MIN_VALUE);
                if (value == Double.MIN_VALUE) continue;
            }

            ei.enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
            if (ei.enchantment == null){
                Utils.logger.warning("Invalid enchantment: " + enchantmentName);
                continue;
            }

            ei.value = (float) value;

            if (levelScale > Double.MIN_VALUE)
                ei.levelScale = (float) levelScale;
            enchantmentsMap.put(ei.enchantment, ei);
        }

        return enchantmentsMap;
    }

    private static @NotNull Map<Material, ItemInfo> parseMaterials(final @Nullable ConfigurationSection cfg){
        final Map<Material, ItemInfo> itemsMap = new HashMap<>();
        if (cfg == null) return itemsMap;
        final ConfigurationSection cs = cfg.getConfigurationSection("materials");
        if (cs == null) return itemsMap;

        for (final String materialName : cs.getKeys(false)){
            double value = cs.getDouble(materialName, Double.MIN_VALUE);
            if (value == Double.MIN_VALUE) continue;

            Material material;
            try{
                material = Material.getMaterial(materialName);
            }
            catch (Exception e){
                Utils.logger.warning("Invalid material: " + materialName);
                continue;
            }

            ItemInfo itemInfo = new ItemInfo();
            itemInfo.material = material;
            itemInfo.value = (float) value;

            itemsMap.put(material, itemInfo);
        }

        return itemsMap;
    }

    @SuppressWarnings("unchecked")
    private @Nullable static ConfigurationSection objToCS(final @Nullable Object object){
        if (object == null) return null;

        if (object instanceof ConfigurationSection) {
            return (ConfigurationSection) object;
        } else if (object instanceof Map) {
            final MemoryConfiguration result = new MemoryConfiguration();

            result.addDefaults((Map<String, Object>) object);
            return result.getDefaultSection();
        }
        else if (object instanceof ArrayList){
            final MemoryConfiguration result = new MemoryConfiguration();
            final ArrayList<Object> lst = (ArrayList<Object>) object;
            final Map<String, Object> map = new HashMap<>();

            for (final Object test : lst){
                LinkedHashMap<Object, Object> contents = (LinkedHashMap<Object, Object>) test;
                for (final Object name : contents.keySet()){
                    Object value = contents.get(name);
                    map.put(name.toString(), value);
                }
            }

            result.addDefaults(map);
            return result.getDefaultSection();
        }
        else {
            Utils.logger.warning("couldn't parse Config of type: " + object.getClass().getSimpleName() + ", value: " + object);
            return null;
        }
    }
}
