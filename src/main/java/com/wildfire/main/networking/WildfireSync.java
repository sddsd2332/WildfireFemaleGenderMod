package com.wildfire.main.networking;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.12.2 的同步门控与异步序列化实现。
 *
 * <p>高版本依赖 SimpleChannel#isRemotePresent 判断对端是否安装本 mod；1.12 没有同款 API，
 * 这里通过 REGISTER/UNREGISTER 自定义通道事件维护 remote-present 状态。工作线程只处理已经
 * 从主线程复制出来的不可变字段快照，Minecraft 对象访问与实际发包仍固定在主线程。</p>
 */
public class WildfireSync {
    private static final Set<UUID> REMOTE_PRESENT_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private static final Map<UUID, String> LAST_CLIENT_PAYLOADS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Runnable> CLIENT_SEND_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<Runnable> SERVER_SEND_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ExecutorService SERIALIZER = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "WildfireGender-Sync-" + this.count.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });

    private static volatile boolean serverRemotePresent;
    private static volatile boolean registered;

    private WildfireSync() {
    }

    /**
     * 注册同步事件。Forge 1.12 的 FML 网络事件、tick 事件和普通 Forge 事件最终都在该总线上投递。
     */
    public static void register() {
        if (!registered) {
            registered = true;
            FMLCommonHandler.instance().bus().register(new WildfireSync());
        }
    }

    /**
     * 客户端判断当前服务器是否注册了本 mod 的自定义通道。
     */
    public static boolean isServerRemotePresent() {
        return serverRemotePresent;
    }

    /**
     * 服务端判断指定玩家连接是否注册了本 mod 的自定义通道。
     *
     * @param player 要检测的玩家。
     * @return true 表示该玩家客户端安装并注册了本 mod 网络通道。
     */
    public static boolean isRemotePresent(EntityPlayerMP player) {
        return player != null && REMOTE_PRESENT_PLAYERS.contains(player.getUniqueID());
    }

    /**
     * 客户端把自己的配置快照加入异步序列化队列。
     *
     * @param player 主线程复制出的本地玩家配置。
     * @param force  true 时即使快照与上次相同也发送。
     */
    public static void sendToServer(GenderPlayer player, boolean force) {
        final PacketGenderInfo.Snapshot snapshot = PacketGenderInfo.Snapshot.copy(player);
        if (snapshot == null || (!force && !player.needsSync)) {
            return;
        }
        SERIALIZER.execute(new Runnable() {
            @Override
            public void run() {
                final String key = snapshot.key();
                if (!force && key.equals(LAST_CLIENT_PAYLOADS.get(snapshot.uuid))) {
                    return;
                }
                LAST_CLIENT_PAYLOADS.put(snapshot.uuid, key);
                CLIENT_SEND_QUEUE.add(new Runnable() {
                    @Override
                    public void run() {
                        WildfireGender.NETWORK.sendToServer(new PacketSendGenderInfo(snapshot));
                    }
                });
            }
        });
        player.needsSync = false;
    }

    /**
     * 服务端把某个玩家配置发给正在追踪他的客户端。
     *
     * @param toSync       被同步的服务端玩家实体。
     * @param genderPlayer 被同步玩家的配置。
     */
    public static void sendToOtherClients(final EntityPlayerMP toSync, GenderPlayer genderPlayer) {
        final PacketGenderInfo.Snapshot snapshot = PacketGenderInfo.Snapshot.copy(genderPlayer);
        if (toSync == null || snapshot == null || toSync.hasDisconnected() || toSync instanceof FakePlayer) {
            return;
        }
        SERIALIZER.execute(new Runnable() {
            @Override
            public void run() {
                SERVER_SEND_QUEUE.add(new Runnable() {
                    @Override
                    public void run() {
                        sendSnapshotToTrackingPlayers(toSync, snapshot);
                    }
                });
            }
        });
    }

    /**
     * 服务端把某个玩家配置发给指定客户端。
     *
     * @param sendTo 接收同步的玩家。
     * @param toSync 要同步的配置。
     */
    public static void sendToClient(final EntityPlayerMP sendTo, GenderPlayer toSync) {
        if (!canSendTo(sendTo)) {
            return;
        }
        final PacketGenderInfo.Snapshot snapshot = PacketGenderInfo.Snapshot.copy(toSync);
        if (snapshot == null) {
            return;
        }
        SERIALIZER.execute(new Runnable() {
            @Override
            public void run() {
                SERVER_SEND_QUEUE.add(new Runnable() {
                    @Override
                    public void run() {
                        if (canSendTo(sendTo)) {
                            WildfireGender.NETWORK.sendTo(new PacketSync(snapshot), sendTo);
                        }
                    }
                });
            }
        });
    }

    /**
     * 服务端向指定玩家发送当前已知玩家配置的全量快照。
     *
     * @param sendTo 接收同步的玩家。
     * @param players 当前服务端缓存配置。
     */
    public static void sendFullSyncToClient(final EntityPlayerMP sendTo, Collection<GenderPlayer> players) {
        if (!canSendTo(sendTo)) {
            return;
        }
        final ArrayList<PacketGenderInfo.Snapshot> snapshots = new ArrayList<>();
        for (GenderPlayer player : players) {
            PacketGenderInfo.Snapshot snapshot = PacketGenderInfo.Snapshot.copy(player);
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }
        SERIALIZER.execute(new Runnable() {
            @Override
            public void run() {
                SERVER_SEND_QUEUE.add(new Runnable() {
                    @Override
                    public void run() {
                        if (canSendTo(sendTo)) {
                            for (PacketGenderInfo.Snapshot snapshot : snapshots) {
                                WildfireGender.NETWORK.sendTo(new PacketSync(snapshot), sendTo);
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 主线程执行客户端待发送任务。
     */
    public static void flushClient() {
        flushQueue(CLIENT_SEND_QUEUE);
    }

    /**
     * 主线程执行服务端待发送任务。
     */
    public static void flushServer() {
        flushQueue(SERVER_SEND_QUEUE);
    }

    /**
     * 客户端退出世界时清理同步状态。
     */
    public static void resetClient() {
        serverRemotePresent = false;
        LAST_CLIENT_PAYLOADS.clear();
        CLIENT_SEND_QUEUE.clear();
    }

    private static void flushQueue(ConcurrentLinkedQueue<Runnable> queue) {
        Runnable task;
        while ((task = queue.poll()) != null) {
            task.run();
        }
    }

    private static void sendSnapshotToTrackingPlayers(EntityPlayerMP toSync, PacketGenderInfo.Snapshot snapshot) {
        if (toSync.world instanceof WorldServer) {
            WorldServer world = (WorldServer) toSync.world;
            for (EntityPlayer player : world.getEntityTracker().getTrackingPlayers(toSync)) {
                if (player instanceof EntityPlayerMP && canSendTo((EntityPlayerMP) player)) {
                    WildfireGender.NETWORK.sendTo(new PacketSync(snapshot), (EntityPlayerMP) player);
                }
            }
        }
    }

    private static boolean canSendTo(EntityPlayerMP player) {
        return player != null && !player.hasDisconnected() && !(player instanceof FakePlayer) && isRemotePresent(player);
    }

    private static EntityPlayerMP playerFromEvent(FMLNetworkEvent<?> event) {
        INetHandler handler = event.getHandler();
        if (handler instanceof NetHandlerPlayServer) {
            return ((NetHandlerPlayServer) handler).player;
        }
        return null;
    }

    private static void scheduleServerTask(EntityPlayerMP player, Runnable task) {
        if (player == null || task == null || player.hasDisconnected()) {
            return;
        }
        IThreadListener thread = player.getServerWorld();
        if (thread.isCallingFromMinecraftThread()) {
            task.run();
        } else {
            thread.addScheduledTask(task);
        }
    }

    @SubscribeEvent
    public void onCustomPacketRegistration(FMLNetworkEvent.CustomPacketRegistrationEvent<?> event) {
        boolean present = event.getRegistrations().contains(WildfireGender.MODID);
        if (!present) {
            return;
        }
        boolean registered = "REGISTER".equals(event.getOperation());
        if (event.getSide() == Side.CLIENT) {
            serverRemotePresent = registered;
            if (!registered) {
                LAST_CLIENT_PAYLOADS.clear();
                CLIENT_SEND_QUEUE.clear();
            }
            return;
        }

        EntityPlayerMP player = playerFromEvent(event);
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueID();
        if (registered) {
            REMOTE_PRESENT_PLAYERS.add(uuid);
            scheduleServerTask(player, new Runnable() {
                @Override
                public void run() {
                    WildfireGender.syncTo(player);
                }
            });
        } else {
            REMOTE_PRESENT_PLAYERS.remove(uuid);
        }
    }

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        serverRemotePresent = event.isLocal();
        LAST_CLIENT_PAYLOADS.clear();
        CLIENT_SEND_QUEUE.clear();
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        resetClient();
    }

    @SubscribeEvent
    public void onServerConnected(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        EntityPlayerMP player = playerFromEvent(event);
        if (player != null && event.isLocal()) {
            REMOTE_PRESENT_PLAYERS.add(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onServerDisconnected(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        EntityPlayerMP player = playerFromEvent(event);
        if (player != null) {
            REMOTE_PRESENT_PLAYERS.remove(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            flushServer();
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayerMP) || !(event.getTarget() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP sendTo = (EntityPlayerMP) event.getEntityPlayer();
        Entity target = event.getTarget();
        GenderPlayer genderToSync = WildfireGender.SERVER_PLAYERS.get(target.getUniqueID());
        if (genderToSync != null) {
            sendToClient(sendTo, genderToSync);
        }
    }

}
