package io.github.stumper66.armorplaceholders;

import io.github.stumper66.armorplaceholders.definitions.EnchantmentInfo;
import io.github.stumper66.armorplaceholders.definitions.ItemInfo;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Calculator {
    public Calculator(){
        this.calculateInfo = new StringBuilder();
    }

    Map<Material, ItemInfo> itemsMap;
    Map<Enchantment, EnchantmentInfo> enchantmentsMap;
    private boolean showInfo;
    private int itemCount;
    private final StringBuilder calculateInfo;
    MiscOptions miscOptions;

    public CalculateResult calculatePlayerNumber(final @NotNull Player player, final boolean showInfo){
        this.showInfo = showInfo;
        float score = 0f;
        this.itemCount = 0;
        this.calculateInfo.setLength(0);

        final EntityEquipment ee = player.getEquipment();
        score += checkItem(ee.getHelmet(), "helmet");
        score += checkItem(ee.getChestplate(), "chestplate");
        score += checkItem(ee.getLeggings(), "leggings");
        score += checkItem(ee.getBoots(), "boots");

        score += checkItem(ee.getItemInMainHand(), "main item");
        score += checkItem(ee.getItemInOffHand(), "offhand item");

        if (showInfo){
            if (calculateInfo.length() > 0)
                calculateInfo.append("\n");
            calculateInfo.append(String.format("items checked: %s, result: %s", itemCount, score));
        }

        return new CalculateResult(score, calculateInfo.toString());
    }

    private float checkItem(final @Nullable ItemStack item, final String description){
        Utils.logger.info(description + ", " + item);

        if (item == null || item.getType() == Material.AIR) return 0f;
        this.itemCount++;
        final ItemInfo itemInfo = itemsMap.get(item.getType());

        float itemScore = miscOptions.itemDefaultValue;
        if (itemInfo == null) {
            if (calculateInfo.length() > 0)
                calculateInfo.append("\n");
            calculateInfo.append(String.format("%s: item: %s, score: %s, no defined value",
                    description, item.getType(), itemScore));
            return itemScore;
        }
        itemScore += itemInfo.value;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return itemScore;

        if (this.enchantmentsMap == null) this.enchantmentsMap = new HashMap<>();
        float enchantmentScore = 0.0f;
        for (final Enchantment enchantment : meta.getEnchants().keySet()){
            final int level = meta.getEnchants().get(enchantment);
            float levelScale = miscOptions.enchantmentLevelScale;
            float value;
            EnchantmentInfo ei = this.enchantmentsMap.get(enchantment);
            if (ei != null){
                value = ei.value;
                if (ei.levelScale != null) levelScale = ei.levelScale;
            }
            else
                value = miscOptions.enchantmentDefaultValue;

            if (showInfo) {
                if (calculateInfo.length() > 0)
                    calculateInfo.append("\n");

                calculateInfo.append(String.format("   %s, value: %s, lvl: %s, lvl-scl: %s",
                        enchantment.key(), value, level, levelScale));
            }
            enchantmentScore += value + ((float) level * levelScale);
        }

        itemScore += enchantmentScore;

        if (showInfo) {
            if (calculateInfo.length() > 0)
                calculateInfo.append("\n");
            calculateInfo.append(String.format("%s: %s, score: %s",
                    description, item.getType(), itemScore));
        }

        if (miscOptions.finalScale != 1.0f){
            itemScore *= miscOptions.finalScale;;
            if (showInfo){
                calculateInfo.append(", scl: ");
                calculateInfo.append(miscOptions.finalScale);
            }
        }

        if (miscOptions.finalScoreCap != null && itemScore > miscOptions.finalScoreCap){
            itemScore = miscOptions.finalScoreCap;
            if (showInfo) {
                calculateInfo.append(", hit-cap: ");
                calculateInfo.append(miscOptions.finalScoreCap);
            }
        }

        return itemScore;
    }
}
