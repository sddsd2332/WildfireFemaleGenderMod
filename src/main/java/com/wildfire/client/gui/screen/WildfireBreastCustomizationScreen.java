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

package com.wildfire.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wildfire.client.gui.WildfireButton;
import com.wildfire.gui.WildfireBreastPresetList;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.common.main.Breasts;
import com.wildfire.common.main.GenderPlayer;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.config.ClientConfiguration;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLLoader;

import javax.annotation.Nonnull;
import java.util.UUID;

public class WildfireBreastCustomizationScreen extends BaseWildfireScreen {

    private WildfireSlider breastSlider, xOffsetBoobSlider, yOffsetBoobSlider, zOffsetBoobSlider, cleavageSlider;
    private WildfireButton btnDualPhysics, btnPresets, btnCustomization;
    private WildfireButton btnAddPreset, btnDeletePreset;

    private WildfireBreastPresetList PRESET_LIST;
    private Tab currentTab = Tab.CUSTOMIZATION;

    public WildfireBreastCustomizationScreen(GuiScreen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.appearance_settings.title"), parent, uuid);
    }

    @Override
    public void initGui() {
        int j = this.height / 2 - 11;

        GenderPlayer plr = getPlayer();
        Breasts breasts = plr.getBreasts();
        FloatConsumer onSave = value -> {
            //Just save as we updated the actual value in value change
            GenderPlayer.saveGenderInfo(plr);
        };

        this.addButton(new WildfireButton(this.width / 2 + 178, j - 72, 9, 9, Component.literal("X"),
                button -> Minecraft.getMinecraft().displayGuiScreen(parent)));

        //Customization Tab
        this.addButton(btnCustomization = new WildfireButton(this.width / 2 + 30, j - 60, 158 / 2 - 1, 10,
                I18n.translateToLocal("wildfire_gender.breast_customization.tab_customization"), button -> {
            currentTab = Tab.CUSTOMIZATION;
            updatePresetTab();
        }));
        //Presets Tab
        this.addButton(btnPresets = new WildfireButton(this.width / 2 + 31 + 79, j - 60, 158 / 2 - 1, 10,
                I18n.translateToLocal("wildfire_gender.breast_customization.tab_presets"), button -> {
            // TODO temporary release readiness fix: lock presets tab behind a development environment
            if (FMLLoader.isProduction()) return;

            currentTab = Tab.PRESETS;
            PRESET_LIST.refreshList();
            updatePresetTab();
        }));
        if (FMLLoader.isProduction()) {
            btnPresets.setTooltip(Tooltip.create(I18n.translateToLocal("wildfire_gender.coming_soon")));
        }
        this.addButton(btnAddPreset = new WildfireButton(this.width / 2 + 31 + 79, j + 80, 158 / 2 - 1, 12,
                I18n.translateToLocal("wildfire_gender.breast_customization.presets.add_new"), button -> {
            createNewPreset("Test Preset");
        }));

        this.addButton(btnDeletePreset = new WildfireButton(this.width / 2 + 30, j + 80, 158 / 2 - 1, 12,
                I18n.translateToLocal("wildfire_gender.breast_customization.presets.delete"), button -> {

        })).active = false;

        //Customization Tab Below

        this.addButton(this.breastSlider = new WildfireSlider(this.width / 2 + 30, j - 48, 158, 20, ClientConfiguration.BUST_SIZE, plr.getBustSize(),
                plr::updateBustSize, value ->I18n.translateToLocal("wildfire_gender.wardrobe.slider.breast_size", Math.round(value * 125)), onSave));

        //Customization
        this.addButton(this.xOffsetBoobSlider = new WildfireSlider(this.width / 2 + 30, j - 27, 158, 20, ClientConfiguration.BREASTS_OFFSET_X, breasts.getXOffset(),
                breasts::updateXOffset, value ->I18n.translateToLocal("wildfire_gender.wardrobe.slider.separation", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.addButton(this.yOffsetBoobSlider = new WildfireSlider(this.width / 2 + 30, j - 6, 158, 20, ClientConfiguration.BREASTS_OFFSET_Y, breasts.getYOffset(),
                breasts::updateYOffset, value ->I18n.translateToLocal("wildfire_gender.wardrobe.slider.height", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.addButton(this.zOffsetBoobSlider = new WildfireSlider(this.width / 2 + 30, j + 15, 158, 20, ClientConfiguration.BREASTS_OFFSET_Z, breasts.getZOffset(),
                breasts::updateZOffset, value ->I18n.translateToLocal("wildfire_gender.wardrobe.slider.depth", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));

        this.addButton(this.cleavageSlider = new WildfireSlider(this.width / 2 + 30, j + 36, 158, 20, ClientConfiguration.BREASTS_CLEAVAGE, breasts.getCleavage(),
                breasts::updateCleavage, value ->I18n.translateToLocal("wildfire_gender.wardrobe.slider.rotation", Math.round((Math.round(value * 100f) / 100f) * 100)), onSave));

        this.addButton(this.btnDualPhysics = new WildfireButton(this.width / 2 + 30, j + 57, 158, 20,
                I18n.translateToLocal("wildfire_gender.breast_customization.dual_physics",I18n.translateToLocal(breasts.isUniboob() ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")), button -> {
            boolean isUniboob = !breasts.isUniboob();
            if (breasts.updateUniboob(isUniboob)) {
                button.setMessage(Component.translatable("wildfire_gender.breast_customization.dual_physics",I18n.translateToLocal(isUniboob ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")));
                GenderPlayer.saveGenderInfo(plr);
            }
        }));

        //Preset Tab Below
        PRESET_LIST = new WildfireBreastPresetList(this, 156, (j - 48), (j + 77));
        PRESET_LIST.setLeftPos(this.width / 2 + 30);

        this.addWidget(this.PRESET_LIST);

        this.currentTab = Tab.CUSTOMIZATION;
        //Set default visibilities
        updatePresetTab();

        super.init();
    }

    private void createNewPreset(String presetName) {
        BreastPresetConfiguration cfg = new BreastPresetConfiguration(presetName);
        cfg.set(BreastPresetConfiguration.PRESET_NAME, presetName);
        GenderPlayer player = this.getPlayer();
        cfg.set(BreastPresetConfiguration.BUST_SIZE, player.getBustSize());
        cfg.set(BreastPresetConfiguration.BREASTS_UNIBOOB, player.getBreasts().isUniboob());
        cfg.set(BreastPresetConfiguration.BREASTS_CLEAVAGE, player.getBreasts().getCleavage());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_X, player.getBreasts().getXOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Y, player.getBreasts().getYOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Z, player.getBreasts().getZOffset());
        cfg.save();

        PRESET_LIST.refreshList();
    }

    private void updatePresetTab() {
        boolean displayBreastSettings = getPlayer().getGender().canHaveBreasts() && currentTab == Tab.CUSTOMIZATION;
        breastSlider.visible = displayBreastSettings;
        xOffsetBoobSlider.visible = displayBreastSettings;
        yOffsetBoobSlider.visible = displayBreastSettings;
        zOffsetBoobSlider.visible = displayBreastSettings;
        cleavageSlider.visible = displayBreastSettings;
        btnDualPhysics.visible = displayBreastSettings;
        PRESET_LIST.visible = currentTab == Tab.PRESETS;

        btnCustomization.active = currentTab != Tab.CUSTOMIZATION;
        btnPresets.active = currentTab != Tab.PRESETS;
        btnAddPreset.visible = currentTab == Tab.PRESETS;
        btnDeletePreset.visible = currentTab == Tab.PRESETS;
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics graphics) {
        super.renderBackground(graphics);
        int x = this.width / 2;
        int y = this.height / 2;
        graphics.fill(x + 28, y - 64 - 21, x + 190, y + 68, 0x55000000);
        graphics.fill(x + 29, y - 63 - 21, x + 189, y - 60, 0x55000000);
        graphics.drawString(font, getTitle(), x + 32, y - 60 - 21, 0xFFFFFF, false);
        if (currentTab == Tab.PRESETS) {
            graphics.fill(PRESET_LIST.getLeft(), PRESET_LIST.getTop(), PRESET_LIST.getRight(), PRESET_LIST.getBottom(), 0x55000000);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.width / 2;
        int y = this.height / 2;
        if (minecraft != null && minecraft.level != null) {
            Player ent = minecraft.level.getPlayerByUUID(this.playerUUID);
            if (ent != null) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x - 102, y + 275, 200, -20, -20, ent);
            }
        }

        if (currentTab == Tab.PRESETS) {
            PRESET_LIST.render(graphics, mouseX, mouseY, delta);
            if (!PRESET_LIST.hasPresets()) {
                graphics.drawCenteredString(font,I18n.translateToLocal("wildfire_gender.breast_customization.presets.none"), x + ((190 + 28) / 2), y - 4, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        //Ensure all sliders are saved
        breastSlider.save();
        xOffsetBoobSlider.save();
        yOffsetBoobSlider.save();
        zOffsetBoobSlider.save();
        cleavageSlider.save();
        return super.mouseReleased(mouseX, mouseY, state);
    }

    private enum Tab {
        CUSTOMIZATION,
        PRESETS
    }
}
