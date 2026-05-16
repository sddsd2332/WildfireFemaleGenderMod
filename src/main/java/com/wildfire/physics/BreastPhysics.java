//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.render.armor.EmptyGenderArmor;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * 胸部物理模拟：依据移动、下落、乘骑、挥手等动作计算弹跳位移和旋转。
 */
public class BreastPhysics {
    private float bounceVel = 0.0F;
    private float targetBounce = 0.0F;
    private float velocity = 0.0F;
    private float wfg_femaleBreast;
    private float wfg_preBounce;
    private float bounceRotVel = 0.0F;
    private float targetRotVel = 0.0F;
    private float rotVelocity = 0.0F;
    private float wfg_bounceRotation;
    private float wfg_preBounceRotation;
    private float bounceVelX = 0.0F;
    private float targetBounceX = 0.0F;
    private float velocityX = 0.0F;
    private float wfg_femaleBreastX;
    private float wfg_preBounceX;
    private boolean justSneaking = false;
    private boolean alreadySleeping = false;
    private float breastSize = 0.0F;
    private float preBreastSize = 0.0F;
    private Vec3d motion;
    private Vec3d prePos;
    private GenderPlayer genderPlayer;
    private int randomB = 1;
    private boolean alreadyFalling = false;

    /**
     * 将所有物理位移逐帧衰减回静止状态。
     * 高版本只有在物理开启时才把这些值用于渲染；1.12 这里额外做归零，避免站立时残留速度导致胸部上下抖动。
     */
    /**
     * 物理关闭或玩家静止时把弹跳状态逐步归零，防止原地抖动。
     */
    public void settle() {
        settle(EmptyGenderArmor.INSTANCE);
    }

    public void settle(IGenderArmor armor) {
        this.wfg_preBounce = this.wfg_femaleBreast;
        this.wfg_preBounceX = this.wfg_femaleBreastX;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preBreastSize = this.breastSize;
        float targetBreastSize = getTargetBreastSize(armor);
        this.breastSize = lerp(0.5F, this.breastSize, targetBreastSize);
        this.targetBounce = 0.0F;
        this.targetBounceX = 0.0F;
        this.targetRotVel = 0.0F;
        this.velocity = lerp(0.35F, this.velocity, 0.0F);
        this.velocityX = lerp(0.35F, this.velocityX, 0.0F);
        this.rotVelocity = lerp(0.35F, this.rotVelocity, 0.0F);
        this.bounceVel = lerp(0.35F, this.bounceVel, 0.0F);
        this.bounceVelX = lerp(0.35F, this.bounceVelX, 0.0F);
        this.bounceRotVel = lerp(0.35F, this.bounceRotVel, 0.0F);
        this.wfg_femaleBreast = this.bounceVel;
        this.wfg_femaleBreastX = this.bounceVelX;
        this.wfg_bounceRotation = this.bounceRotVel;
    }

    public BreastPhysics(GenderPlayer genderPlayer) {
        this.genderPlayer = genderPlayer;
    }

    /**

     * 按高版本 BreastPhysics 的思路更新当前 tick 的目标位移、速度和插值状态。

     */

    public void update(EntityPlayer plr) {
        update(plr, EmptyGenderArmor.INSTANCE);
    }

