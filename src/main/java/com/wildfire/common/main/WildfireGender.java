package com.wildfire.common.main;

import com.wildfire.common.main.networking.WildfireSync;
import com.wildfire.main.wildfire_gender.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(modid = WildfireGender.MODID, acceptedMinecraftVersions = "[1.12,1.13)", version = WildfireGender.VERSION)
@Mod.EventBusSubscriber()
public class WildfireGender {

    public static final String VERSION = Tags.VERSION;
    public static final String MODID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;
    public static final String LOG_TAG = '[' + MOD_NAME + ']';

    public static Logger logger = LogManager.getLogger(MOD_NAME);

    public static Map<UUID, GenderPlayer> CLOTHING_PLAYERS = new HashMap<>();

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Nullable
    public static GenderPlayer getPlayerById(UUID id) {
        return CLOTHING_PLAYERS.get(id);
    }

    public static GenderPlayer getOrAddPlayerById(UUID id) {
        return CLOTHING_PLAYERS.computeIfAbsent(id, GenderPlayer::new);
    }

    private void onStartTracking(PlayerEvent.StartTracking evt) {
        if (evt.getTarget() instanceof EntityPlayer toSync && evt.getEntity() instanceof EntityPlayerMP sendTo) {
            GenderPlayer genderToSync = WildfireGender.getPlayerById(toSync.getUUID(toSync.getGameProfile()));
            if (genderToSync == null) return;
            // Note that we intentionally don't check if we've previously synced a player with this code path;
            // because we use entity tracking to sync, it's entirely possible that one player would leave the
            // tracking distance of another, change their settings, and then re-enter their tracking distance;
            // we wouldn't sync while they're out of tracking distance, and as such, their settings would be out
            // of sync until they relog.
            WildfireSync.sendToClient(sendTo, genderToSync);
        }
    }

    public static void loadGenderInfoAsync(UUID uuid, boolean markForSync) {
        Thread thread = new Thread(() -> WildfireGender.loadGenderInfo(uuid, markForSync));
        thread.setName("WFGM_GetPlayer-" + uuid);
        thread.start();
    }

    public static GenderPlayer loadGenderInfo(UUID uuid, boolean markForSync) {
        return GenderPlayer.loadCachedPlayer(uuid, markForSync);
    }

}
