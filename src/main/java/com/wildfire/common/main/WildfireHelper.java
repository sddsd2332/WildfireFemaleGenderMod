/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.common.main;

import com.wildfire.api.IGenderArmor;
import com.wildfire.common.main.capabilities.Capabilities;
import com.wildfire.common.main.capabilities.DefaultStorageHelper;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.ThreadLocalRandom;

public class WildfireHelper implements IGenderArmor {


    public static void register() {
        CapabilityManager.INSTANCE.register(IGenderArmor.class, new DefaultStorageHelper.DefaultStorage<>(), WildfireHelper::new);
    }


    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max + 1);
    }

    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmptyGenderArmor.INSTANCE;
        }
        if (stack.getCapability(Capabilities.GENDER_ARMOR_CAPABILITY, null) != null) {
            if (stack.getItem() instanceof ItemArmor armorItem && armorItem.armorType == EntityEquipmentSlot.CHEST) {
                //Start by checking if it is a vanilla chestplate as we have custom configurations for those we check against
                // the armor material instead of the item instance in case any mods define custom armor items using vanilla
                // materials as then we can make a better guess at what we want the default implementation to be
                ItemArmor.ArmorMaterial material = armorItem.getArmorMaterial();
                if (material == ItemArmor.ArmorMaterial.LEATHER) {
                    return SimpleGenderArmor.LEATHER;
                } else if (material == ItemArmor.ArmorMaterial.CHAIN) {
                    return SimpleGenderArmor.CHAIN_MAIL;
                } else if (material == ItemArmor.ArmorMaterial.GOLD) {
                    return SimpleGenderArmor.GOLD;
                } else if (material == ItemArmor.ArmorMaterial.IRON) {
                    return SimpleGenderArmor.IRON;
                } else if (material == ItemArmor.ArmorMaterial.DIAMOND) {
                    return SimpleGenderArmor.DIAMOND;
                }
                //Otherwise just fallback to our default armor implementation
                return SimpleGenderArmor.FALLBACK;
            }
        }
        return EmptyGenderArmor.INSTANCE;
    }
}
