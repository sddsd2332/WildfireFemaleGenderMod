package com.wildfire.main;

import com.wildfire.main.networking.PacketSendGenderInfo;
import com.wildfire.main.networking.PacketSync;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.main.proxy.GenderServer;
import com.wildfire.api.GenderArmorCapability;
import com.wildfire.main.wildfire_gender.Tags;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mod(modid = Tags.MOD_ID, acceptedMinecraftVersions = "[1.12,1.13)", version = Tags.VERSION, name = "Wildfire's Female Gender Mod")
/**
 * FemaleGenderMod 的 1.12.2 Forge 主入口：注册代理、网络通道，并维护客户端/服务端的性别玩家缓存。
 */
public class WildfireGender {
    public static final String VERSION = Tags.VERSION;
    public static final String MODID = Tags.MOD_ID;
    public static boolean modEnabled = true;

    public static final Map<UUID, GenderPlayer> CLOTHING_PLAYERS = new LinkedHashMap<>();
    public static final Map<UUID, GenderPlayer> SERVER_PLAYERS = new LinkedHashMap<>();
    public static SimpleNetworkWrapper NETWORK;

    @SidedProxy(clientSide = "com.wildfire.main.proxy.GenderClient", serverSide = "com.wildfire.main.proxy.GenderServer")
    public static GenderServer proxy;

    @Mod.EventHandler
    /**
     * 预初始化：迁移旧配置目录并注册 1.12 的 SimpleNetworkWrapper 消息。
     */
    public void preInit(FMLPreInitializationEvent event) {
        File oldFolder = new File(System.getProperty("user.dir"), "config/KittGender");
        File newFolder = new File(System.getProperty("user.dir"), "config/WildfireGender");
        if (oldFolder.exists() && !newFolder.exists()) {
            oldFolder.renameTo(newFolder);
        }

        GenderArmorCapability.register();
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORK.registerMessage(PacketSync.Handler.class, PacketSync.class, 0, Side.CLIENT);
        NETWORK.registerMessage(PacketSendGenderInfo.Handler.class, PacketSendGenderInfo.class, 1, Side.SERVER);
    }

    @Mod.EventHandler
    /**
     * 初始化：注册客户端/服务端代理与通用事件。
     */
    public void init(FMLInitializationEvent event) {
        proxy.register();
        WildfireSync.register();
        MinecraftForge.EVENT_BUS.register(new WildfireCommonEvents());
    }

    @Nullable
    public static GenderPlayer getPlayerById(UUID id) {
        return CLOTHING_PLAYERS.get(id);
    }

    @Nullable
    public static GenderPlayer getPlayerByName(String uuid) {
        try {
            return getPlayerById(UUID.fromString(uuid));
        } catch (IllegalArgumentException ignored) {
            for (GenderPlayer player : CLOTHING_PLAYERS.values()) {
                if (player.username.equalsIgnoreCase(uuid)) {
                    return player;
                }
            }
            return null;
        }
    }

    public static GenderPlayer getOrAddPlayerById(UUID id) {
        GenderPlayer player = CLOTHING_PLAYERS.get(id);
        if (player == null) {
            player = loadGenderInfo(id.toString(), false);
            CLOTHING_PLAYERS.put(id, player);
        }
        return player;
    }

    public static void putPlayer(GenderPlayer player) {
        try {
            putPlayer(player, false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void putPlayer(GenderPlayer player, boolean fromNetwork) {
        try {
            UUID id = UUID.fromString(player.username);
            GenderPlayer current = CLOTHING_PLAYERS.get(id);
            if (fromNetwork && current != null && current.needsSync && System.currentTimeMillis() - current.lastLocalSaveTime < 10000L) {
                return;
            }
            CLOTHING_PLAYERS.put(id, player);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void loadGenderInfoAsync(final String uuid) {
        loadGenderInfoAsync(uuid, false);
    }

    /**
     * 异步加载本地玩家配置；markForSync 用于首进世界后主动上报服务器。
     */
    public static void loadGenderInfoAsync(final String uuid, final boolean markForSync) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                GenderPlayer player = loadGenderInfo(uuid, markForSync);
                putPlayer(player);
            }
        }, "WFGM_GetPlayer-" + uuid);
        thread.setDaemon(true);
        thread.start();
    }

    public static GenderPlayer loadGenderInfo(String uuid) {
        return loadGenderInfo(uuid, false);
    }

    public static GenderPlayer loadGenderInfo(String uuid, boolean markForSync) {
        GenderPlayer player = GenderPlayer.loadCachedPlayer(uuid);
        if (player != null && markForSync) {
            player.needsSync = true;
            player.lastLocalSaveTime = System.currentTimeMillis();
        }
        return player;
    }

    public static void syncTo(EntityPlayerMP target) {
        WildfireSync.sendFullSyncToClient(target, SERVER_PLAYERS.values());
    }

    /**
     * 创建本 mod 命名空间下的资源路径，等价于高版本 WildfireGender#rl。
     */
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }
}
