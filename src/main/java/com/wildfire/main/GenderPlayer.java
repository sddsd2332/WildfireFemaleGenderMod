package com.wildfire.main;

import com.wildfire.physics.BreastPhysics;

/**
 * 单个玩家的性别、胸部外观、物理、音效和同步状态数据；对应高版本 GenderPlayer。
 */
public class GenderPlayer {
    public static final int GENDER_FEMALE = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_OTHER = 2;
    public static final float DEFAULT_BUST_SIZE = 0.6F;
    public static final float MIN_BUST_SIZE = 0.0F;
    public static final float MAX_BUST_SIZE = 0.8F;
    public static final boolean DEFAULT_HURT_SOUNDS = true;
    public static final boolean DEFAULT_BREAST_PHYSICS = true;
    public static final boolean DEFAULT_ARMOR_PHYSICS_OVERRIDE = false;
    public static final boolean DEFAULT_SHOW_IN_ARMOR = true;
    public static final float DEFAULT_BOUNCE_MULTIPLIER = 0.34F;
    public static final float DEFAULT_FLOPPY_MULTIPLIER = 0.75F;

    public String username;
    public boolean needsSync;
    public long lastLocalSaveTime;
    public int gender;
    public float pBustSize;
    public boolean hurtSounds;
    public boolean breast_physics;
    public boolean breast_physics_armor;
    public float bounceMultiplier;
    public float floppyMultiplier;
    public SyncStatus syncStatus;
    public boolean show_in_armor;
    private Configuration cfg;
    private BreastPhysics lBreastPhysics;
    private BreastPhysics rBreastPhysics;
    private Breasts breasts;


    public GenderPlayer(String username) {
        this(username, 1);
    }

    public GenderPlayer(String username, int gender) {
        this.pBustSize = DEFAULT_BUST_SIZE;
        this.hurtSounds = DEFAULT_HURT_SOUNDS;
        this.breast_physics = DEFAULT_BREAST_PHYSICS;
        this.breast_physics_armor = DEFAULT_ARMOR_PHYSICS_OVERRIDE;
        this.bounceMultiplier = DEFAULT_BOUNCE_MULTIPLIER;
        this.floppyMultiplier = DEFAULT_FLOPPY_MULTIPLIER;
        this.syncStatus = GenderPlayer.SyncStatus.UNKNOWN;
        this.show_in_armor = DEFAULT_SHOW_IN_ARMOR;
        this.lBreastPhysics = new BreastPhysics(this);
        this.rBreastPhysics = new BreastPhysics(this);
        this.breasts = new Breasts();
        this.username = username;
        this.gender = gender;
        this.cfg = new Configuration("WildfireGender", this.username);
        this.cfg.setDefaultParameter("username", "NOT_AVAILABLE");
        this.cfg.setDefaultParameter("gender", GENDER_MALE);
        this.cfg.setDefaultParameter("bust_size", DEFAULT_BUST_SIZE);
        this.cfg.setDefaultParameter("hurt_sounds", DEFAULT_HURT_SOUNDS);
        this.cfg.setDefaultParameter("breasts_xOffset", Breasts.DEFAULT_X_OFFSET);
        this.cfg.setDefaultParameter("breasts_yOffset", Breasts.DEFAULT_Y_OFFSET);
        this.cfg.setDefaultParameter("breasts_zOffset", Breasts.DEFAULT_Z_OFFSET);
        this.cfg.setDefaultParameter("breasts_uniboob", Breasts.DEFAULT_UNIBOOB);
        this.cfg.setDefaultParameter("breasts_cleavage", Breasts.DEFAULT_CLEAVAGE);
        this.cfg.setDefaultParameter("breast_physics", DEFAULT_BREAST_PHYSICS);
        this.cfg.setDefaultParameter("armor_physics_override", DEFAULT_ARMOR_PHYSICS_OVERRIDE);
        this.cfg.setDefaultParameter("breast_physics_armor", DEFAULT_ARMOR_PHYSICS_OVERRIDE);
        this.cfg.setDefaultParameter("show_in_armor", DEFAULT_SHOW_IN_ARMOR);
        this.cfg.setDefaultParameter("bounce_multiplier", DEFAULT_BOUNCE_MULTIPLIER);
        this.cfg.setDefaultParameter("floppy_multiplier", DEFAULT_FLOPPY_MULTIPLIER);
        this.cfg.finish();
    }
    public Configuration getConfig() {
        return this.cfg;
    }

