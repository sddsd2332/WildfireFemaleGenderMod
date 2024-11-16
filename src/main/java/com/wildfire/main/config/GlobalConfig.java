package com.wildfire.main.config;

public class GlobalConfig extends AbstractConfiguration {
    public static final GlobalConfig INSTANCE = new GlobalConfig();

    private GlobalConfig() {
        super(".", "wildfire_gender");
    }

    public static final BooleanConfigKey CLOUD_SYNC_ENABLED = new BooleanConfigKey("cloud_sync", false);
    public static final StringConfigKey CLOUD_SERVER = new StringConfigKey("cloud_server", "");

    static {
        INSTANCE.setDefault(CLOUD_SYNC_ENABLED);
        INSTANCE.setDefault(CLOUD_SERVER);
        if(!INSTANCE.exists()) {
            INSTANCE.save();
        }
    }
}
