package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

/**
 * 未穿戴覆盖胸部装备时使用的空护甲配置。
 */
public class EmptyGenderArmor implements IGenderArmor {
    public static final EmptyGenderArmor INSTANCE = new EmptyGenderArmor();

    private EmptyGenderArmor() {
    }

    @Override
    public boolean coversBreasts() {
        return false;
    }
}
