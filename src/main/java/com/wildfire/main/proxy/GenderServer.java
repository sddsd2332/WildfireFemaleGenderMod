package com.wildfire.main.proxy;

import com.wildfire.main.networking.PacketSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * 服务端代理基类；客户端代理继承它以共享初始化入口。
 */
public class GenderServer {

    public GenderServer() {
    }

    public void playSound(SoundEvent evt, SoundCategory cat, float vol, float pitch, EntityPlayer ent) {
    }

    public void register() {
    }

    public void handleSync(PacketSync message) {
    }

}
