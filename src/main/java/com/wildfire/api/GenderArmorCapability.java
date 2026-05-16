package com.wildfire.api;

import com.wildfire.render.armor.EmptyGenderArmor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * 1.12.2 Forge capability 注册入口，用于让任意 ItemStack 暴露 IGenderArmor。
 */
public class GenderArmorCapability {
    @CapabilityInject(IGenderArmor.class)
    public static Capability<IGenderArmor> GENDER_ARMOR = null;

    private GenderArmorCapability() {
    }

    /**
     * 注册 IGenderArmor capability。
     */
    public static void register() {
        CapabilityManager.INSTANCE.register(IGenderArmor.class, new Capability.IStorage<IGenderArmor>() {
            @Override
            public NBTBase writeNBT(Capability<IGenderArmor> capability, IGenderArmor instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IGenderArmor> capability, IGenderArmor instance, EnumFacing side, NBTBase nbt) {
            }
        }, () -> EmptyGenderArmor.INSTANCE);
    }
}
