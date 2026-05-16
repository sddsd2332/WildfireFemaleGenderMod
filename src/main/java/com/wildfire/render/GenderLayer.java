package com.wildfire.render;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.Breasts;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.GeneralClientConfig;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家女性胸部渲染层：复刻高版本 GenderLayer 的模型、UV 和矩阵变换顺序。
 */
public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {
    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_CACHE = new HashMap<>();
    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private final RenderPlayer renderer;
    private SteinModelRenderer.BreastModelBox leftBreast = new SteinModelRenderer.BreastModelBox(64, 64, 16, 17, -4F, 0F, 0F, 4, 5, 4, 0F, false);
    private SteinModelRenderer.BreastModelBox rightBreast = new SteinModelRenderer.BreastModelBox(64, 64, 20, 17, 0F, 0F, 0F, 4, 5, 4, 0F, false);
    private final SteinModelRenderer.OverlayModelBox leftWear = new SteinModelRenderer.OverlayModelBox(true, 64, 64, 17, 34, -4F, 0F, 0F, 4, 5, 3, 0F, false);
    private final SteinModelRenderer.OverlayModelBox rightWear = new SteinModelRenderer.OverlayModelBox(false, 64, 64, 21, 34, 0F, 0F, 0F, 4, 5, 3, 0F, false);
    private final SteinModelRenderer.BreastModelBox leftArmor = new SteinModelRenderer.BreastModelBox(64, 32, 16, 17, -4F, 0F, 0F, 4, 5, 3, 0F, false);
    private final SteinModelRenderer.BreastModelBox rightArmor = new SteinModelRenderer.BreastModelBox(64, 32, 20, 17, 0F, 0F, 0F, 4, 5, 3, 0F, false);
    private float preBreastSize;
    private float preBreastOffsetZ;

    public GenderLayer(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    /**
     * Forge 1.12 LayerRenderer 入口：判断性别/护甲后渲染左右胸部。
     */
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!WildfireGender.modEnabled || GeneralClientConfig.INSTANCE.disableRendering || player.isSpectator()) {
            return;
        }

        GenderPlayer genderPlayer = WildfireGender.getOrAddPlayerById(player.getUniqueID());
        if (!genderPlayer.canHaveBreasts() || genderPlayer.getBustSize() <= 0.01F) {
            return;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        IGenderArmor genderArmor = WildfireHelper.getArmorConfig(chest);
        boolean chestOccupied = genderArmor.coversBreasts();
        if (genderArmor.alwaysHidesBreasts() || chestOccupied && !genderPlayer.showBreastsInArmor()) {
            return;
        }
        float armorResistance = genderPlayer.getArmorPhysicsOverride() ? 0F : MathHelper.clamp(genderArmor.physicsResistance(), 0F, 1F);
        boolean bounceEnabled = genderPlayer.hasBreastPhysics() && (!chestOccupied || armorResistance < 1F);
        boolean breathingAnimation = armorResistance <= 0.5F && (!player.isInWater() || player.isPotionActive(MobEffects.WATER_BREATHING));
        float alpha = player.isInvisible() && Minecraft.getMinecraft().player != null && !player.isInvisibleToPlayer(Minecraft.getMinecraft().player) ? 0.15F : 1F;
        if (player.isInvisible() && alpha >= 1F) {
            return;
        }
        renderSide(player, genderPlayer, partialTicks, scale, chest, chestOccupied, bounceEnabled, breathingAnimation, alpha, true);
        renderSide(player, genderPlayer, partialTicks, scale, chest, chestOccupied, bounceEnabled, breathingAnimation, alpha, false);
    }

    /**
     * 按高版本 GenderLayer#renderBreastWithTransforms 的顺序应用位移和旋转。
     * 1.12 没有 PoseStack，因此这里使用 GlStateManager 逐步复现 body -> bounce -> offset -> rotate 的矩阵顺序。
     */
    /**
     * 按高版本 renderBreastWithTransforms 的矩阵顺序渲染单侧胸部。
     */
    private void renderSide(AbstractClientPlayer player, GenderPlayer genderPlayer, float partialTicks, float scale, ItemStack chest, boolean chestOccupied, boolean bounceEnabled, boolean breathingAnimation, float alpha, boolean left) {
        BreastPhysics physics = left || genderPlayer.getBreasts().isUniboob() ? genderPlayer.getLeftBreastPhysics() : genderPlayer.getRightBreastPhysics();
        Breasts breasts = genderPlayer.getBreasts();
        ModelBiped model = this.renderer.getMainModel();
        float breastSize = physics.getBreastSize(partialTicks);
        if (breastSize < 0.02F) {
            return;
        }

        float breastOffsetX = Math.round(Math.round(breasts.getXOffset() * 100F) / 100F * 10F) / 10F;
        float breastOffsetY = -Math.round(Math.round(breasts.getYOffset() * 100F) / 100F * 10F) / 10F;
        float breastOffsetZ = -Math.round(Math.round(breasts.getZOffset() * 100F) / 100F * 10F) / 10F;
        float outwardAngle = Math.min(Math.round(breasts.getCleavage() * 100F) / 100F * 100F, 10F);
        if (!left) {
            breastOffsetX = -breastOffsetX;
            outwardAngle = -outwardAngle;
        }

        float total = bounceEnabled ? BreastPhysics.lerp(partialTicks, physics.getPreBounceY(), physics.getBounceY()) : 0F;
        float totalX = bounceEnabled ? BreastPhysics.lerp(partialTicks, physics.getPreBounceX(), physics.getBounceX()) : 0F;
        float bounceRotation = bounceEnabled ? BreastPhysics.lerp(partialTicks, physics.getPreBounceRotation(), physics.getBounceRotation()) : 0F;
        float renderSize = breastSize * 1.5F;
        if (renderSize > 0.7F) renderSize = 0.7F;
        if (breastSize > 0.7F) renderSize = breastSize;
        float zOff = 0.0625F - breastSize * 0.0625F;
        renderSize = breastSize + 0.5F * Math.abs(breastSize - 0.7F) * 2F;
        updateDynamicBreastBoxes(breastSize, breastOffsetZ);

        GlStateManager.pushMatrix();
        if (alpha < 1F) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1F, 1F, 1F, alpha);
        }
        model.bipedBody.postRender(scale);
        if (bounceEnabled) {
            GlStateManager.translate(totalX / 32F, total / 32F, 0F);
        }
        GlStateManager.translate(breastOffsetX * 0.0625F, 0.05625F + breastOffsetY * 0.0625F, zOff - 0.125F + breastOffsetZ * 0.0625F);
        if (!breasts.isUniboob()) {
            GlStateManager.translate(-0.125F * (left ? 1F : -1F), 0F, 0F);
        }
        if (bounceEnabled) {
            GlStateManager.rotate(bounceRotation, 0F, 1F, 0F);
        }
        if (!breasts.isUniboob()) {
            GlStateManager.translate(0.125F * (left ? 1F : -1F), 0F, 0F);
        }

        float rotationMultiplier = 0F;
        if (bounceEnabled) {
            GlStateManager.translate(0F, -0.035F * renderSize, 0F);
            rotationMultiplier = -total / 12F;
        }
        float totalRotation = MathHelper.clamp(renderSize + rotationMultiplier, 0F, Math.min(renderSize + 0.2F, 1F));
        if (chestOccupied) {
            GlStateManager.translate(0F, 0F, 0.01F);
        }
        GlStateManager.rotate(outwardAngle, 0F, 1F, 0F);
        GlStateManager.rotate(-35F * totalRotation, 1F, 0F, 0F);
        if (breathingAnimation) {
            float breathing = -MathHelper.cos(player.ticksExisted * 0.09F) * 0.45F + 0.45F;
            GlStateManager.rotate(breathing, 1F, 0F, 0F);
        }
        GlStateManager.scale(0.9995F, 1F, 1F);
        this.renderer.bindTexture(player.getLocationSkin());
        renderBox(left ? leftBreast : rightBreast, 1F, 1F, 1F, alpha);
        if (player.isWearing(net.minecraft.entity.player.EnumPlayerModelParts.JACKET)) {
            GlStateManager.translate(0F, 0F, -0.012F);
            GlStateManager.scale(1.035F, 1.035F, 1.035F);
            renderBox(left ? leftWear : rightWear, 1F, 1F, 1F, alpha);
        }
        if (chestOccupied) {
            renderArmorBreast(player, chest, left, partialTicks);
        }
        if (alpha < 1F) {
            GlStateManager.disableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        GlStateManager.popMatrix();
    }

    private void updateDynamicBreastBoxes(float breastSize, float breastOffsetZ) {
        if (this.preBreastSize == breastSize && this.preBreastOffsetZ == breastOffsetZ) {
            return;
        }
        float reducer = 0F;
        if (breastSize < 0.84F) reducer++;
        if (breastSize < 0.72F) reducer++;
        int depth = MathHelper.clamp((int) (4 - breastOffsetZ - reducer), 1, 4);
        this.leftBreast = new SteinModelRenderer.BreastModelBox(64, 64, 16, 17, -4F, 0F, 0F, 4, 5, depth, 0F, false);
        this.rightBreast = new SteinModelRenderer.BreastModelBox(64, 64, 20, 17, 0F, 0F, 0F, 4, 5, depth, 0F, false);
        this.preBreastSize = breastSize;
        this.preBreastOffsetZ = breastOffsetZ;
    }

    /**
     * 渲染随胸部变形的胸甲覆盖层，补齐高版本 Render Breast Armor 在 1.12.2 的替代实现。
     *
     * @param player 当前渲染的玩家。
     * @param chest 胸甲物品栈。
     * @param left 是否为左侧胸部。
     */
    private void renderArmorBreast(AbstractClientPlayer player, ItemStack chest, boolean left, float partialTicks) {
        ItemArmor armor = (ItemArmor) chest.getItem();
        GlStateManager.pushMatrix();
        GlStateManager.translate(left ? 0.001F : -0.001F, 0.015F, -0.015F);
        GlStateManager.scale(1.05F, 1F, 1F);
        SteinModelRenderer.BreastModelBox armorBox = left ? this.leftArmor : this.rightArmor;
        if (armor.hasOverlay(chest)) {
            int color = armor.getColor(chest);
            float red = (float) (color >> 16 & 255) / 255.0F;
            float green = (float) (color >> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;
            this.renderer.bindTexture(getArmorResource(player, chest, null));
            renderBox(armorBox, red, green, blue, 1F);
            this.renderer.bindTexture(getArmorResource(player, chest, "overlay"));
            renderBox(armorBox, 1F, 1F, 1F, 1F);
        } else {
            this.renderer.bindTexture(getArmorResource(player, chest, null));
            renderBox(armorBox, 1F, 1F, 1F, 1F);
        }
        if (chest.hasEffect()) {
            renderArmorGlint(player, armorBox, partialTicks);
        }
        GlStateManager.popMatrix();
    }

    private void renderArmorGlint(AbstractClientPlayer player, SteinModelRenderer.BreastModelBox armorBox, float partialTicks) {
        float existed = (float) player.ticksExisted + partialTicks;
        this.renderer.bindTexture(ENCHANTED_ITEM_GLINT);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

        for (int pass = 0; pass < 2; ++pass) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - (float) pass * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, existed * (0.001F + (float) pass * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            renderBox(armorBox, 1F, 1F, 1F, 1F);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
    }

    /**
     * 获取胸甲贴图路径；等价于 1.12 LayerArmorBase#getArmorResource 的胸甲分支。
     *
     * @param player 当前玩家。
     * @param chest 胸甲物品栈。
     * @param type 贴图类型，overlay 或 null。
     * @return 可绑定的护甲贴图。
     */
    private ResourceLocation getArmorResource(AbstractClientPlayer player, ItemStack chest, String type) {
        ItemArmor armor = (ItemArmor) chest.getItem();
        String texture = armor.getArmorMaterial().getName();
        String domain = "minecraft";
        int index = texture.indexOf(':');
        if (index != -1) {
            domain = texture.substring(0, index);
            texture = texture.substring(index + 1);
        }
        String path = String.format("%s:textures/models/armor/%s_layer_1%s.png", domain, texture, type == null ? "" : String.format("_%s", type));
        path = ForgeHooksClient.getArmorTexture(player, chest, path, EntityEquipmentSlot.CHEST, type);
        ResourceLocation location = ARMOR_TEXTURE_CACHE.get(path);
        if (location == null) {
            location = new ResourceLocation(path);
            ARMOR_TEXTURE_CACHE.put(path, location);
        }
        return location;
    }

    private static void renderBox(SteinModelRenderer.BreastModelBox box, float red, float green, float blue, float alpha) {
        renderQuads(box.quads, red, green, blue, alpha);
    }

    private static void renderBox(SteinModelRenderer.OverlayModelBox box, float red, float green, float blue, float alpha) {
        renderQuads(box.quads, red, green, blue, alpha);
    }

    private static void renderQuads(SteinModelRenderer.TexturedQuad[] quads, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (SteinModelRenderer.TexturedQuad quad : quads) {
            Vec3d normal = quad.normal;
            for (SteinModelRenderer.PositionTextureVertex vertex : quad.vertexPositions) {
                Vec3d pos = vertex.vector3D;
                buffer.pos(pos.x / 16.0D, pos.y / 16.0D, pos.z / 16.0D)
                        .tex(vertex.texturePositionX, vertex.texturePositionY)
                        .color(red, green, blue, alpha)
                        .normal((float) normal.x, (float) normal.y, (float) normal.z)
                        .endVertex();
            }
        }
        tessellator.draw();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
