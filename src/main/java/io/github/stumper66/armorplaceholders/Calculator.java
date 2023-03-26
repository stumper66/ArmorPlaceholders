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
        final String separators = "&l------------------------------&r";
        if (showInfo) this.calculateInfo.append(separators);

        final EntityEquipment ee = player.getEquipment();
        if (miscOptions.checkArmor) {
            score += checkItem(ee.getHelmet(), "Helmet");
            score += checkItem(ee.getChestplate(), "Chestplate");
            score += checkItem(ee.getLeggings(), "Leggings");
            score += checkItem(ee.getBoots(), "Boots");
        }

        if (miscOptions.checkMainHand) {
            score += checkItem(ee.getItemInMainHand(), "Main-Hand");
        }
        if (miscOptions.checkOffHand) {
            score += checkItem(ee.getItemInOffHand(), "Off-Hand");
        }

        StringBuilder sbMisc = new StringBuilder();
        if (miscOptions.finalScoreCap != null && score > miscOptions.finalScoreCap){
            score = miscOptions.finalScoreCap;
            if (showInfo) {
                sbMisc.append(String.format(", hit-cap: &9%s&r",
                        miscOptions.finalScoreCap));
            }
        }

        if (miscOptions.finalScale != 1.0f){
            score *= miscOptions.finalScale;
            if (showInfo){
                sbMisc.append(String.format(", scl: &9%s&r",
                        miscOptions.finalScale));
            }
        }

        if (showInfo){
            if (itemCount > 0) {
                if (calculateInfo.length() > 0)
                    calculateInfo.append("\n");
                calculateInfo.append(separators);
                calculateInfo.append(String.format("\nTotal result: &9%s&r", score));
                if (sbMisc.length() > 0)
                    calculateInfo.append(sbMisc);

                final String finalMsg = Utils.colorizeStandardCodes(calculateInfo.toString());
                calculateInfo.setLength(0);
                calculateInfo.append(finalMsg);
            }
            else{
                calculateInfo.setLength(0);
                calculateInfo.append("No items checked");
            }
        }

        return new CalculateResult(score, calculateInfo.toString());
    }

    private float checkItem(final @Nullable ItemStack item, final String description){
        if (item == null || item.getType() == Material.AIR) return 0f;
        this.itemCount++;
        final ItemInfo itemInfo = itemsMap.get(item.getType());

        float itemScore = miscOptions.itemDefaultValue;
        if (itemInfo == null) {
            if (calculateInfo.length() > 0)
                calculateInfo.append("\n");
            calculateInfo.append(String.format("[%s] %s: item: %s, score: %s, no defined value",
                    itemCount, description, item.getType(), itemScore));
            return itemScore;
        }
        itemScore += itemInfo.value;
        final float noEnchantmentScore = itemScore;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return itemScore;

        if (this.enchantmentsMap == null) this.enchantmentsMap = new HashMap<>();
        StringBuilder sbEnchantments = new StringBuilder();
        float enchantmentScore = 0.0f;
        boolean hasEnchantments = false;
        for (final Enchantment enchantment : meta.getEnchants().keySet()){
            hasEnchantments = true;
            final int enchantmentLevel = meta.getEnchants().get(enchantment);
            float levelScale = miscOptions.enchantmentLevelScale;
            float value;
            EnchantmentInfo ei = this.enchantmentsMap.get(enchantment);
            if (ei != null){
                value = ei.value;
                if (ei.levelScale != null) levelScale = ei.levelScale;
                if (ei.levelAssignments.containsKey(enchantmentLevel))
                    value = ei.levelAssignments.get(enchantmentLevel).floatValue();

            }
            else
                value = miscOptions.enchantmentDefaultValue;

            float totalValue = value * (float) enchantmentLevel * levelScale;
            if (showInfo) {
                final String enchantName = Utils.capitalize(enchantment.key().value().replace("_", " "));
                sbEnchantments.append(String.format("\n  - &7&o%s_%s&r (&9%s&r)",
                        enchantName, enchantmentLevel, totalValue));
            }
            enchantmentScore += totalValue;
        }

        itemScore += enchantmentScore;

        if (showInfo) {
            if (calculateInfo.length() > 0)
                calculateInfo.append("\n");
            String extra = hasEnchantments ?
                    " + &3" + itemScore + "&r" :
                    "";
            calculateInfo.append(String.format("[%s] %s: &7&o%s&r, (&9%s&r%s)",
                    itemCount, description, item.getType(), noEnchantmentScore, extra));
            if (sbEnchantments.length() > 0)
                calculateInfo.append(sbEnchantments);
        }

        return itemScore;
    }
}
