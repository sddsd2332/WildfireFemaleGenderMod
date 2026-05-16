package com.wildfire.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

/**
 * 1.12 GUI 滑条控件：实时更新内存值，并在释放/关闭时触发保存。
 */
public class WildfireSlider extends GuiButton {
    /** 滑条值变化时实时回写到 GenderPlayer 内存对象。 */
    public interface ChangeHandler {
        void onChange(float value);
    }

    /** 松开鼠标或关闭界面时执行持久化，模拟高版本 WildfireSlider#save。 */
    public interface SaveHandler {
        void onSave(float value);
    }

    /** 根据当前数值生成显示文本，避免滑条拖动后仍显示旧值。 */
    public interface MessageHandler {
        String getMessage(float value);
    }

    private final float min;
    private final float max;
    private final String label;
    private final ChangeHandler handler;
    private final SaveHandler saveHandler;
    private final MessageHandler messageHandler;
    private boolean dragging;
    private float value;

    public WildfireSlider(int id, int x, int y, int width, String label, float min, float max, float value, ChangeHandler handler) {
        this(id, x, y, width, label, min, max, value, handler, null, null);
    }

    public WildfireSlider(int id, int x, int y, int width, String label, float min, float max, float value, ChangeHandler handler, SaveHandler saveHandler) {
        this(id, x, y, width, label, min, max, value, handler, saveHandler, null);
    }

    public WildfireSlider(int id, int x, int y, int width, String label, float min, float max, float value, ChangeHandler handler, SaveHandler saveHandler, MessageHandler messageHandler) {
        super(id, x, y, width, 20, "");
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = MathHelper.clamp(value, min, max);
        this.handler = handler;
        this.saveHandler = saveHandler;
        this.messageHandler = messageHandler;
        updateDisplay();
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.dragging) {
            setValue(this.min + (this.max - this.min) * MathHelper.clamp((float) (mouseX - this.x) / (float) this.width, 0F, 1F));
        }
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xCC202050);
        int filled = this.x + (int) ((this.value - this.min) / (this.max - this.min) * this.width);
        drawRect(this.x, this.y, filled, this.y + this.height, 0xCC2D2D7A);
        drawRect(filled - 1, this.y, filled + 1, this.y + this.height, 0xFFE6E6E6);
        this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFF);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.dragging = true;
            setValue(this.min + (this.max - this.min) * MathHelper.clamp((float) (mouseX - (this.x + 4)) / (float) (this.width - 8), 0F, 1F));
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
        save();
    }

    public float getValue() {
        return this.value;
    }

    /**

     * 触发滑条保存回调，对齐高版本 WildfireSlider#save。

     */

    public void save() {
        if (this.saveHandler != null) {
            this.saveHandler.onSave(this.value);
        }
    }

    private void setValue(float value) {
        this.value = MathHelper.clamp(value, this.min, this.max);
        updateDisplay();
        this.handler.onChange(this.value);
    }

    private void updateDisplay() {
        this.displayString = this.messageHandler == null ? this.label + ": " + String.format("%.2f", this.value) : this.messageHandler.getMessage(this.value);
    }
}
