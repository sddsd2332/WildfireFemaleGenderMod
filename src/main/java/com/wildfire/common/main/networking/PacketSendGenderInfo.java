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

package com.wildfire.common.main.networking;

import com.wildfire.common.main.GenderPlayer;
import com.wildfire.common.main.WildfireGender;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.Supplier;

public class PacketSendGenderInfo extends PacketGenderInfo {

    public PacketSendGenderInfo(GenderPlayer plr) {
        super(plr);
    }

    public PacketSendGenderInfo(ByteBuf buffer) {
        super(buffer);
    }

    public static void handle(final PacketSendGenderInfo packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            EntityPlayerMP player = context.get().getSender();
            if (player == null || !player.getUUID(player.getGameProfile()).equals(packet.uuid)) {
                //Validate the uuid matches the player who sent it
                return;
            }
            GenderPlayer plr = WildfireGender.getOrAddPlayerById(packet.uuid);
            packet.updatePlayerFromPacket(plr);
            //WildfireGender.logger.debug("Received data from player {}", plr.uuid);
            //Sync changes to other online players that are tracking us
            WildfireSync.sendToOtherClients(player, plr);
        });

        context.get().setPacketHandled(true);
    }
}