    public float getBustSize() {
        return this.pBustSize;
    }

    /**
     * 获取当前性别编号；1.12 用 int 代替高版本 Gender enum。
     */
    public int getGender() {
        return this.gender;
    }

    /**
     * 获取高版本等价的 Gender 枚举视图；底层仍以 1.12 配置兼容的 int 存储。
     */
    public Gender getGenderType() {
        return Gender.fromId(this.gender);
    }

    /**
     * 更新性别；只接受女性/男性/其他三个高版本等价值。
     */
    public boolean updateGender(int value) {
        if (value < GENDER_FEMALE || value > GENDER_OTHER) {
            return false;
        }
        this.gender = value;
        return true;
    }

    public boolean updateGender(Gender value) {
        return value != null && updateGender(value.id);
    }

    /**
     * 当前性别是否拥有女性受伤音效；等价于高版本 Gender#getHurtSound 是否非空。
     */
    public boolean hasGenderHurtSound() {
        return this.getGenderType().hasHurtSound();
    }

    public float getBounceMultiplier() {
        return (float)Math.round(this.getBounceMultiplierRaw() * 3.0F * 100.0F) / 100.0F;
    }

    /**
     * 获取未乘 3 的原始弹跳倍率，高版本网络/配置保存使用该值。
     */
    public float getBounceMultiplierRaw() {
        return this.bounceMultiplier;
    }

    public float getFloppiness() {
        return Float.valueOf(this.floppyMultiplier);
    }

    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    public boolean updateBustSize(float v) {
        if (v < MIN_BUST_SIZE || v > MAX_BUST_SIZE) {
            return false;
        }
        this.pBustSize = v;
        return true;
    }

    public boolean hasHurtSounds() {
        return this.hurtSounds;
    }

    public boolean updateHurtSounds(boolean value) {
        this.hurtSounds = value;
        return true;
    }

    public boolean hasBreastPhysics() {
        return this.breast_physics;
    }

    public boolean updateBreastPhysics(boolean value) {
        this.breast_physics = value;
        return true;
    }

    public boolean getArmorPhysicsOverride() {
        return this.breast_physics_armor;
    }

    public boolean updateArmorPhysicsOverride(boolean value) {
        this.breast_physics_armor = value;
        return true;
    }

    public boolean showBreastsInArmor() {
        return this.show_in_armor;
    }

    public boolean updateShowBreastsInArmor(boolean value) {
        this.show_in_armor = value;
        return true;
    }

    public boolean updateBounceMultiplier(float value) {
        if (value < 0F || value > 0.5F) {
            return false;
        }
        this.bounceMultiplier = value;
        return true;
    }

    public boolean updateFloppiness(float value) {
        if (value < 0.25F || value > 1F) {
            return false;
        }
        this.floppyMultiplier = value;
        return true;
    }

    /**
     * 当前性别是否允许显示胸部，等价于高版本 Gender#canHaveBreasts。
     */
    public boolean canHaveBreasts() {
        return this.getGenderType().canHaveBreasts();
    }

    /**

     * 从本地缓存读取玩家配置，读取失败时使用高版本默认值。

     */

    public static GenderPlayer loadCachedPlayer(String uuid) {
        GenderPlayer plr = WildfireGender.getPlayerByName(uuid);
        if (plr == null) {
            plr = new GenderPlayer(uuid);
        }
        plr.syncStatus = GenderPlayer.SyncStatus.CACHED;

        try {
            plr.gender = Integer.valueOf(plr.getConfig().getParameter("gender").toString());
        } catch (Exception var3) {
            plr.gender = Boolean.valueOf(String.valueOf(plr.getConfig().getParameter("gender"))) ? 1 : 0;
        }

        plr.updateBustSize(configFloat(plr, "bust_size", DEFAULT_BUST_SIZE));
        plr.updateHurtSounds(configBool(plr, "hurt_sounds", DEFAULT_HURT_SOUNDS));
        plr.updateBreastPhysics(configBool(plr, "breast_physics", DEFAULT_BREAST_PHYSICS));
        plr.updateArmorPhysicsOverride(configBool(plr, "armor_physics_override", configBool(plr, "breast_physics_armor", DEFAULT_ARMOR_PHYSICS_OVERRIDE)));
        plr.updateShowBreastsInArmor(configBool(plr, "show_in_armor", DEFAULT_SHOW_IN_ARMOR));
        plr.updateBounceMultiplier(configFloat(plr, "bounce_multiplier", DEFAULT_BOUNCE_MULTIPLIER));
        plr.updateFloppiness(configFloat(plr, "floppy_multiplier", DEFAULT_FLOPPY_MULTIPLIER));
        plr.getBreasts().copyFrom(plr.getConfig());
        return plr;
    }

