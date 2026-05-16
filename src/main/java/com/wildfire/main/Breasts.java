package com.wildfire.main;

/**
 * 胸部外观细节参数：偏移、合并胸部以及沟壑强度。
 */
public class Breasts {
    public static final float DEFAULT_X_OFFSET = 0.0F;
    public static final float DEFAULT_Y_OFFSET = 0.0F;
    public static final float DEFAULT_Z_OFFSET = 0.0F;
    public static final float DEFAULT_CLEAVAGE = 0.0F;
    public static final boolean DEFAULT_UNIBOOB = true;

    public float xOffset = 0.0F;
    public float yOffset = 0.0F;
    public float zOffset = 0.0F;
    public float cleavage = DEFAULT_CLEAVAGE;
    public boolean isUniboob = true;

    public Breasts() {
    }

    /**
     * 获取胸部水平偏移，和高版本 Breasts#getXOffset 对齐。
     */
    public float getXOffset() {
        return this.xOffset;
    }

    /**
     * 更新胸部水平偏移；合法范围和高版本 ClientConfiguration.BREASTS_OFFSET_X 一致。
     */
    public boolean updateXOffset(float value) {
        if (value < -1F || value > 1F) {
            return false;
        }
        this.xOffset = value;
        return true;
    }

    /**
     * 获取胸部高度偏移。
     */
    public float getYOffset() {
        return this.yOffset;
    }

    /**
     * 更新胸部高度偏移；合法范围和高版本一致。
     */
    public boolean updateYOffset(float value) {
        if (value < -1F || value > 1F) {
            return false;
        }
        this.yOffset = value;
        return true;
    }

    /**
     * 获取胸部深度偏移。
     */
    public float getZOffset() {
        return this.zOffset;
    }

    /**
     * 更新胸部深度偏移；高版本只允许 -1 到 0。
     */
    public boolean updateZOffset(float value) {
        if (value < -1F || value > 0F) {
            return false;
        }
        this.zOffset = value;
        return true;
    }

    /**
     * 获取胸部分离/旋转强度。
     */
    public float getCleavage() {
        return this.cleavage;
    }

    /**
     * 更新胸部分离/旋转强度；合法范围和高版本一致。
     */
    public boolean updateCleavage(float value) {
        if (value < 0F || value > 0.1F) {
            return false;
        }
        this.cleavage = value;
        return true;
    }

    /**
     * 是否使用合并胸部物理。
     */
    public boolean isUniboob() {
        return this.isUniboob;
    }

    /**
     * 更新合并胸部物理开关。
     */
    public boolean updateUniboob(boolean value) {
        this.isUniboob = value;
        return true;
    }

    /**
     * 从配置复制胸部参数，缺失项保留当前值。
     */
    public boolean copyFrom(Configuration copyFrom) {
        boolean changed = false;
        changed |= updateXOffset(configFloat(copyFrom, "breasts_xOffset", this.xOffset));
        changed |= updateYOffset(configFloat(copyFrom, "breasts_yOffset", this.yOffset));
        changed |= updateZOffset(configFloat(copyFrom, "breasts_zOffset", this.zOffset));
        changed |= updateCleavage(configFloat(copyFrom, "breasts_cleavage", this.cleavage));
        changed |= updateUniboob(configBool(copyFrom, "breasts_uniboob", this.isUniboob));
        return changed;
    }

    private static float configFloat(Configuration config, String key, float fallback) {
        try {
            Object value = config.getParameter(key);
            return value == null ? fallback : Float.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean configBool(Configuration config, String key, boolean fallback) {
        try {
            Object value = config.getParameter(key);
            return value == null ? fallback : Boolean.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
