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

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncingTooFrequentlyException;
import com.wildfire.main.config.GlobalConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Environment(EnvType.CLIENT)
public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

	//TODO: PROPER TRANSLATIONS

	private static final Text ENABLED = Text.translatable("wildfire_gender.label.enabled").formatted(Formatting.GREEN);
	private static final Text DISABLED = Text.translatable("wildfire_gender.label.disabled").formatted(Formatting.RED);
	private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/first_time_bg.png");

	public WildfireFirstTimeSetupScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {

		//var config = GlobalConfig.INSTANCE;
		//					config.set(GlobalConfig.CLOUD_SYNC_ENABLED, !config.get(GlobalConfig.CLOUD_SYNC_ENABLED));

		int x = this.width / 2;
		int y = this.height / 2;

		this.addDrawableChild(new WildfireButton(x + 1, y + 75, 128 - 5 - 1, 20,
				Text.of("Yes"),
				button -> {
					var config = GlobalConfig.INSTANCE;
					//Enable both settings, they can always disable automatic later? TBD
					config.set(GlobalConfig.CLOUD_SYNC_ENABLED, true);
					config.set(GlobalConfig.AUTOMATIC_CLOUD_SYNC, true);
					config.set(GlobalConfig.FIRST_TIME_LOAD, false);
					config.save();

					client.setScreen(new WardrobeBrowserScreen(null, client.player.getUuid()));
				}));


		this.addDrawableChild(new WildfireButton(x - 128 + 5, y + 75, 128 - 5 - 1, 20,
				Text.of("No"),
				button -> {
					var config = GlobalConfig.INSTANCE;
					config.set(GlobalConfig.CLOUD_SYNC_ENABLED, false);
					config.set(GlobalConfig.AUTOMATIC_CLOUD_SYNC, false);
					config.set(GlobalConfig.FIRST_TIME_LOAD, false);
					config.save();

					client.setScreen(new WardrobeBrowserScreen(null, client.player.getUuid()));
				}));

		super.init();
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);
		ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, (this.width - 256) / 2, (this.height - 200) / 2, 0, 0, 256, 200, 256, 256);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if(client == null || client.world == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		MatrixStack mStack = ctx.getMatrices();

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("Welcome to Wildfire's Female Gender Mod!").formatted(Formatting.UNDERLINE), x, y - 20, 0x000000);

		mStack.push();
		mStack.translate(x, y - 5, 0);
		mStack.scale(0.8f, 0.8f, 1);
		mStack.translate(-x, -y + 5, 0);
		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("Would you like to enable cloud server syncing for your gender settings? This feature allows other players to view your customized gender appearance, even if the server doesn't have the mod installed."), x, y - 5, (int) ((256-10) * 1.2f), 4210752);
		mStack.pop();


		mStack.push();
			mStack.translate(x, y + 47, 0);
			mStack.scale(0.6f, 0.6f, 1);
			mStack.translate(-x, -y - 47, 0);
		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("You can always change this setting later in the mod menu."), x, y + 77, (int) ((256-10) * 1.2f), 4210752);
		mStack.pop();


	}

	@Override
	public void close() {
		GlobalConfig.INSTANCE.save();
		super.close();
	}
}