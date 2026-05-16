package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireBreastPresetList;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.BreastPresetConfiguration;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;

import java.io.IOException;
import java.util.UUID;

/**
 * 胸部外观设置界面：同步高版本的自定义/预设分页与角色预览。
 */
public class WildfireBreastCustomizationScreen extends GuiScreen {
    private final GuiScreen parent;
    private final UUID playerUUID;
    private GenderPlayer player;
    private Tab currentTab = Tab.CUSTOMIZATION;
    private WildfireButton customizationButton;
    private WildfireButton presetsButton;
    private WildfireButton dualPhysicsButton;
    private WildfireButton addPresetButton;
    private WildfireButton deletePresetButton;
    private WildfireBreastPresetList presetList;
    private WildfireSlider breastSlider;
    private WildfireSlider xOffsetSlider;
    private WildfireSlider yOffsetSlider;
    private WildfireSlider zOffsetSlider;
    private WildfireSlider cleavageSlider;

    public WildfireBreastCustomizationScreen(GuiScreen parent, UUID uuid) {
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
        int j = this.height / 2 - 11;
        this.buttonList.add(new WildfireButton(0, this.width / 2 + 178, j - 72, 9, 9, "X"));
        this.buttonList.add(this.customizationButton = new WildfireButton(1, this.width / 2 + 30, j - 60, 78, 10, I18n.format("wildfire_gender.breast_customization.tab_customization")));
        this.buttonList.add(this.presetsButton = new WildfireButton(2, this.width / 2 + 109, j - 60, 78, 10, I18n.format("wildfire_gender.breast_customization.tab_presets")));
        if (this.currentTab == Tab.CUSTOMIZATION) {
            addCustomizationControls(j);
        } else {
            addPresetControls(j);
        }
        updateTabButtons();
    }

    private void addCustomizationControls(int j) {
        int x = this.width / 2 + 30;
        this.buttonList.add(this.breastSlider = new WildfireSlider(10, x, j - 48, 158, I18n.format("wildfire_gender.wardrobe.slider.breast_size", Math.round(this.player.getBustSize() * 125F)), GenderPlayer.MIN_BUST_SIZE, GenderPlayer.MAX_BUST_SIZE, this.player.getBustSize(), value -> this.player.updateBustSize(value), value -> GenderPlayer.saveGenderInfo(this.player), value -> I18n.format("wildfire_gender.wardrobe.slider.breast_size", Math.round(value * 125F))));
        this.buttonList.add(this.xOffsetSlider = new WildfireSlider(11, x, j - 27, 158, separationText(this.player.getBreasts().getXOffset()), -1F, 1F, this.player.getBreasts().getXOffset(), value -> this.player.getBreasts().updateXOffset(value), value -> GenderPlayer.saveGenderInfo(this.player), this::separationText));
        this.buttonList.add(this.yOffsetSlider = new WildfireSlider(12, x, j - 6, 158, offsetText("wildfire_gender.wardrobe.slider.height", this.player.getBreasts().getYOffset()), -1F, 1F, this.player.getBreasts().getYOffset(), value -> this.player.getBreasts().updateYOffset(value), value -> GenderPlayer.saveGenderInfo(this.player), value -> offsetText("wildfire_gender.wardrobe.slider.height", value)));
        this.buttonList.add(this.zOffsetSlider = new WildfireSlider(13, x, j + 15, 158, offsetText("wildfire_gender.wardrobe.slider.depth", this.player.getBreasts().getZOffset()), -1F, 0F, this.player.getBreasts().getZOffset(), value -> this.player.getBreasts().updateZOffset(value), value -> GenderPlayer.saveGenderInfo(this.player), value -> offsetText("wildfire_gender.wardrobe.slider.depth", value)));
        this.buttonList.add(this.cleavageSlider = new WildfireSlider(14, x, j + 36, 158, rotationText(this.player.getBreasts().getCleavage()), 0F, 0.1F, this.player.getBreasts().getCleavage(), value -> this.player.getBreasts().updateCleavage(value), value -> GenderPlayer.saveGenderInfo(this.player), this::rotationText));
        this.buttonList.add(this.dualPhysicsButton = new WildfireButton(15, x, j + 57, 158, 20, I18n.format("wildfire_gender.breast_customization.dual_physics", I18n.format(this.player.getBreasts().isUniboob() ? "wildfire_gender.label.no" : "wildfire_gender.label.yes"))));
    }

