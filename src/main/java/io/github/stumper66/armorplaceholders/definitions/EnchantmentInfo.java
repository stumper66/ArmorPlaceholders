package io.github.stumper66.armorplaceholders.definitions;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentInfo {
    public EnchantmentInfo(){
        this.levelAssignments = new HashMap<>();
    }

    public Enchantment enchantment;
    public Float levelScale;
    public float value;
    public final Map<Integer, Double> levelAssignments;

    public String toString(){
        return String.valueOf(value);
    }
}
