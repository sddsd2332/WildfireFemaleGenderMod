package com.wildfire.api;

/**
 * 胸部护甲交互配置接口；1.12 没有高版本 capability，这里作为物品实现/默认映射的替代入口。
 */
public interface IGenderArmor {
    /**
     * 护甲是否覆盖胸部区域。
     *
     * @return 覆盖胸部时为 true。
     */
    default boolean coversBreasts() {
        return true;
    }

    /**
     * 是否无视玩家“穿盔甲时显示胸部”设置并始终隐藏胸部。
     *
     * @return 始终隐藏胸部时为 true。
     */
    default boolean alwaysHidesBreasts() {
        return false;
    }

    /**
     * 护甲物理阻力，0 为完全保留弹跳，1 为完全抑制弹跳。
     *
     * @return 0 到 1 的阻力值。
     */
    default float physicsResistance() {
        return 0F;
    }

    /**
     * 护甲紧身度，影响胸部尺寸，1 时最多缩小 15%。
     *
     * @return 0 到 1 的紧身度。
     */
    default float tightness() {
        return 0F;
    }
}
