package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

/**
 * 衣柜主界面：显示角色预览、切换性别，并进入外观/角色设置。
 */
public class WardrobeBrowserScreen extends GuiScreen {
    private static final ResourceLocation BACKGROUND_FEMALE = WildfireGender.rl("textures/gui/wardrobe_bg2.png");
    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/wardrobe_bg3.png");
    private static final ResourceLocation TXTR_RIBBON = WildfireGender.rl("textures/bc_ribbon.png");
    private static final UUID CREATOR_UUID = UUID.fromString("33c937ae-6bfc-423e-a38e-3a613e7c1256");

    public static float modelRotation = 0.5F;
    private final GuiScreen parent;
    private final UUID playerUUID;
    private final boolean breastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;
    private GenderPlayer player;

    public WardrobeBrowserScreen(GuiScreen parent, UUID uuid) {
        this.parent = parent;
        this.playerUUID = uuid;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.player = WildfireGender.getOrAddPlayerById(this.playerUUID);
        int y = this.height / 2;
        int buttonX = this.width / 2 - 42;
        this.buttonList.add(new WildfireButton(0, buttonX, y - 52, 158, 20, genderText()));
        int yOffset = 32;
        if (canHaveBreasts()) {
            this.buttonList.add(new WildfireButton(1, buttonX, y - yOffset, 158, 20, I18n.format("wildfire_gender.appearance_settings.title") + "..."));
            yOffset -= 20;
        }
        this.buttonList.add(new WildfireButton(2, buttonX, y - yOffset, 158, 20, I18n.format("wildfire_gender.char_settings.title") + "..."));
        this.buttonList.add(new WildfireButton(3, this.width / 2 + 111, y - 63, 9, 9, "X"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            if (this.player.getGenderType() == GenderPlayer.Gender.MALE) {
                this.player.updateGender(GenderPlayer.Gender.FEMALE);
            } else if (this.player.getGenderType() == GenderPlayer.Gender.FEMALE) {
                this.player.updateGender(GenderPlayer.Gender.OTHER);
            } else {
                this.player.updateGender(GenderPlayer.Gender.MALE);
            }
            GenderPlayer.saveGenderInfo(this.player);
            this.mc.displayGuiScreen(new WardrobeBrowserScreen(this.parent, this.playerUUID));
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(new WildfireBreastCustomizationScreen(this, this.playerUUID));
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(new WildfireCharacterSettingsScreen(this, this.playerUUID));
        } else if (button.id == 3) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = canHaveBreasts() ? BACKGROUND_FEMALE : BACKGROUND;
        this.mc.getTextureManager().bindTexture(background);
        drawModalRectWithCustomSizedTexture((this.width - 248) / 2, (this.height - 134) / 2, 0, 0, 248, 156, 256, 256);

        int x = this.width / 2;
        int y = this.height / 2;
        this.fontRenderer.drawString(I18n.format("wildfire_gender.wardrobe.title"), x - 118, y - 62, 0x444444);
        if (this.mc.world != null) {
            EntityLivingBase entity = (EntityLivingBase) this.mc.world.getPlayerEntityByUUID(this.playerUUID);
            if (entity != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.enableGUIStandardItemLighting();
                int xP = this.width / 2 - 82;
                int yP = this.height / 2 + 40;
                GuiInventory.drawEntityOnScreen(xP, yP, 45, (float)(xP - mouseX), (float)(yP - 72 - mouseY), entity);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        y = y - 45;
        if (this.breastCancerAwarenessMonth) {
            drawRect(x - 159, y + 106, x + 159, y + 136, 0x55000000);
            this.fontRenderer.drawString(TextFormatting.BOLD.toString() + TextFormatting.ITALIC + I18n.format("wildfire_gender.cancer_awareness.title"), this.width / 2 - 148, y + 117, 0xFFFFFF);
            this.mc.getTextureManager().bindTexture(TXTR_RIBBON);
            drawModalRectWithCustomSizedTexture(x + 130, y + 109, 0, 0, 20, 20, 20, 20);
            y += 55;
        }
        if (this.mc.player != null && this.mc.player.connection != null && this.mc.player.connection.getPlayerInfo(CREATOR_UUID) != null) {
            drawCenteredString(this.fontRenderer, I18n.format("wildfire_gender.label.with_creator"), this.width / 2, y + 89, 0xFF00FF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean canHaveBreasts() {
        return this.player != null && this.player.canHaveBreasts();
    }

    private String genderText() {
        String label = I18n.format("wildfire_gender.label.gender") + " - ";
        if (this.player.getGenderType() == GenderPlayer.Gender.FEMALE) {
            return label + TextFormatting.LIGHT_PURPLE + I18n.format("wildfire_gender.label.female");
        }
        if (this.player.getGenderType() == GenderPlayer.Gender.MALE) {
            return label + TextFormatting.BLUE + I18n.format("wildfire_gender.label.male");
        }
        return label + TextFormatting.GREEN + I18n.format("wildfire_gender.label.other");
    }
}
