package com.wildfire.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;

public class WildfireButton extends GuiButton {

    public boolean transparent = false;

    public WildfireButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int clr = 0x444444 + (84 << 24);
        if (this.isMouseOver()) clr = 0x666666 + (84 << 24);
        if (!this.enabled) clr = 0x222222 + (84 << 24);
        if (!transparent) mc.currentScreen.drawRect(x, y, x + width, y + height, clr);
        drawCenteredString(minecraft.fontRenderer, displayString, x, y, enabled ? 0xFFFFFF : 0x666666);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public WildfireButton setTransparent(boolean b) {
        this.transparent = b;
        return this;
    }
}
