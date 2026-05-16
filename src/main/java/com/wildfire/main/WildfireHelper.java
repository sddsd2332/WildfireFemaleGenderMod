package com.wildfire.main;

import com.wildfire.api.GenderArmorCapability;
import com.wildfire.api.IGenderArmor;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 通用工具方法：随机数、护甲交互配置和 1.12 capability 适配。
 */
public class WildfireHelper {
    private WildfireHelper() {
    }

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static float randFloat(float min, float max) {
        return (float)ThreadLocalRandom.current().nextDouble((double)min, (double)max + 1.0);
    }

    /**
     * 获取胸甲与女性胸部模型的交互配置；优先读取 ItemStack capability，然后回退到物品接口和材质默认值。
     *
     * @param stack 胸口装备栏物品。
     * @return 护甲交互配置。
     */
    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmptyGenderArmor.INSTANCE;
        }
        if (GenderArmorCapability.GENDER_ARMOR != null && stack.hasCapability(GenderArmorCapability.GENDER_ARMOR, null)) {
            IGenderArmor armor = stack.getCapability(GenderArmorCapability.GENDER_ARMOR, null);
            if (armor != null) {
                return armor;
            }
        }
        if (stack.getItem() instanceof IGenderArmor) {
            return (IGenderArmor) stack.getItem();
        }
        return getDefaultArmorConfig(stack);
    }

    /**
     * 按材质推断胸甲默认交互配置，不读取 ItemStack capability。
     *
     * @param stack 胸口装备栏物品。
     * @return 材质默认护甲配置。
     */
    private static IGenderArmor getDefaultArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmptyGenderArmor.INSTANCE;
        }
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            if (armor.getEquipmentSlot() == EntityEquipmentSlot.CHEST) {
                ItemArmor.ArmorMaterial material = armor.getArmorMaterial();
                if (material == ItemArmor.ArmorMaterial.LEATHER) {
                    return SimpleGenderArmor.LEATHER;
                }
                if (material == ItemArmor.ArmorMaterial.CHAIN) {
                    return SimpleGenderArmor.CHAIN_MAIL;
                }
                if (material == ItemArmor.ArmorMaterial.GOLD) {
                    return SimpleGenderArmor.GOLD;
                }
                if (material == ItemArmor.ArmorMaterial.IRON) {
                    return SimpleGenderArmor.IRON;
                }
                if (material == ItemArmor.ArmorMaterial.DIAMOND) {
                    return SimpleGenderArmor.DIAMOND;
                }
                return SimpleGenderArmor.FALLBACK;
            }
        }
        return EmptyGenderArmor.INSTANCE;
    }
}