    private static float configFloat(GenderPlayer player, String key, float fallback) {
        try {
            Object value = player.getConfig().getParameter(key);
            return value == null ? fallback : Float.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean configBool(GenderPlayer player, String key, boolean fallback) {
        try {
            Object value = player.getConfig().getParameter(key);
            return value == null ? fallback : Boolean.valueOf(value.toString());
        } catch (Exception ignored) {
            return fallback;
        }
    }


    /**


     * 把当前玩家的所有可配置项写回 config/WildfireGender/<uuid>.json。


     */


    public static void saveGenderInfo(GenderPlayer plr) {
        plr.getConfig().setParameter("username", plr.username);
        plr.getConfig().setParameter("gender", plr.gender);
        plr.getConfig().setParameter("bust_size", plr.getBustSize());
        plr.getConfig().setParameter("hurt_sounds", plr.hurtSounds);
        plr.getConfig().setParameter("breast_physics", plr.breast_physics);
        plr.getConfig().setParameter("armor_physics_override", plr.breast_physics_armor);
        plr.getConfig().setParameter("breast_physics_armor", plr.breast_physics_armor);
        plr.getConfig().setParameter("show_in_armor", plr.show_in_armor);
        plr.getConfig().setParameter("bounce_multiplier", plr.bounceMultiplier);
        plr.getConfig().setParameter("floppy_multiplier", plr.floppyMultiplier);
        plr.getConfig().setParameter("breasts_xOffset", plr.getBreasts().xOffset);
        plr.getConfig().setParameter("breasts_yOffset", plr.getBreasts().yOffset);
        plr.getConfig().setParameter("breasts_zOffset", plr.getBreasts().zOffset);
        plr.getConfig().setParameter("breasts_uniboob", plr.getBreasts().isUniboob);
        plr.getConfig().setParameter("breasts_cleavage", plr.getBreasts().cleavage);
        plr.getConfig().save();
        plr.needsSync = true;
        plr.lastLocalSaveTime = System.currentTimeMillis();
        WildfireGender.putPlayer(plr);
    }

    public Breasts getBreasts() {
        return this.breasts;
    }


    public BreastPhysics getLeftBreastPhysics() {
        return this.lBreastPhysics;
    }

    public BreastPhysics getRightBreastPhysics() {
        return this.rBreastPhysics;
    }

    public static enum SyncStatus {
        CACHED,
        SYNCED,
        UNKNOWN;

        private SyncStatus() {
        }
    }

    /**
     * 高版本 Gender enum 的 1.12 适配；ordinal/id 与原配置数字保持一致。
     */
    public enum Gender {
        FEMALE(GENDER_FEMALE, true, true),
        MALE(GENDER_MALE, false, false),
        OTHER(GENDER_OTHER, true, false);

        private static final Gender[] BY_ID = new Gender[]{FEMALE, MALE, OTHER};
        private final int id;
        private final boolean canHaveBreasts;
        private final boolean hurtSound;

        Gender(int id, boolean canHaveBreasts, boolean hurtSound) {
            this.id = id;
            this.canHaveBreasts = canHaveBreasts;
            this.hurtSound = hurtSound;
        }

        public int getId() {
            return this.id;
        }

        public boolean canHaveBreasts() {
            return this.canHaveBreasts;
        }

        public boolean hasHurtSound() {
            return this.hurtSound;
        }

        public static Gender fromId(int id) {
            return id >= 0 && id < BY_ID.length ? BY_ID[id] : MALE;
        }
    }
}
