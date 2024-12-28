/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.common.main.GenderPlayer;
import com.wildfire.common.main.WildfireHelper;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class BreastPhysics {

    private float bounceVel = 0, targetBounceY = 0, velocity = 0, wfg_femaleBreast, wfg_preBounce;
    private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, wfg_bounceRotation, wfg_preBounceRotation;
    private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, wfg_femaleBreastX, wfg_preBounceX;

    private boolean justSneaking = false, alreadySleeping = false;

    private float breastSize = 0, preBreastSize = 0;

    private Vec3d prePos;
    private final GenderPlayer genderPlayer;
    public BreastPhysics(GenderPlayer genderPlayer) {
        this.genderPlayer = genderPlayer;
    }

    private int randomB = 1;
    private boolean alreadyFalling = false;
    public void update(EntityPlayer plr, IGenderArmor armor) {
        this.wfg_preBounce = this.wfg_femaleBreast;
        this.wfg_preBounceX = this.wfg_femaleBreastX;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preBreastSize = this.breastSize;

        if(this.prePos == null) {
            this.prePos = new Vec3d(plr.posX,plr.posY,plr.posZ);
            return;
        }

        float breastWeight = genderPlayer.getBustSize() * 1.25f;
        float targetBreastSize = genderPlayer.getBustSize();

        if (!genderPlayer.getGender().canHaveBreasts()) {
            targetBreastSize = 0;
        } else if (!genderPlayer.getArmorPhysicsOverride()) { //skip resistance if physics is overridden
            float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
            //Scale breast size by how tight the armor is, clamping at a max adjustment of shrinking by 0.15
            targetBreastSize *= 1 - 0.15F * tightness;
        }

        if(breastSize < targetBreastSize) {
            breastSize += Math.abs(breastSize - targetBreastSize) / 2f;
        } else {
            breastSize -= Math.abs(breastSize - targetBreastSize) / 2f;
        }

        Vec3d motion = new Vec3d(plr.posX,plr.posY,plr.posZ).subtract(this.prePos);
       // Vec3d motion = plr.position().subtract(this.prePos);
        this.prePos = new Vec3d(plr.posX,plr.posY,plr.posZ);
        //WildfireGender.logger.debug("Motion: {}", motion);

        float bounceIntensity = (targetBreastSize * 3f) * genderPlayer.getBounceMultiplier();
        if (!genderPlayer.getArmorPhysicsOverride()) { //skip resistance if physics is overridden
            float resistance = MathHelper.clamp(armor.physicsResistance(), 0, 1);
            //Adjust bounce intensity by physics resistance of the worn armor
            bounceIntensity *= 1 - resistance;
        }

        if(!genderPlayer.getBreasts().isUniboob()) {
            bounceIntensity = bounceIntensity * WildfireHelper.randFloat(0.5f, 1.5f);
        }
        if(plr.fallDistance > 0 && !alreadyFalling) {
            randomB = plr.getEntityWorld().rand.nextBoolean() ? -1 : 1;
            alreadyFalling = true;
        }
        if(plr.fallDistance == 0) alreadyFalling = false;


        this.targetBounceY = (float) motion.y * bounceIntensity;
        this.targetBounceY += breastWeight;
        //float horizVel = (float) Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2)) * (bounceIntensity);
        //float horizLocal = -horizVel * ((plr.getRotationYawHead()-plr.renderYawOffset)<0?-1:1);
        this.targetRotVel = -((plr.renderYawOffset - plr.prevRenderYawOffset) / 15f) * bounceIntensity;



        float f = (float) (plr.motionX * plr.motionX + plr.motionY * plr.motionY + plr.motionZ *  plr.motionZ )/ 0.2F;
        f = f * f * f;

        if (f < 1.0F) {
            f = 1.0F;
        }

        this.targetBounceY += MathHelper.cos(plr.limbSwing * 0.6662F + (float)Math.PI) * 0.5F * plr.limbSwingAmount * 0.5F / f;
        //WildfireGender.logger.debug("Rotation yaw: {}", plr.rotationYaw);

        this.targetRotVel += (float) motion.y * bounceIntensity * randomB;


        if(plr.isSneaking() && !this.justSneaking) {
            this.justSneaking = true;
            this.targetBounceY += bounceIntensity;
        }
        if(!plr.isSneaking()  && this.justSneaking) {
            this.justSneaking = false;
            this.targetBounceY += bounceIntensity;
        }


        //button option for extra entities
        if(plr.getRidingEntity() != null) {
            if(plr.getRidingEntity() instanceof EntityBoat boat) {
                int rowTime = (int) boat.getRowingTime(0, plr.limbSwing);
                int rowTime2 = (int) boat.getRowingTime(1,  plr.limbSwing);

                float rotationL = (float)MathHelper.clampedLerp(-(float)Math.PI / 3F, -0.2617994F, (double)((MathHelper.sin(-rowTime2) + 1.0F) / 2.0F));
                float rotationR = (float)MathHelper.clampedLerp(-(float)Math.PI / 4F, (float)Math.PI / 4F, (double)((MathHelper.sin(-rowTime + 1.0F) + 1.0F) / 2.0F));
                //WildfireGender.logger.debug("{}, {}", rotationL, rotationR);
                if(rotationL < -1 || rotationR < -0.6f) {
                    this.targetBounceY = bounceIntensity / 3.25f;
                }
            }

            if(plr.getRidingEntity() instanceof EntityMinecart cart) {
                float speed = (float) (cart.motionX * cart.motionX + cart.motionY * cart.motionY + cart.motionZ *  cart.motionZ);
                if(Math.random() * speed < 0.5f && speed > 0.2f) {
                    this.targetBounceY = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
                }
            }
            if(plr.getRidingEntity() instanceof AbstractHorse horse) {
                float movement = (float)Math.sqrt(horse.motionX * horse.motionX + horse.motionY * horse.motionY + horse.motionZ *  horse.motionZ);
                if(horse.ticksExisted % clampMovement(movement) == 5 && movement > 0.1f) {
                    this.targetBounceY = bounceIntensity / 4f;
                }
            }
            if(plr.getRidingEntity() instanceof EntityPig pig) {
                float movement = (float)Math.sqrt(pig.motionX * pig.motionX + pig.motionY * pig.motionY + pig.motionZ *  pig.motionZ);
                if(pig.ticksExisted % clampMovement(movement) == 5 && movement > 0.08f) {
                    this.targetBounceY = bounceIntensity / 4f;
                }
            }
        }
        if(plr.isSwingInProgress && plr.ticksExisted % 5 == 0 &&!plr.isPlayerSleeping()) {
            this.targetBounceY += (Math.random() > 0.5 ? -0.25f : 0.25f) * bounceIntensity;
        }
        if(plr.isPlayerSleeping() && !this.alreadySleeping) {
            this.targetBounceY = bounceIntensity;
            this.alreadySleeping = true;
        }
        if(!plr.isPlayerSleeping() && this.alreadySleeping) {
            this.targetBounceY = bounceIntensity;
            this.alreadySleeping = false;
        }
		/*if(plr.getPose() == EntityPose.SWIMMING) {
			//WildfireGender.logger.debug(1 - plr.getRotationVec(tickDelta).getY());
			rotationMultiplier = 1 - (float) plr.getRotationVec(tickDelta).getY();
		}
		*/


        float percent =  genderPlayer.getFloppiness();
        float bounceAmount = 0.45f * (1f - percent) + 0.15f; //0.6f * percent - 0.15f;
        bounceAmount = MathHelper.clamp(bounceAmount, 0.15f, 0.6f);
        float delta = 2.25f - bounceAmount;
        //if(plr.isInWater()) delta = 0.75f - (1f * bounceAmount); //water resistance

        float distanceFromMin = Math.abs(bounceVel + 0.5f) * 0.5f;
        float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;

        if(bounceVel < -0.5f) {
            targetBounceY += distanceFromMin;
        }
        if(bounceVel > 2.5f) {
            targetBounceY -= distanceFromMax;
        }
        if(targetBounceY < -1.5f) targetBounceY = -1.5f;
        if(targetBounceY > 2.5f) targetBounceY = 2.5f;
        if(targetRotVel < -25f) targetRotVel = -25f;
        if(targetRotVel > 25f) targetRotVel = 25f;

        this.velocity = lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
        //this.preY = lerp(0.5f, this.preY, (this.targetBounce - this.bounceVel) * 1.25f);
        this.bounceVel += this.velocity * percent * 1.1625f;

        //X
        this.velocityX = lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity = lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        this.wfg_bounceRotation = this.bounceRotVel;
        this.wfg_femaleBreastX = this.bounceVelX;
        this.wfg_femaleBreast = this.bounceVel;
    }

    public float getBreastSize(float partialTicks) {
        return lerp(partialTicks, preBreastSize, breastSize);
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
        int val = (int) (10 - movement*2f);
        if(val < 1) val = 1;
        return val;
    }

    public static float lerp(float pDelta, float pStart, float pEnd) {
        return pStart + pDelta * (pEnd - pStart);
    }
}
