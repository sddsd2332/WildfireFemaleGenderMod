package com.wildfire.main;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * 通用 Forge 事件：只保留服务端安全逻辑，避免专服加载客户端类。
 */
public class WildfireCommonEvents {
    public WildfireCommonEvents() {
    }

    /**
     * 玩家登录后发送服务端已知的玩家性别配置。
     */
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            WildfireGender.syncTo((EntityPlayerMP) event.player);
        }
    }
}
