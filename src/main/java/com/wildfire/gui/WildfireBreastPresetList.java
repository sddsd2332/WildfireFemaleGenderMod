package com.wildfire.gui;

import com.wildfire.main.BreastPresetConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.12 外观预设列表；用简单滚动/选中列表替代高版本 AbstractSelectionList。
 */
public class WildfireBreastPresetList {
    private final int left;
    private final int top;
    private final int width;
    private final int height;
    private final List<BreastPresetConfiguration> presets = new ArrayList<>();
    private int selectedIndex = -1;

    public WildfireBreastPresetList(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        refreshList();
    }

    /**
     * 重新扫描 config/WildfireGender/presets 下的预设文件。
     */
    public void refreshList() {
        this.presets.clear();
        for (BreastPresetConfiguration config : BreastPresetConfiguration.getBreastPresetConfigurationFiles()) {
            this.presets.add(config);
        }
        if (this.selectedIndex >= this.presets.size()) {
            this.selectedIndex = this.presets.isEmpty() ? -1 : this.presets.size() - 1;
        }
    }

    /**
     * 清空当前选中的预设，用于删除后避免继续引用旧文件。
     */
    public void clearSelection() {
        this.selectedIndex = -1;
    }

    public boolean hasPresets() {
        return !this.presets.isEmpty();
    }

    public BreastPresetConfiguration getSelectedPreset() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.presets.size() ? this.presets.get(this.selectedIndex) : null;
    }

    /**
     * 绘制预设列表条目。
     *
     * @param minecraft Minecraft 客户端。
     * @param mouseX 鼠标 X。
     * @param mouseY 鼠标 Y。
     */
    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        Gui.drawRect(this.left, this.top, this.left + this.width, this.top + this.height, 0x55000000);
        int y = this.top + 2;
        for (int index = 0; index < this.presets.size(); index++) {
            boolean selected = index == this.selectedIndex;
            boolean hovered = mouseX >= this.left && mouseY >= y && mouseX < this.left + this.width && mouseY < y + 20;
            int color = selected ? 0xCC2D2D7A : hovered ? 0x88333355 : 0x55202020;
            Gui.drawRect(this.left + 2, y, this.left + this.width - 2, y + 18, color);
            minecraft.fontRenderer.drawString(this.presets.get(index).getPresetName(), this.left + 6, y + 5, 0xFFFFFF);
            y += 20;
            if (y + 18 > this.top + this.height) {
                break;
            }
        }
    }

    /**
     * 处理列表点击，选中对应预设。
     *
     * @param mouseX 鼠标 X。
     * @param mouseY 鼠标 Y。
     * @return 点击到条目时为 true。
     */
    public boolean mouseClicked(int mouseX, int mouseY) {
        if (mouseX < this.left || mouseX >= this.left + this.width || mouseY < this.top || mouseY >= this.top + this.height) {
            return false;
        }
        int index = (mouseY - this.top - 2) / 20;
        if (index >= 0 && index < this.presets.size()) {
            this.selectedIndex = index;
            return true;
        }
        return false;
    }

    /**
     * 生成给“删除”按钮使用的可用状态。
     *
     * @param button 删除按钮。
     */
    public void updateDeleteButton(GuiButton button) {
        if (button != null) {
            button.enabled = getSelectedPreset() != null;
        }
    }
}
