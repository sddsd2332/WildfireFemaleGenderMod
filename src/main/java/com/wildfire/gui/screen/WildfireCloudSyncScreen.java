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

package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.GlobalConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCloudSyncScreen extends BaseWildfireScreen {

	private static final Text ENABLED = Text.translatable("wildfire_gender.label.enabled").formatted(Formatting.GREEN);
	private static final Text DISABLED = Text.translatable("wildfire_gender.label.disabled").formatted(Formatting.RED);
	private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/sync_bg.png");

	protected WildfireCloudSyncScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;
		int yPos = y - 44;
		int xPos = x + 60 / 2 - 1;

		this.addDrawableChild(new WildfireButton(this.width / 2 + 85, yPos - 11, 9, 9, Text.literal("X"), button -> close()));

		this.addDrawableChild(new WildfireButton(xPos - 15, yPos, 80, 20,
				CloudSync.isEnabled() ? ENABLED : DISABLED,
				button -> {
					var config = GlobalConfig.INSTANCE;
					config.set(GlobalConfig.CLOUD_SYNC_ENABLED, !config.get(GlobalConfig.CLOUD_SYNC_ENABLED));
					button.setMessage(CloudSync.isEnabled() ? ENABLED : DISABLED);
				}));

		var automaticTooltip = Tooltip.of(Text.empty()
				.append(Text.translatable("wildfire_gender.cloud.automatic.tooltip.line1"))
				.append("\n\n")
				.append(Text.translatable("wildfire_gender.cloud.automatic.tooltip.line2")));
		this.addDrawableChild(new WildfireButton(xPos - 15, yPos + 22, 80, 20,
				GlobalConfig.INSTANCE.get(GlobalConfig.AUTOMATIC_CLOUD_SYNC) ? ENABLED : DISABLED,
				button -> {
					var config = GlobalConfig.INSTANCE;
					var newVal = !config.get(GlobalConfig.AUTOMATIC_CLOUD_SYNC);
					config.set(GlobalConfig.AUTOMATIC_CLOUD_SYNC, newVal);
					button.setMessage(newVal ? ENABLED : DISABLED);
				}, automaticTooltip));

		var syncButton = new WildfireButton(xPos - 80, yPos + 80, 100, 15, Text.translatable("wildfire_gender.cloud.sync"), this::sync);
		syncButton.setActive(GlobalConfig.INSTANCE.get(GlobalConfig.CLOUD_SYNC_ENABLED));
		this.addDrawableChild(syncButton);

		super.init();
	}

	private void sync(ButtonWidget button) {
		button.active = false;
		button.setMessage(Text.translatable("wildfire_gender.cloud.syncing"));
		CloudSync.sync(Objects.requireNonNull(getPlayer()))
				.thenRun(() -> button.setMessage(Text.translatable("wildfire_gender.cloud.syncing.success")))
				.exceptionallyAsync(exc -> {
					WildfireGender.LOGGER.error("Failed to sync settings", exc);
					button.setMessage(Text.translatable("wildfire_gender.cloud.syncing.fail"));
					return null;
				});
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);
		ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, (this.width - 203) / 2, (this.height - 117) / 2, 0, 0, 203, 117, 256, 256);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if(client == null || client.world == null) return;
		super.render(ctx, mouseX, mouseY, delta);
		int x = this.width / 2;
		int y = this.height / 2;

		ctx.drawText(textRenderer, Text.translatable("wildfire_gender.cloud.status"), x - 95, y - 40, 0x000000, false);
		ctx.drawText(textRenderer, Text.translatable("wildfire_gender.cloud.automatic"), x - 95, y - 16, 0x000000, false);
	}

	@Override
	public void close() {
		GlobalConfig.INSTANCE.save();
		super.close();
	}
}