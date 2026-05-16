package com.wildfire.main;

import com.wildfire.api.IGenderArmor;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.main.proxy.GenderClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 客户端事件处理：按键打开 GUI、玩家加入缓存、每 tick 更新胸部物理。
 */
public class SteinEventHandler {
    private static final int CLIENT_SYNC_INTERVAL = 5;
    private static final Set<SoundEvent> PLAYER_HURT_SOUNDS = new HashSet<>(Arrays.asList(
            SoundEvents.ENTITY_PLAYER_HURT,
            SoundEvents.ENTITY_PLAYER_HURT_DROWN,
            SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE
    ));
    private int clientSyncTimer;

    @SubscribeEvent
    /**
     * 客户端 tick：离开世界时清空缓存，并定期向服务端发送自己的性别配置。
     */
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) {
            WildfireGender.CLOTHING_PLAYERS.clear();
            WildfireSync.resetClient();
            this.clientSyncTimer = 0;
            return;
        }
        if (minecraft.player == null) {
            this.clientSyncTimer = 0;
            return;
        }
        WildfireSync.flushClient();
        if (WildfireSync.isServerRemotePresent() && this.clientSyncTimer++ % CLIENT_SYNC_INTERVAL == 0) {
            GenderPlayer player = WildfireGender.getPlayerById(minecraft.player.getUniqueID());
            WildfireSync.sendToServer(player, false);
        }
    }

    @SubscribeEvent
    /**
     * 玩家 tick：按高版本逻辑只在物理开启时更新弹跳，否则衰减归零。
     */
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            GenderPlayer player = WildfireGender.getOrAddPlayerById(event.player.getUniqueID());
            IGenderArmor armor = WildfireHelper.getArmorConfig(event.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
            if (player.hasBreastPhysics()) {
                player.getLeftBreastPhysics().update(event.player, armor);
                if (player.getBreasts().isUniboob()) {
                    player.getRightBreastPhysics().settle();
                } else {
                    player.getRightBreastPhysics().update(event.player, armor);
                }
            } else {
                player.getLeftBreastPhysics().settle(armor);
                player.getRightBreastPhysics().settle(armor);
            }
        }
    }

    @SubscribeEvent
    /**
     * 按 G 打开当前玩家的衣柜界面。
     */
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (GenderClient.toggleEditGUI.isPressed() && WildfireGender.modEnabled) {
            refreshAllGenders();
            Minecraft.getMinecraft().displayGuiScreen(new WardrobeBrowserScreen(null, Minecraft.getMinecraft().player.getUniqueID()));
        }
    }

    private void refreshAllGenders() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.getConnection() == null) {
            return;
        }
        minecraft.getConnection().getPlayerInfoMap().forEach(info -> WildfireGender.getOrAddPlayerById(info.getGameProfile().getId()));
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && event.getEntity() instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
            boolean markForSync = Minecraft.getMinecraft().player != null && player.getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID());
            if (WildfireGender.getPlayerById(player.getUniqueID()) == null) {
                WildfireGender.CLOTHING_PLAYERS.put(player.getUniqueID(), new GenderPlayer(player.getUniqueID().toString()));
                WildfireGender.loadGenderInfoAsync(player.getUniqueID().toString(), markForSync);
            } else if (markForSync) {
                WildfireGender.getOrAddPlayerById(player.getUniqueID()).needsSync = true;
            }
        }
    }

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
        if (GeneralClientConfig.INSTANCE.disableSoundReplacement) {
            return;
        }
        if (!(event.getEntity() instanceof EntityPlayer) || !PLAYER_HURT_SOUNDS.contains(event.getSound())) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntity();
        if (!player.world.isRemote) {
            return;
        }
        event.setCanceled(true);
        SoundEvent sound = event.getSound();
        if (player.hurtTime == player.maxHurtTime && player.hurtTime > 0) {
            GenderPlayer genderPlayer = WildfireGender.getOrAddPlayerById(player.getUniqueID());
            if (genderPlayer.hasGenderHurtSound() && genderPlayer.hasHurtSounds()) {
                sound = WildfireSounds.FEMALE_HURT;
            }
        } else if (Minecraft.getMinecraft().player != null && player.getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID())) {
            return;
        }
        player.world.playSound(player.posX, player.posY, player.posZ, sound, event.getCategory(), event.getVolume(), event.getPitch(), false);
    }
}
