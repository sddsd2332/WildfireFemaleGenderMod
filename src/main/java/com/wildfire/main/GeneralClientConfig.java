package com.wildfire.main;

/**
 * 高版本 GeneralClientConfig 的 1.12 简化实现，提供客户端全局开关。
 */
public class GeneralClientConfig {
    public static final GeneralClientConfig INSTANCE = new GeneralClientConfig();

    public boolean disableRendering;
    public boolean disableSoundReplacement;
    private final Configuration config;

    private GeneralClientConfig() {
        this.config = new Configuration("WildfireGender", "client");
        this.config.setDefaultParameter("disableRendering", false);
        this.config.setDefaultParameter("disableSoundReplacement", false);
        this.config.setDefaultParameter("disable_rendering", false);
        this.config.setDefaultParameter("disable_sound_replacement", false);
        this.config.finish();
        load();
    }

    /**
     * 从 config/WildfireGender/client.json 读取客户端全局开关。
     */
    public void load() {
        this.disableRendering = getBoolean("disableRendering", getBoolean("disable_rendering", false));
        this.disableSoundReplacement = getBoolean("disableSoundReplacement", getBoolean("disable_sound_replacement", false));
    }

    private boolean getBoolean(String key, boolean fallback) {
        try {
            Object value = this.config.getParameter(key);
            return value == null ? fallback : Boolean.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
