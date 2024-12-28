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

package com.wildfire.common.main;

import com.wildfire.api.IGenderArmor;
import com.wildfire.common.main.networking.WildfireSync;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.render.GenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.core.Holder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.util.Set;
import java.util.UUID;


public class WildfireEventHandler {


    public static final KeyBinding toggleEditGUI = new KeyBinding("key.wildfire_gender.gender_menu", Keyboard.KEY_G, "category.wildfire_gender.generic") {

        /*
        @Override
        public void setDown(boolean value) {
            if (value && !isDown()) {
                //When the key goes from not down to down try to open the wardrobe screen
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen == null && minecraft.player != null) {
                    minecraft.setScreen(new WardrobeBrowserScreen(minecraft.player.getUUID(minecraft.player.getGameProfile())));
                }
            }
            super.setDown(value);
        }

         */
    };


    public static void init() {
        ClientRegistry.registerKeyBinding(toggleEditGUI);
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = WildfireGender.MODID)
    private static class ClientModEventBusListeners {
        @SubscribeEvent
        public static void entityLayers(EntityRenderersEvent.AddLayers event) {
            for (String skinName : event.getSkins()) {
                LivingEntityRenderer<AbstractClientPlayer, PlayerMode<AbstractClientPlayer>> renderer = event.getSkin(skinName);
                if (renderer != null) {
                    //TODO - 1.20: Switch to get model manager from event https://github.com/MinecraftForge/MinecraftForge/pull/9562
                    renderer.addLayer(new GenderLayer(renderer, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()));
                }
            }
        }

        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(new WildfireEventHandler());
        }

        @SubscribeEvent
        public static void registerKeybindings(RegisterKeyMappingsEvent event) {
            event.register(toggleEditGUI);
        }
    }

    private int timer = 0;
    private int toastTick = 0;
    private boolean showedToast = false;

    @SubscribeEvent
    public void onGUI(TickEvent.ClientTickEvent evt) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (Minecraft.getMinecraft().world == null || player == null) {
            WildfireGender.CLOTHING_PLAYERS.clear();
            toastTick = 0;
            return;
        }/* else if (!showedToast && toastTick++ > 100) {
			 Minecraft.getInstance().getToasts().addToast(new Toast() {
				 @Nonnull
				 @Override
				 public Visibility render(@Nonnull GuiGraphics graphics, @Nonnull ToastComponent component, long timeSinceLastVisible) {
					 RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

					 graphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
					 Font font = component.getMinecraft().font;
					 graphics.drawString(font, Component.translatable("category.wildfire_gender.generic"), 0, 7, 0xFF000000, false);
					 graphics.drawString(font, Component.translatable("toast.wildfire_gender.get_started", toggleEditGUI.getTranslatedKeyMessage()), 0, 18, -1, false);

					 return Visibility.SHOW;
				 }
			 });
			 showedToast = true;
		 }*/
        boolean shouldSync = false;
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
        if (connection != null) {
            shouldSync = WildfireSync.NETWORK.isRemotePresent(connection.getConnection());
        }
        //20 ticks per second / 5 = 4 times per second
        if (shouldSync && timer++ % 5 == 0) {
            GenderPlayer aPlr = WildfireGender.getPlayerById(player.getUUID());
            WildfireSync.sendToServer(aPlr);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END && evt.side.isClient()) {
            GenderPlayer aPlr = WildfireGender.getPlayerById(evt.player.getUUID());
            if (aPlr == null) return;
            IGenderArmor armor = WildfireHelper.getArmorConfig(evt.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
            aPlr.getLeftBreastPhysics().update(evt.player, armor);
            aPlr.getRightBreastPhysics().update(evt.player, armor);
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent evt) {
        if (evt.getWorld().isRemote && evt.getEntity() instanceof AbstractClientPlayer plr) {
            UUID uuid = plr.getUUID(plr.getGameProfile());
            GenderPlayer aPlr = WildfireGender.getPlayerById(uuid);
            if (aPlr == null) {
                aPlr = new GenderPlayer(uuid);
                WildfireGender.CLOTHING_PLAYERS.put(uuid, aPlr);
                //Mark the player as needing sync if it is the client's own player
                EntityPlayer player = Minecraft.getMinecraft().player;
                ;
                WildfireGender.loadGenderInfoAsync(uuid, player != null && uuid.equals(player.getUUID(player.getGameProfile())));
            }
        }
    }

    //TODO: Eventually we may want to replace this with a map or something and replace things like drowning sounds with other drowning sounds
    private final Set<SoundEvent> playerHurtSounds = Set.of(
            SoundEvent.PLAYER_HURT,
            SoundEvent.PLAYER_HURT_DROWN,
            SoundEvent.PLAYER_HURT_FREEZE,
            SoundEvent.PLAYER_HURT_ON_FIRE,
            SoundEvent.PLAYER_HURT_SWEET_BERRY_BUSH
    );

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if (GeneralClientConfig.INSTANCE.disableSoundReplacement.get()) {
            return;
        }
        Holder<SoundEvent> soundHolder = event.getSound();
        if (soundHolder != null) {
            SoundEvent soundEvent = soundHolder.get();
            if (playerHurtSounds.contains(soundEvent) && event.getEntity() instanceof EntityPlayer p && p.getEntityWorld().isRemote) {
                //Cancel as we handle all hurt sounds manually so that we can
                event.setCanceled(true);
                if (p.hurtTime == p.maxHurtTime && p.hurtTime > 0) {
                    //Note: We check hurtTime == hurtDuration and hurtTime > 0 or otherwise when the server sends a hurt sound to the client
                    // and the client will check itself instead of the player who was damaged.
                    GenderPlayer plr = WildfireGender.getPlayerById(p.getUUID(p.getGameProfile()));
                    if (plr != null && plr.hasHurtSounds()) {
                        SoundEvent soundOverride = plr.getGender().getHurtSound();
                        if (soundOverride != null) {
                            //If the player who produced the hurt sound is a female sound replace it
                            soundEvent = soundOverride;
                        }
                    }
                } else {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    if (player != null && p.getUUID(p.getGameProfile()).equals(player.getUUID(p.getGameProfile()))) {
                        //Skip playing remote hurt sounds. Note: sounds played via /playsound will not be intercepted
                        // as they are played directly
                        //Note: This might behave slightly strangely if a mod is manually firing a player damage sound
                        // only on the server and not also on the client
                        //TODO: Ideally we would fix that edge case but I find it highly unlikely it will ever actually occur
                        return;
                    }
                }
                p.getEntityWorld().playSound(p.posX, p.posY, p.posZ, soundEvent, event.getCategory(), event.getVolume(), event.getPitch(), false);
            }
        }
    }
}
