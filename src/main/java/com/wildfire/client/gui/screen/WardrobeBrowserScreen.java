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


import com.wildfire.client.gui.WildfireButton;
import com.wildfire.common.main.GenderPlayer;
import com.wildfire.common.main.GenderPlayer.Gender;
import com.wildfire.common.main.WildfireGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import java.util.Calendar;
import java.util.UUID;

public class WardrobeBrowserScreen extends BaseWildfireScreen {

    private static final ResourceLocation BACKGROUND_FEMALE = WildfireGender.rl("textures/gui/wardrobe_bg2.png");
    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/wardrobe_bg3.png");
    private static final ResourceLocation TXTR_RIBBON = WildfireGender.rl("textures/bc_ribbon.png");

    private static final UUID CREATOR_UUID = UUID.fromString("33c937ae-6bfc-423e-a38e-3a613e7c1256");

    private final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;

    public WardrobeBrowserScreen(UUID uuid) {
        super(null, uuid);
    }

    @Override
    public void initGui() {
        int y = this.height / 2;

        GenderPlayer plr = getPlayer();

        this.addButton(new WildfireButton(this.width / 2 - 42, y - 52, 158, 20, getGenderLabel(plr.getGender()), button -> {
            Gender gender = switch (plr.getGender()) {
                case MALE -> Gender.FEMALE;
                case FEMALE -> Gender.OTHER;
                case OTHER -> Gender.MALE;
            };
            if (plr.updateGender(gender)) {
                sendChatMessage(getGenderLabel(gender));
                GenderPlayer.saveGenderInfo(plr);
                rebuildWidgets();
            }
        }));

        int yOffset = 32;
        if (plr.getGender().canHaveBreasts()) {
            this.addButton(new WildfireButton(this.width / 2 - 42, y - yOffset, 158, 20, I18n.translateToLocal("wildfire_gender.appearance_settings.title").append("..."),
                    button -> Minecraft.getMinecraft().displayGuiScreen(new WildfireBreastCustomizationScreen(WardrobeBrowserScreen.this, this.playerUUID))));
            yOffset -= 20;
        }
        this.addButton(new WildfireButton(this.width / 2 - 42, y - yOffset, 158, 20, I18n.translateToLocal("wildfire_gender.char_settings.title").append("..."),
                button -> Minecraft.getMinecraft().displayGuiScreen(new WildfireCharacterSettingsScreen(WardrobeBrowserScreen.this, this.playerUUID))));

        this.addButton(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Component.literal("X"),
                button -> Minecraft.getMinecraft().displayGuiScreen(parent)));

        super.initGui();
    }

    private String getGenderLabel(Gender gender) {
        return I18n.translateToLocal("wildfire_gender.label.gender") + " - " + gender.getDisplayName();
    }

    @Override
    public void drawWorldBackground(int tint) {
        super.drawWorldBackground(tint);
        ResourceLocation backgroundTexture = getPlayer().getGender().canHaveBreasts() ? BACKGROUND_FEMALE : BACKGROUND;
        graphics.blit(backgroundTexture, (this.width - 248) / 2, (this.height - 134) / 2, 0, 0, 248, 156);
    }

    @Override
    public void drawScreen(int f1, int f2, float f3) {
        drawWorldBackground(0);
        super.drawScreen(f1, f2, f3);

        int x = this.width / 2;
        int y = this.height / 2;

        graphics.drawString(this.font, title, x - 118, y - 62, 4473924, false);
        GlStateManager.color(1f, 1.0F, 1.0F, 1.0F);
        int xP = this.width / 2 - 82;
        int yP = this.height / 2 + 40;
        if (mc != null && mc.world != null) {
            EntityPlayer ent = mc.world.getPlayerEntityByUUID(this.playerUUID);
            if (ent != null) {
//                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, xP, yP, 45, xP - f1, yP - 107 + 75 - 40 - f2, ent);
            }
        }

        y = y - 45;
        if (isBreastCancerAwarenessMonth) {
            graphics.fill(x - 159, y + 106, x + 159, y + 136, 0x55000000);
            graphics.drawString(font, I18n.translateToLocal("wildfire_gender.cancer_awareness.title").withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC), this.width / 2 - 148, y + 117, 0xFFFFFF, false);
            graphics.blit(TXTR_RIBBON, x + 130, y + 109, 26, 26, 0, 0, 20, 20, 20, 20);
            y += 55;
        }

        if (mc != null && mc.player != null && mc.player.connection.getPlayerInfoMap().stream()
                .anyMatch(player -> player.getGameProfile().getId().equals(CREATOR_UUID))) {
            graphics.drawCenteredString(font, I18n.translateToLocal("wildfire_gender.label.with_creator"), this.width / 2, y + 89, 0xFF00FF);
        }
    }
}