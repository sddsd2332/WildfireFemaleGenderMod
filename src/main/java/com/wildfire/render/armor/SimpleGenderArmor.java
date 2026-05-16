package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

/**
 * 高版本默认护甲配置在 1.12.2 的等价实现。
 */
public class SimpleGenderArmor implements IGenderArmor {
    public static final SimpleGenderArmor FALLBACK = new SimpleGenderArmor(0.5F);
    public static final SimpleGenderArmor LEATHER = new SimpleGenderArmor(0.3F, 0.5F);
    public static final SimpleGenderArmor CHAIN_MAIL = new SimpleGenderArmor(0.5F, 0.2F);
    public static final SimpleGenderArmor GOLD = new SimpleGenderArmor(0.85F);
    public static final SimpleGenderArmor IRON = new SimpleGenderArmor(1F);
    public static final SimpleGenderArmor DIAMOND = new SimpleGenderArmor(1F);

    private final float physicsResistance;
    private final float tightness;

    public SimpleGenderArmor(float physicsResistance) {
        this(physicsResistance, 0F);
    }

    public SimpleGenderArmor(float physicsResistance, float tightness) {
        this.physicsResistance = physicsResistance;
        this.tightness = tightness;
    }

    @Override
    public float physicsResistance() {
        return this.physicsResistance;
    }

    @Override
    public float tightness() {
        return this.tightness;
    }
}
