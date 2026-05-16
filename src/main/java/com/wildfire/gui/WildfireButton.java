package com.wildfire.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * 1.12 版高版本 WildfireButton：使用半透明矩形替代原版按钮贴图，贴近 1.20.1 的 GUI 样式。
 */
public class WildfireButton extends GuiButton {
    public boolean transparent;
    private String prefix;
    private String state;
    private int stateColor;

    public WildfireButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int color = this.enabled ? 0x54000000 | (this.hovered ? 0x666666 : 0x444444) : 0x54222222;
        if (!this.transparent) {
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, color);
        }
        if (this.prefix != null && this.state != null) {
            String separator = "：";
            int totalWidth = minecraft.fontRenderer.getStringWidth(this.prefix + separator + this.state);
            int textX = this.x + (this.width - totalWidth) / 2;
            int textY = this.y + (this.height - 8) / 2;
            minecraft.fontRenderer.drawString(this.prefix + separator, textX, textY, this.enabled ? 0xFFFFFF : 0x666666);
            minecraft.fontRenderer.drawString(this.state, textX + minecraft.fontRenderer.getStringWidth(this.prefix + separator), textY, this.enabled ? this.stateColor : 0x666666);
        } else {
            this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, this.enabled ? 0xFFFFFF : 0x666666);
        }
    }

    public WildfireButton setTransparent(boolean transparent) {
        this.transparent = transparent;
        return this;
    }

    public WildfireButton setStateText(String prefix, String state, int stateColor) {
        this.prefix = prefix;
        this.state = state;
        this.stateColor = stateColor;
        this.displayString = prefix + "：" + state;
        return this;
    }
}