    private void addPresetControls(int j) {
        int x = this.width / 2 + 30;
        this.presetList = new WildfireBreastPresetList(x, j - 48, 156, 125);
        this.buttonList.add(this.deletePresetButton = new WildfireButton(20, x, j + 80, 78, 12, I18n.format("wildfire_gender.breast_customization.presets.delete")));
        this.buttonList.add(this.addPresetButton = new WildfireButton(21, x + 80, j + 80, 78, 12, I18n.format("wildfire_gender.breast_customization.presets.add_new")));
        this.presetList.updateDeleteButton(this.deletePresetButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            GenderPlayer.saveGenderInfo(this.player);
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 1) {
            this.currentTab = Tab.CUSTOMIZATION;
            initGui();
        } else if (button.id == 2) {
            this.currentTab = Tab.PRESETS;
            initGui();
        } else if (button.id == 15) {
            this.player.getBreasts().updateUniboob(!this.player.getBreasts().isUniboob());
            button.displayString = I18n.format("wildfire_gender.breast_customization.dual_physics", I18n.format(this.player.getBreasts().isUniboob() ? "wildfire_gender.label.no" : "wildfire_gender.label.yes"));
            GenderPlayer.saveGenderInfo(this.player);
        } else if (button.id == 20 && this.presetList != null) {
            BreastPresetConfiguration selectedPreset = this.presetList.getSelectedPreset();
            if (selectedPreset != null) {
                if (!selectedPreset.delete()) {
                    System.err.println("Failed to delete breast preset " + selectedPreset.getPresetName());
                }
                this.presetList.clearSelection();
                this.presetList.refreshList();
                this.presetList.updateDeleteButton(this.deletePresetButton);
            }
        } else if (button.id == 21) {
            BreastPresetConfiguration.createFromPlayer(nextPresetName(), this.player);
            initGui();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.currentTab == Tab.PRESETS && this.presetList != null && this.presetList.mouseClicked(mouseX, mouseY)) {
            BreastPresetConfiguration selectedPreset = this.presetList.getSelectedPreset();
            if (selectedPreset != null) {
                selectedPreset.applyTo(this.player);
                GenderPlayer.saveGenderInfo(this.player);
                this.presetList.updateDeleteButton(this.deletePresetButton);
            }
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
        if (this.breastSlider != null) this.breastSlider.save();
        if (this.xOffsetSlider != null) this.xOffsetSlider.save();
        if (this.yOffsetSlider != null) this.yOffsetSlider.save();
        if (this.zOffsetSlider != null) this.zOffsetSlider.save();
        if (this.cleavageSlider != null) this.cleavageSlider.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, 0x66000000, 0x66000000);
        int x = this.width / 2;
        int y = this.height / 2;
        drawRect(x + 28, y - 85, x + 190, y + 68, 0x55000000);
        drawRect(x + 29, y - 84, x + 189, y - 71, 0x55000000);
        this.fontRenderer.drawString(I18n.format("wildfire_gender.appearance_settings.title"), x + 32, y - 81, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.currentTab == Tab.PRESETS && this.presetList != null) {
            this.presetList.draw(this.mc, mouseX, mouseY);
            if (!this.presetList.hasPresets()) {
                drawCenteredString(this.fontRenderer, I18n.format("wildfire_gender.breast_customization.presets.none"), x + 109, y - 4, 0xFFFFFF);
            }
        }
        if (this.mc.world != null) {
            EntityLivingBase entity = (EntityLivingBase) this.mc.world.getPlayerEntityByUUID(this.playerUUID);
            if (entity != null) {
                GuiInventory.drawEntityOnScreen(x - 102, y + 275, 200, -20F, -20F, entity);
            }
        }
    }

    /**
     * 同步高版本分页按钮的禁用状态：当前页按钮不可再次点击。
     */
    private void updateTabButtons() {
        if (this.customizationButton != null) {
            this.customizationButton.enabled = this.currentTab != Tab.CUSTOMIZATION;
        }
        if (this.presetsButton != null) {
            this.presetsButton.enabled = this.currentTab != Tab.PRESETS;
        }
    }

    /**
     * 格式化胸部水平分离值，和高版本一样显示一位小数映射后的整数。
     *
     * @param value 胸部 X 偏移。
     * @return 本地化后的滑条文本。
     */
    private String separationText(float value) {
        return offsetText("wildfire_gender.wardrobe.slider.separation", value);
    }

    /**
     * 格式化胸部偏移值，保持与高版本 Math.round(value * 10) 一致。
     *
     * @param key 本地化键。
     * @param value 偏移原始值。
     * @return 本地化后的滑条文本。
     */
    private String offsetText(String key, float value) {
        return I18n.format(key, Math.round((Math.round(value * 100F) / 100F) * 10F));
    }

    /**
     * 格式化沟壑/旋转滑条，和高版本一样显示角度。
     *
     * @param value 沟壑强度原始值。
     * @return 本地化后的滑条文本。
     */
    private String rotationText(float value) {
        return I18n.format("wildfire_gender.wardrobe.slider.rotation", Math.round((Math.round(value * 100F) / 100F) * 100F));
    }

    /**
     * 生成新的预设名称，避免覆盖已有文件。
     *
     * @return 可用的预设名称。
     */
    private String nextPresetName() {
        int index = 1;
        while (new java.io.File(com.wildfire.main.Configuration.ROOT_FOLDER + "/config/" + BreastPresetConfiguration.PRESET_FOLDER + "/Preset " + index + ".json").exists()) {
            index++;
        }
        return "Preset " + index;
    }

    private enum Tab {
        CUSTOMIZATION,
        PRESETS
    }
}
