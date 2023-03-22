package io.github.stumper66.armorplaceholders.definitions;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentInfo {
    public Enchantment enchantment;
    public Float levelScale;
    public float value;

    public String toString(){
        return "" + value;
    }
}
