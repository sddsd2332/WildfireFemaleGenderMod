package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 角色设置界面：控制物理、护甲显示、弹跳倍率、动量和受伤音效。
 */
public class WildfireCharacterSettingsScreen extends GuiScreen {
    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/settings_bg.png");
    private final GuiScreen parent;
    private final UUID playerUUID;
    private GenderPlayer player;
    private boolean bounceWarning;
    private WildfireSlider bounceSlider;
    private WildfireSlider floppySlider;

    public WildfireCharacterSettingsScreen(GuiScreen parent, UUID uuid) {
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
        int x = this.width / 2;
        int y = this.height / 2;
        int yPos = y - 47;
        int xPos = x - 156 / 2 - 1;
        this.bounceWarning = shouldShowBounceWarning(this.player.getBounceMultiplierRaw());
        this.buttonList.add(new WildfireButton(0, this.width / 2 + 73, yPos - 11, 9, 9, "X"));
        this.buttonList.add(stateButton(1, xPos, yPos, "wildfire_gender.char_settings.physics", this.player.hasBreastPhysics()));
        this.buttonList.add(stateButton(2, xPos, yPos + 20, "wildfire_gender.char_settings.hide_in_armor", !this.player.showBreastsInArmor()));
        this.buttonList.add(stateButton(3, xPos, yPos + 40, "wildfire_gender.char_settings.override_armor_physics", this.player.getArmorPhysicsOverride()));
        this.buttonList.add(this.bounceSlider = new WildfireSlider(4, xPos, yPos + 60, 157, bounceText(this.player.getBounceMultiplierRaw()), 0F, 0.5F, this.player.getBounceMultiplierRaw(), value -> {
            this.player.updateBounceMultiplier(value);
            this.bounceWarning = shouldShowBounceWarning(value);
        }, value -> GenderPlayer.saveGenderInfo(this.player), this::bounceText));
        this.buttonList.add(this.floppySlider = new WildfireSlider(5, xPos, yPos + 80, 157, I18n.format("wildfire_gender.slider.floppy", Math.round(this.player.getFloppiness() * 100F)), 0.25F, 1F, this.player.getFloppiness(), value -> this.player.updateFloppiness(value), value -> GenderPlayer.saveGenderInfo(this.player), value -> I18n.format("wildfire_gender.slider.floppy", Math.round(value * 100F))));
        this.buttonList.add(stateButton(6, xPos, yPos + 100, "wildfire_gender.char_settings.hurt_sounds", this.player.hasHurtSounds()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            GenderPlayer.saveGenderInfo(this.player);
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 1) {
            this.player.updateBreastPhysics(!this.player.hasBreastPhysics());
            updateStateButton((WildfireButton) button, "wildfire_gender.char_settings.physics", this.player.hasBreastPhysics());
            GenderPlayer.saveGenderInfo(this.player);
        } else if (button.id == 2) {
            this.player.updateShowBreastsInArmor(!this.player.showBreastsInArmor());
            updateStateButton((WildfireButton) button, "wildfire_gender.char_settings.hide_in_armor", !this.player.showBreastsInArmor());
            GenderPlayer.saveGenderInfo(this.player);
        } else if (button.id == 3) {
            this.player.updateArmorPhysicsOverride(!this.player.getArmorPhysicsOverride());
            updateStateButton((WildfireButton) button, "wildfire_gender.char_settings.override_armor_physics", this.player.getArmorPhysicsOverride());
            GenderPlayer.saveGenderInfo(this.player);
        } else if (button.id == 6) {
            this.player.updateHurtSounds(!this.player.hasHurtSounds());
            updateStateButton((WildfireButton) button, "wildfire_gender.char_settings.hurt_sounds", this.player.hasHurtSounds());
            GenderPlayer.saveGenderInfo(this.player);
        }
    }

    @Override
    public void onGuiClosed() {
        saveSliders();
        if (this.player != null) {
            GenderPlayer.saveGenderInfo(this.player);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        saveSliders();
    }

    private void saveSliders() {
        if (this.bounceSlider != null) this.bounceSlider.save();
        if (this.floppySlider != null) this.floppySlider.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture((this.width - 172) / 2, (this.height - 124) / 2, 0, 0, 172, 144, 256, 256);
        int x = this.width / 2;
        int y = this.height / 2;
        this.fontRenderer.drawString(I18n.format("wildfire_gender.char_settings.title"), x - 79, y - 57, 0x444444);
        if (this.mc.player != null) {
            drawCenteredString(this.fontRenderer, this.mc.player.getDisplayNameString(), x, y - 77, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.bounceWarning) {
            drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("wildfire_gender.tooltip.bounce_warning"), x, y + 90, 0xFF6666);
        }
        drawButtonTooltip(mouseX, mouseY);
    }

    /**
     * 绘制角色设置按钮的悬停提示；滑条和胸部物理按钮按高版本逻辑不显示提示。
     *
     * @param mouseX 鼠标当前 X 坐标。
     * @param mouseY 鼠标当前 Y 坐标。
     */
    private void drawButtonTooltip(int mouseX, int mouseY) {
        GuiButton hoveredButton = getHoveredTooltipButton(mouseX, mouseY);
        if (hoveredButton == null) {
            return;
        }

        List<String> tooltip = tooltipForButton(hoveredButton.id);
        if (!tooltip.isEmpty()) {
            drawHoveringText(tooltip, mouseX, mouseY);
        }
    }

    /**
     * 查找鼠标指向的、需要提示的角色设置按钮。
     *
     * @param mouseX 鼠标当前 X 坐标。
     * @param mouseY 鼠标当前 Y 坐标。
     * @return 指向的按钮；没有可提示按钮时返回 null。
     */
    private GuiButton getHoveredTooltipButton(int mouseX, int mouseY) {
        for (GuiButton button : this.buttonList) {
            if (!button.visible || !button.enabled || !hasTooltip(button.id)) {
                continue;
            }
            if (mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height) {
                return button;
            }
        }
        return null;
    }

    /**
     * 判断按钮是否拥有高版本同款悬停提示。
     *
     * @param buttonId 按钮编号。
     * @return 有提示时为 true。
     */
    private boolean hasTooltip(int buttonId) {
        return buttonId == 2 || buttonId == 3 || buttonId == 6;
    }

    /**
     * 获取按钮对应的本地化提示文本。
     *
     * @param buttonId 按钮编号。
     * @return 1.12.2 悬停框可直接绘制的多行文本。
     */
    private List<String> tooltipForButton(int buttonId) {
        if (buttonId == 2) {
            return Collections.singletonList(I18n.format("wildfire_gender.tooltip.hide_in_armor"));
        }
        if (buttonId == 3) {
            return Arrays.asList(
                    I18n.format("wildfire_gender.tooltip.override_armor_physics.line1"),
                    "",
                    I18n.format("wildfire_gender.tooltip.override_armor_physics.line2")
            );
        }
        if (buttonId == 6) {
            return Collections.singletonList(I18n.format("wildfire_gender.tooltip.hurt_sounds"));
        }
        return Collections.emptyList();
    }

    private WildfireButton stateButton(int id, int x, int y, String key, boolean enabled) {
        return updateStateButton(new WildfireButton(id, x, y, 157, 20, ""), key, enabled);
    }

    private WildfireButton updateStateButton(WildfireButton button, String key, boolean enabled) {
        String pattern = I18n.format(key, "%s");
        int placeholder = pattern.indexOf("%s");
        String prefix = placeholder >= 0 ? pattern.substring(0, placeholder) : pattern;
        while (prefix.endsWith(" ") || prefix.endsWith(":" ) || prefix.endsWith("：")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String state = I18n.format(enabled ? "wildfire_gender.label.enabled" : "wildfire_gender.label.disabled");
        return button.setStateText(prefix, state, enabled ? 0x33FF33 : 0xFF3333);
    }

    private String bounceText(float value) {
        float bounceText = 3F * value;
        float rounded = Math.round(bounceText * 10F) / 10F;
        if (rounded == 1.5F) {
            return I18n.format("wildfire_gender.slider.max_bounce");
        }
        if (rounded == 0F) {
            return I18n.format("wildfire_gender.slider.min_bounce");
        }
        return I18n.format("wildfire_gender.slider.bounce", rounded);
    }

    /**
     * 判断弹跳强度是否需要显示警告；高版本逻辑是严格大于 100%。
     *
     * @param value 弹跳倍率原始值。
     * @return 显示红色警告时为 true。
     */
    private boolean shouldShowBounceWarning(float value) {
        return value * 300F > 100F;
    }
}
