package com.wildfire.main.networking;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

/**
 * 服务端到客户端的单个玩家性别同步包。
 */
public class PacketSync extends PacketGenderInfo {
    public PacketSync() {
    }

    public PacketSync(GenderPlayer player) {
        super(player);
    }

    public PacketSync(Snapshot snapshot) {
        super(snapshot);
    }

    public void applyToClient() {
        GenderPlayer player = WildfireGender.getOrAddPlayerById(this.uuid);
        updatePlayerFromPacket(player);
        player.syncStatus = GenderPlayer.SyncStatus.SYNCED;
        WildfireGender.putPlayer(player, true);
    }

    public boolean isFor(UUID playerId) {
        return this.uuid.equals(playerId);
    }

    public static class Handler implements IMessageHandler<PacketSync, IMessage> {
        @Override
        public IMessage onMessage(PacketSync message, MessageContext ctx) {
            WildfireGender.proxy.handleSync(message);
            return null;
        }
    }
}
