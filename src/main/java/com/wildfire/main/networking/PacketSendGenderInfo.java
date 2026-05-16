package com.wildfire.main.networking;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 客户端到服务端的本玩家性别配置上报包。
 */
public class PacketSendGenderInfo extends PacketGenderInfo {
    public PacketSendGenderInfo() {
    }

    public PacketSendGenderInfo(GenderPlayer player) {
        super(player);
    }

    public PacketSendGenderInfo(Snapshot snapshot) {
        super(snapshot);
    }

    public static void send(GenderPlayer player) {
        WildfireSync.sendToServer(player, true);
    }

    public static class Handler implements IMessageHandler<PacketSendGenderInfo, IMessage> {
        @Override
        public IMessage onMessage(final PacketSendGenderInfo message, final MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayerMP sender = ctx.getServerHandler().player;
                    if (sender == null || !sender.getUniqueID().equals(message.uuid)) {
                        return;
                    }
                    GenderPlayer player = WildfireGender.SERVER_PLAYERS.get(message.uuid);
                    if (player == null) {
                        player = new GenderPlayer(message.uuid.toString());
                    }
                    message.updatePlayerFromPacket(player);
                    WildfireGender.SERVER_PLAYERS.put(message.uuid, player);
                    WildfireSync.sendToOtherClients(sender, player);
                }
            });
            return null;
        }
    }
}
