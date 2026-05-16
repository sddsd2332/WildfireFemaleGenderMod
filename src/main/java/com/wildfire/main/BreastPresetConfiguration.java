package com.wildfire.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 胸部预设配置文件；对应高版本 BreastPresetConfiguration 的 1.12 JSON 实现。
 */
public class BreastPresetConfiguration extends Configuration {
    public static final String PRESET_FOLDER = "WildfireGender/presets";
    public static final String PRESET_NAME = "preset_name";
    public static final String BUST_SIZE = "bust_size";
    public static final String BREASTS_OFFSET_X = "breasts_xOffset";
    public static final String BREASTS_OFFSET_Y = "breasts_yOffset";
    public static final String BREASTS_OFFSET_Z = "breasts_zOffset";
    public static final String BREASTS_UNIBOOB = "breasts_uniboob";
    public static final String BREASTS_CLEAVAGE = "breasts_cleavage";

    private final String fileName;

    public BreastPresetConfiguration(String presetName) {
        super(PRESET_FOLDER, sanitize(presetName));
        this.fileName = sanitize(presetName);
        setDefaultParameter(PRESET_NAME, presetName);
        setDefaultParameter(BUST_SIZE, GenderPlayer.DEFAULT_BUST_SIZE);
        setDefaultParameter(BREASTS_OFFSET_X, 0F);
        setDefaultParameter(BREASTS_OFFSET_Y, 0F);
        setDefaultParameter(BREASTS_OFFSET_Z, 0F);
        setDefaultParameter(BREASTS_UNIBOOB, true);
        setDefaultParameter(BREASTS_CLEAVAGE, 0.05F);
        finish();
    }

    /**
     * 读取所有本地胸部预设文件。
     *
     * @return 已加载的预设配置数组。
     */
    public static BreastPresetConfiguration[] getBreastPresetConfigurationFiles() {
        File presetDir = new File(Configuration.ROOT_FOLDER + "/config/" + PRESET_FOLDER + "/");
        File[] presetFiles = presetDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (presetFiles == null || presetFiles.length == 0) {
            return new BreastPresetConfiguration[0];
        }
        List<BreastPresetConfiguration> configs = new ArrayList<>();
        for (File file : presetFiles) {
            String name = file.getName().substring(0, file.getName().length() - ".json".length());
            configs.add(new BreastPresetConfiguration(name));
        }
        return configs.toArray(new BreastPresetConfiguration[0]);
    }

    /**
     * 从玩家当前外观生成并保存预设。
     *
     * @param presetName 预设名称。
     * @param player 当前玩家数据。
     * @return 创建出的配置。
     */
    public static BreastPresetConfiguration createFromPlayer(String presetName, GenderPlayer player) {
        BreastPresetConfiguration config = new BreastPresetConfiguration(presetName);
        config.setParameter(PRESET_NAME, presetName);
        config.setParameter(BUST_SIZE, player.getBustSize());
        config.setParameter(BREASTS_OFFSET_X, player.getBreasts().getXOffset());
        config.setParameter(BREASTS_OFFSET_Y, player.getBreasts().getYOffset());
        config.setParameter(BREASTS_OFFSET_Z, player.getBreasts().getZOffset());
        config.setParameter(BREASTS_UNIBOOB, player.getBreasts().isUniboob());
        config.setParameter(BREASTS_CLEAVAGE, player.getBreasts().getCleavage());
        config.save();
        return config;
    }

    /**
     * 把预设应用到指定玩家。
     *
     * @param player 目标玩家数据。
     */
    public void applyTo(GenderPlayer player) {
        player.updateBustSize(getFloat(BUST_SIZE, GenderPlayer.DEFAULT_BUST_SIZE));
        player.getBreasts().updateXOffset(getFloat(BREASTS_OFFSET_X, Breasts.DEFAULT_X_OFFSET));
        player.getBreasts().updateYOffset(getFloat(BREASTS_OFFSET_Y, Breasts.DEFAULT_Y_OFFSET));
        player.getBreasts().updateZOffset(getFloat(BREASTS_OFFSET_Z, Breasts.DEFAULT_Z_OFFSET));
        player.getBreasts().updateUniboob(getBoolean(BREASTS_UNIBOOB, Breasts.DEFAULT_UNIBOOB));
        player.getBreasts().updateCleavage(getFloat(BREASTS_CLEAVAGE, Breasts.DEFAULT_CLEAVAGE));
    }

    public String getPresetName() {
        Object value = getParameter(PRESET_NAME);
        return value == null ? this.fileName : value.toString();
    }

    public boolean delete() {
        File file = new File(Configuration.ROOT_FOLDER + "/config/" + PRESET_FOLDER + "/" + this.fileName + ".json");
        return !file.exists() || file.delete();
    }

    private float getFloat(String key, float fallback) {
        try {
            Object value = getParameter(key);
            return value == null ? fallback : Float.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean getBoolean(String key, boolean fallback) {
        try {
            Object value = getParameter(key);
            return value == null ? fallback : Boolean.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String sanitize(String name) {
        String sanitized = name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isEmpty() ? "Preset" : sanitized;
    }
}
