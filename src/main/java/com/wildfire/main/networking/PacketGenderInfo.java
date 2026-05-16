package com.wildfire.main.networking;

import com.wildfire.main.Breasts;
import com.wildfire.main.GenderPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

/**
 * 1.12.2 的玩家性别同步基础包；字段顺序对齐高版本 PacketGenderInfo。
 */
public abstract class PacketGenderInfo implements IMessage {
    protected UUID uuid = new UUID(0L, 0L);
    private int gender = GenderPlayer.GENDER_MALE;
    private float bustSize = GenderPlayer.DEFAULT_BUST_SIZE;
    private boolean hurtSounds = GenderPlayer.DEFAULT_HURT_SOUNDS;
    private boolean breastPhysics = GenderPlayer.DEFAULT_BREAST_PHYSICS;
    private boolean showInArmor = GenderPlayer.DEFAULT_SHOW_IN_ARMOR;
    private float bounceMultiplier = GenderPlayer.DEFAULT_BOUNCE_MULTIPLIER;
    private float floppyMultiplier = GenderPlayer.DEFAULT_FLOPPY_MULTIPLIER;
    private float xOffset = Breasts.DEFAULT_X_OFFSET;
    private float yOffset = Breasts.DEFAULT_Y_OFFSET;
    private float zOffset = Breasts.DEFAULT_Z_OFFSET;
    private boolean uniboob = Breasts.DEFAULT_UNIBOOB;
    private float cleavage = Breasts.DEFAULT_CLEAVAGE;

    protected PacketGenderInfo() {
    }

    protected PacketGenderInfo(GenderPlayer player) {
        this(Snapshot.copy(player));
    }

    protected PacketGenderInfo(Snapshot snapshot) {
        if (snapshot != null) {
            this.uuid = snapshot.uuid;
            this.gender = snapshot.gender;
            this.bustSize = snapshot.bustSize;
            this.hurtSounds = snapshot.hurtSounds;
            this.breastPhysics = snapshot.breastPhysics;
            this.showInArmor = snapshot.showInArmor;
            this.bounceMultiplier = snapshot.bounceMultiplier;
            this.floppyMultiplier = snapshot.floppyMultiplier;
            this.xOffset = snapshot.xOffset;
            this.yOffset = snapshot.yOffset;
            this.zOffset = snapshot.zOffset;
            this.uniboob = snapshot.uniboob;
            this.cleavage = snapshot.cleavage;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.gender = buf.readInt();
        this.bustSize = buf.readFloat();
        this.hurtSounds = buf.readBoolean();
        this.breastPhysics = buf.readBoolean();
        this.showInArmor = buf.readBoolean();
        this.bounceMultiplier = buf.readFloat();
        this.floppyMultiplier = buf.readFloat();
        this.xOffset = buf.readFloat();
        this.yOffset = buf.readFloat();
        this.zOffset = buf.readFloat();
        this.uniboob = buf.readBoolean();
        this.cleavage = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        buf.writeInt(this.gender);
        buf.writeFloat(this.bustSize);
        buf.writeBoolean(this.hurtSounds);
        buf.writeBoolean(this.breastPhysics);
        buf.writeBoolean(this.showInArmor);
        buf.writeFloat(this.bounceMultiplier);
        buf.writeFloat(this.floppyMultiplier);
        buf.writeFloat(this.xOffset);
        buf.writeFloat(this.yOffset);
        buf.writeFloat(this.zOffset);
        buf.writeBoolean(this.uniboob);
        buf.writeFloat(this.cleavage);
    }

    protected void updatePlayerFromPacket(GenderPlayer player) {
        player.updateGender(this.gender);
        player.updateBustSize(this.bustSize);
        player.updateHurtSounds(this.hurtSounds);
        player.updateBreastPhysics(this.breastPhysics);
        player.updateShowBreastsInArmor(this.showInArmor);
        player.updateBounceMultiplier(this.bounceMultiplier);
        player.updateFloppiness(this.floppyMultiplier);

        Breasts breasts = player.getBreasts();
        breasts.updateXOffset(this.xOffset);
        breasts.updateYOffset(this.yOffset);
        breasts.updateZOffset(this.zOffset);
        breasts.updateUniboob(this.uniboob);
        breasts.updateCleavage(this.cleavage);
    }

    /**
     * 从主线程复制出的不可变同步快照；异步线程只处理这个快照，不接触 Minecraft 对象。
     */
    public static class Snapshot {
        public final UUID uuid;
        private final int gender;
        private final float bustSize;
        private final boolean hurtSounds;
        private final boolean breastPhysics;
        private final boolean showInArmor;
        private final float bounceMultiplier;
        private final float floppyMultiplier;
        private final float xOffset;
        private final float yOffset;
        private final float zOffset;
        private final boolean uniboob;
        private final float cleavage;

        private Snapshot(UUID uuid, int gender, float bustSize, boolean hurtSounds, boolean breastPhysics,
                         boolean showInArmor, float bounceMultiplier, float floppyMultiplier, float xOffset,
                         float yOffset, float zOffset, boolean uniboob, float cleavage) {
            this.uuid = uuid;
            this.gender = gender;
            this.bustSize = bustSize;
            this.hurtSounds = hurtSounds;
            this.breastPhysics = breastPhysics;
            this.showInArmor = showInArmor;
            this.bounceMultiplier = bounceMultiplier;
            this.floppyMultiplier = floppyMultiplier;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.uniboob = uniboob;
            this.cleavage = cleavage;
        }

        public static Snapshot copy(GenderPlayer player) {
            if (player == null || player.username == null) {
                return null;
            }
            try {
                Breasts breasts = player.getBreasts();
                return new Snapshot(
                        UUID.fromString(player.username),
                        player.getGender(),
                        player.getBustSize(),
                        player.hasHurtSounds(),
                        player.hasBreastPhysics(),
                        player.showBreastsInArmor(),
                        player.getBounceMultiplierRaw(),
                        player.getFloppiness(),
                        breasts.getXOffset(),
                        breasts.getYOffset(),
                        breasts.getZOffset(),
                        breasts.isUniboob(),
                        breasts.getCleavage()
                );
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        public String key() {
            return this.uuid + "|" + this.gender + "|" + this.bustSize + "|" + this.hurtSounds + "|"
                    + this.breastPhysics + "|" + this.showInArmor + "|" + this.bounceMultiplier + "|"
                    + this.floppyMultiplier + "|" + this.xOffset + "|" + this.yOffset + "|"
                    + this.zOffset + "|" + this.uniboob + "|" + this.cleavage;
        }
    }
}
