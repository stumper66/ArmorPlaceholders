package io.github.stumper66.armorplaceholders;

import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.Map;

public class MiscOptions {
    public MiscOptions(){
        this.attributes = new Hashtable<>();
    }

    public float itemDefaultValue;
    public float finalScale;
    public Float finalScoreCap;
    public float enchantmentDefaultValue;
    public float enchantmentLevelScale;
    public boolean useItemDamageScale;
    public boolean checkMainHand;
    public boolean checkOffHand;
    public boolean checkArmor;
    public boolean checkAttributes;
    public boolean onlyIncludeDefinedItems;
    public boolean applyDamageScaleToEnchantments;
    public final @NotNull Map<Attribute, Float> attributes;
}
