package io.github.stumper66.armorplaceholders;

import io.github.stumper66.armorplaceholders.definitions.EnchantmentInfo;
import io.github.stumper66.armorplaceholders.definitions.ItemInfo;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
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
        float score = 0.0f;
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

        String misc = null;

        if (miscOptions.finalScale != 1.0f){
            final float preScore = score;
            score *= miscOptions.finalScale;
            if (showInfo){
                final float difference = (float) Utils.roundToTwo(score - preScore);
                misc = String.format("\n  - Scaled %s&r (%s&r)",
                        formatPlusAndMinus(miscOptions.finalScale), formatPlusAndMinus(difference, "&b"));
            }
        }

        score = (float) Utils.roundToTwo(score);

        if (miscOptions.finalScoreCap != null && score > miscOptions.finalScoreCap){
            score = miscOptions.finalScoreCap;
            if (showInfo) {
                final String temp = " (reached cap)";
                if (misc == null)
                    misc = temp;
                else
                    misc = temp + misc;
            }
        }

        if (showInfo){
            if (itemCount > 0) {
                calculateInfo.append("\n");
                calculateInfo.append(separators);
                calculateInfo.append(String.format("\nTotal result: &9%s&r", score));
                if (misc != null)
                    calculateInfo.append(misc);

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

        if (miscOptions.onlyIncludeDefinedItems && !this.itemsMap.containsKey(item.getType())) return 0.0f;
        final ItemInfo itemInfo = itemsMap.get(item.getType());

        float itemScore = 0.0f;

        this.itemCount++;
        if (itemInfo != null)
            itemScore += itemInfo.value;
        else
            itemScore = miscOptions.itemDefaultValue;

        float noEnchantmentScore = itemScore;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return itemScore;

        double percentDamaged = 0.0;
        if (miscOptions.useItemDamageScale && meta instanceof Damageable){
            final Damageable dam = (Damageable) meta;
            if (dam.hasDamage()) {
                percentDamaged = ((double) dam.getDamage() / (double) item.getType().getMaxDurability() * 100.0);
                percentDamaged = Utils.roundToTwo(percentDamaged);
            }
        }

        if (this.enchantmentsMap == null) this.enchantmentsMap = new HashMap<>();
        StringBuilder sbEnchantments = new StringBuilder();
        float enchantmentScore = 0.0f;
        for (final Enchantment enchantment : meta.getEnchants().keySet()){
            final int enchantmentLevel = meta.getEnchants().get(enchantment);
            float levelScale = miscOptions.enchantmentLevelScale;
            float value;
            boolean hadLevelAssignment = false;
            EnchantmentInfo ei = this.enchantmentsMap.get(enchantment);
            if (ei != null){
                value = ei.value;
                if (ei.levelScale != null) levelScale = ei.levelScale;
                if (ei.levelAssignments.containsKey(enchantmentLevel)) {
                    value = ei.levelAssignments.get(enchantmentLevel).floatValue();
                    hadLevelAssignment = true;
                }
            }
            else
                value = miscOptions.enchantmentDefaultValue;

            float totalValue = hadLevelAssignment ?
                    value : value * ((float) enchantmentLevel * levelScale);

            if (showInfo) {
                final String enchantName = Utils.capitalize(enchantment.key().value().replace("_", " "));
                sbEnchantments.append(String.format("\n  - &7&o%s %s&r (%s&r)",
                        enchantName, enchantmentLevel, formatPlusAndMinus(totalValue, "&b")));
            }
            enchantmentScore += totalValue;
        }

        double damageDiff = 0.0;
        if (percentDamaged > 0.0){
            final float preScore = miscOptions.applyDamageScaleToEnchantments ?
                    itemScore + enchantmentScore : itemScore;
            damageDiff = Utils.roundToTwo(preScore * percentDamaged * 0.01);
            itemScore -= (float) damageDiff;
        }

        itemScore += enchantmentScore;
        if (itemScore < 0.0f) itemScore = 0.0f;

        if (showInfo) {
            calculateInfo.append("\n");
            final String friendlyName = Utils.capitalize(item.getType().toString().replace("_", " "));
            calculateInfo.append(String.format("[%s] %s: &7&o%s&r (%s&r)",
                    itemCount, description, friendlyName, formatPlusAndMinus(noEnchantmentScore)));
            if (sbEnchantments.length() > 0)
                calculateInfo.append(sbEnchantments);

            if (percentDamaged > 0.0) {
                calculateInfo.append("\n");
                calculateInfo.append(String.format("  - &7&oDamage&r (&c-%s&r)", damageDiff));
            }
        }

        return itemScore;
    }

    @Contract(pure = true)
    private @NotNull String formatPlusAndMinus(final Float value){
        return formatPlusAndMinus(value, "&9");
    }

    @Contract(pure = true)
    private @NotNull String formatPlusAndMinus(final Float value, final @NotNull String colorCode){
        // &c = red
        // &9 = blue
        if (value > 0.0f){
            return colorCode + "+" + value;
        }
        else if (value == 0.0f){
            return colorCode + value;
        }
        else{
            return "&c" + value;
        }
    }
}
