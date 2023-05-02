package io.github.stumper66.armorplaceholders;

import io.github.stumper66.armorplaceholders.definitions.EnchantmentInfo;
import io.github.stumper66.armorplaceholders.definitions.ItemInfo;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
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
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
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
        opts.useItemDamageScale = cfg.getBoolean("use-item-damage-scale", true);
        opts.checkMainHand = cfg.getBoolean("check-main-hand", true);
        opts.checkOffHand = cfg.getBoolean("check-offhand-hand", true);
        opts.checkArmor = cfg.getBoolean("check-armor", true);
        opts.checkAttributes = cfg.getBoolean("check-attributes", false);
        opts.onlyIncludeDefinedItems = cfg.getBoolean("only-include-defined-items", true);
        opts.applyDamageScaleToEnchantments = cfg.getBoolean("apply-damage-scale-to-enchantments", true);
        double temp = cfg.getDouble("final-score-cap", Double.MIN_VALUE);
        opts.finalScoreCap = temp > Double.MIN_VALUE ? (float) temp : null;
        opts.attributes.clear();
        opts.attributes.putAll(parseAttributes(cfg));

        final Map<Enchantment, EnchantmentInfo> enchantmentsMap = parseEnchantments(cfg);
        final Map<Material, ItemInfo> itemsMap = parseMaterials(cfg);

        final ArmorPlaceholders main = ArmorPlaceholders.getInstance();
        main.calculator.miscOptions = opts;
        main.calculator.itemsMap = itemsMap;
        main.calculator.enchantmentsMap = enchantmentsMap;
    }

    private static @NotNull Map<Attribute, Float> parseAttributes(final @NotNull YamlConfiguration cfg){
        final Map<Attribute, Float> result = new Hashtable<>();

        final List<Attribute> attribs = List.of(
                Attribute.GENERIC_ATTACK_DAMAGE,
                Attribute.GENERIC_ATTACK_SPEED,
                Attribute.GENERIC_ARMOR,
                Attribute.GENERIC_ARMOR_TOUGHNESS,
                Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                Attribute.GENERIC_LUCK,
                Attribute.GENERIC_MAX_HEALTH,
                Attribute.GENERIC_MOVEMENT_SPEED
                );

        for (final Attribute attrib : attribs){
            final double value = cfg.getDouble("attributes." + attrib.name(), Double.MIN_VALUE);
            if (value == Double.MIN_VALUE) continue;

            result.put(attrib, (float)value);
        }

        return result;
    }

    private static @NotNull Map<Enchantment, EnchantmentInfo> parseEnchantments(final @NotNull YamlConfiguration cfg){
        final Map<Enchantment, EnchantmentInfo> enchantmentsMap = new HashMap<>();
        final ConfigurationSection cs = cfg.getConfigurationSection("enchantments");
        if (cs == null) return enchantmentsMap;

        for (final String enchantmentName : cs.getKeys(false)){
            Object keys = cs.get(enchantmentName);
            final EnchantmentInfo ei = new EnchantmentInfo();
            double levelScale = cs.getDouble("enchantment-level-scale", Double.MIN_VALUE);
            double value;

            if (keys instanceof MemorySection){
                MemorySection ms = (MemorySection) keys;
                levelScale = ms.getDouble("scale", levelScale);
                final Object valueSectionObj = ((MemorySection) keys).get("value");
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
                else{
                    for (final String enchantLevelStr : ms.getKeys(false)){
                        if ("value".equalsIgnoreCase(enchantLevelStr)){
                            value = ms.getDouble(enchantLevelStr);
                        }
                        else if("scale".equalsIgnoreCase(enchantLevelStr)){
                            levelScale = ms.getDouble(enchantLevelStr);
                        }
                        else if (enchantLevelStr.length() < 4){
                            int enchantLevel;
                            try{
                                enchantLevel = Integer.parseInt(enchantLevelStr);
                            }
                            catch (Exception ignored){
                                continue;
                            }
                            double tempD = ms.getDouble(enchantLevelStr, Double.MIN_VALUE);
                            if (tempD > Double.MIN_VALUE)
                                ei.levelAssignments.put(enchantLevel, tempD);
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

            Material material = null;
            try{
                material = Material.getMaterial(materialName);
            }
            catch (Exception ignored){ }
            if (material == null){
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