    /**
     * 按高版本 BreastPhysics#update(Player, IGenderArmor) 更新物理状态，并应用护甲阻力与紧身度。
     *
     * @param plr 当前玩家。
     * @param armor 当前胸口装备对应的护甲交互配置。
     */
    public void update(EntityPlayer plr, IGenderArmor armor) {
        this.wfg_preBounce = this.wfg_femaleBreast;
        this.wfg_preBounceX = this.wfg_femaleBreastX;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preBreastSize = this.breastSize;
        float breastWeight = this.genderPlayer.getBustSize() * 1.25F;
        if (this.prePos == null) {
            this.prePos = new Vec3d(plr.posX, plr.posY, plr.posZ);
        } else {
            float targetBreastSize = getTargetBreastSize(armor);

            if (this.breastSize < targetBreastSize) {
                this.breastSize += Math.abs(this.breastSize - targetBreastSize) / 2.0F;
            } else {
                this.breastSize -= Math.abs(this.breastSize - targetBreastSize) / 2.0F;
            }

            this.motion = new Vec3d(plr.posX, plr.posY, plr.posZ).subtract(this.prePos);
            this.prePos = new Vec3d(plr.posX, plr.posY, plr.posZ);
            boolean isMoving = this.motion.lengthSquared() > 1.0E-6D || Math.abs(plr.limbSwingAmount) > 1.0E-4F || plr.fallDistance > 0.0F || plr.isSwingInProgress || plr.getRidingEntity() != null || plr.isSneaking() || plr.isPlayerSleeping();
            boolean isIdle = !isMoving;
            if (isIdle) {
                settle(armor);
                return;
            }
            float bounceIntensity = targetBreastSize * 3.0F * this.genderPlayer.getBounceMultiplier();
            if (!this.genderPlayer.getArmorPhysicsOverride()) {
                float resistance = MathHelper.clamp(armor.physicsResistance(), 0F, 1F);
                bounceIntensity *= 1F - resistance;
            }
            if (!this.genderPlayer.getBreasts().isUniboob()) {
                bounceIntensity *= WildfireHelper.randFloat(0.5F, 1.5F);
            }

            if (plr.fallDistance > 0.0F && !this.alreadyFalling) {
                this.randomB = WildfireHelper.randInt(0, 1) == 1 ? -1 : 1;
                this.alreadyFalling = true;
            }

            if (plr.fallDistance == 0.0F) {
                this.alreadyFalling = false;
            }
            this.targetBounce = (float) this.motion.y * bounceIntensity;
            this.targetBounce += breastWeight;

            float horizVel = (float) Math.sqrt(Math.pow(this.motion.x, 2.0) + Math.pow(this.motion.z, 2.0)) * bounceIntensity;
            this.targetRotVel = this.motion.lengthSquared() > 1.0E-6D ? -((plr.renderYawOffset - plr.prevRenderYawOffset) / 15.0F) * bounceIntensity : 0.0F;
            float f = 1.0F;
            f = (float) new Vec3d(plr.motionX, plr.motionY, plr.motionZ).lengthSquared();
            f /= 0.2F;
            f = f * f * f;
            if (f < 1.0F) {
                f = 1.0F;
            }

            this.targetBounce += MathHelper.cos(plr.limbSwing * 0.6662F + 3.1415927F) * 0.5F * plr.limbSwingAmount * 0.5F / f;
            this.targetRotVel += (float) this.motion.y * bounceIntensity * (float) this.randomB;

            if (plr.isSneaking() && !this.justSneaking) {
                this.justSneaking = true;
                this.targetBounce += bounceIntensity;
            }

            if (!plr.isSneaking() && this.justSneaking) {
                this.justSneaking = false;
                this.targetBounce += bounceIntensity;
            }

            float distanceFromMin;
            float distanceFromMax;
            float bounceAmount;
            if (plr.getRidingEntity() != null) {
                if (plr.getRidingEntity() instanceof EntityBoat boat) {
                    int rowTime = (int) boat.getRowingTime(0, plr.limbSwing);
                    int rowTime2 = (int) boat.getRowingTime(1, plr.limbSwing);
                    distanceFromMin = (float) MathHelper.clampedLerp(-1.0471975803375244, -0.2617993950843811, (double) ((MathHelper.sin((float) (-rowTime2)) + 1.0F) / 2.0F));
                    distanceFromMax = (float) MathHelper.clampedLerp(-0.7853981852531433, 0.7853981852531433, (double) ((MathHelper.sin((float) (-rowTime) + 1.0F) + 1.0F) / 2.0F));
                    if (distanceFromMin < -1.0F || distanceFromMax < -0.6F) {
                        this.targetBounce = bounceIntensity / 3.25F;
                    }
                }

                if (plr.getRidingEntity() instanceof EntityMinecart cart) {
                    float speed = (float) (cart.motionX * cart.motionX + cart.motionY * cart.motionY + cart.motionZ * cart.motionZ);
                    if (Math.random() * speed < 0.5f && speed > 0.2f) {
                        this.targetBounce = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
                    }
                }

                if (plr.getRidingEntity() instanceof AbstractHorse horse) {
                    bounceAmount = (float) Math.sqrt(horse.motionX * horse.motionX + horse.motionY * horse.motionY + horse.motionZ * horse.motionZ);
                    if (horse.ticksExisted % this.clampMovement(bounceAmount) == 5 && bounceAmount > 0.1F) {
                        this.targetBounce = bounceIntensity / 4.0F;
                    }
                }

                if (plr.getRidingEntity() instanceof EntityPig pig) {
                    bounceAmount = (float) Math.sqrt(pig.motionX * pig.motionX + pig.motionY * pig.motionY + pig.motionZ * pig.motionZ);
                    if (pig.ticksExisted % this.clampMovement(bounceAmount) == 5 && bounceAmount > 0.08F) {
                        this.targetBounce = bounceIntensity / 4.0F;
                    }
                }

            }

            if (plr.isSwingInProgress && plr.ticksExisted % 5 == 0 && !plr.isPlayerSleeping()) {
                if (Math.random() > 0.5) {
                    this.targetBounce += -0.25F * bounceIntensity;
                } else {
                    this.targetBounce += 0.25F * bounceIntensity;
                }
            }

            if (plr.isPlayerSleeping() && !this.alreadySleeping) {
                this.targetBounce = bounceIntensity;
                this.alreadySleeping = true;
            }

            if (!plr.isPlayerSleeping() && this.alreadySleeping) {
                this.targetBounce = bounceIntensity;
                this.alreadySleeping = false;
            }

            float percent = this.genderPlayer.getFloppiness();
            bounceAmount = 0.45F * (1.0F - percent) + 0.15F;
            bounceAmount = MathHelper.clamp(bounceAmount, 0.15F, 0.6F);
            float delta = 2.25F - 1.0F * bounceAmount;
            distanceFromMin = Math.abs(this.bounceVel + 0.5F) * 0.5F;
            distanceFromMax = Math.abs(this.bounceVel - 2.65F) * 0.5F;
            if (this.bounceVel < -0.5F) {
                this.targetBounce += distanceFromMin;
            }

            if (this.bounceVel > 2.5F) {
                this.targetBounce -= distanceFromMax;
            }

            if (this.targetBounce < -1.5F) {
                this.targetBounce = -1.5F;
            }

            if (this.targetBounce > 2.5F) {
                this.targetBounce = 2.5F;
            }

            if (this.targetRotVel < -25.0F) {
                this.targetRotVel = -25.0F;
            }

            if (this.targetRotVel > 25.0F) {
                this.targetRotVel = 25.0F;
            }

            this.velocity = lerp(bounceAmount, this.velocity, (this.targetBounce - this.bounceVel) * delta);
            this.bounceVel += this.velocity * percent * 1.1625F;
            this.velocityX = lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
            this.bounceVelX += this.velocityX * percent;
            this.rotVelocity = lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
            this.bounceRotVel += this.rotVelocity * percent;
            this.wfg_bounceRotation = this.bounceRotVel;
            this.wfg_femaleBreastX = this.bounceVelX;
            this.wfg_femaleBreast = this.bounceVel;
        }
    }

