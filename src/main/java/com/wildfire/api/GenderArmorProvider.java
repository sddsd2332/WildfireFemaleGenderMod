package com.wildfire.api;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 把固定 IGenderArmor 配置暴露为 ItemStack capability 的 provider。
 */
public class GenderArmorProvider implements ICapabilityProvider {
    private final IGenderArmor armor;

    public GenderArmorProvider(IGenderArmor armor) {
        this.armor = armor;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == GenderArmorCapability.GENDER_ARMOR;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == GenderArmorCapability.GENDER_ARMOR ? GenderArmorCapability.GENDER_ARMOR.cast(this.armor) : null;
    }
}