    public float getBreastSize(float partialTicks) {
        return lerp(partialTicks, this.preBreastSize, this.breastSize);
    }

    private float getTargetBreastSize(IGenderArmor armor) {
        if (!this.genderPlayer.canHaveBreasts()) {
            return 0.0F;
        }
        float targetBreastSize = this.genderPlayer.getBustSize();
        if (!this.genderPlayer.getArmorPhysicsOverride()) {
            float tightness = MathHelper.clamp(armor.tightness(), 0F, 1F);
            targetBreastSize *= 1F - 0.15F * tightness;
        }
        return targetBreastSize;
    }

    public float getPreBounceY() {
        return this.wfg_preBounce;
    }

    public float getBounceY() {
        return this.wfg_femaleBreast;
    }

    public float getPreBounceX() {
        return this.wfg_preBounceX;
    }

    public float getBounceX() {
        return this.wfg_femaleBreastX;
    }

    public float getBounceRotation() {
        return this.wfg_bounceRotation;
    }

    public float getPreBounceRotation() {
        return this.wfg_preBounceRotation;
    }


    private int clampMovement(float movement) {
        int val = (int) (10.0F - movement * 2.0F);
        if (val < 1) {
            val = 1;
        }

        return val;
    }

    public static float lerp(float pDelta, float pStart, float pEnd) {
        return pStart + pDelta * (pEnd - pStart);
    }
}
