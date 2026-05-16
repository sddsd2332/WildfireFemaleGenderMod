package com.wildfire.main.proxy;

import com.wildfire.main.SteinEventHandler;
import com.wildfire.main.networking.PacketSync;
import com.wildfire.render.GenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * 客户端代理：注册按键、客户端事件和玩家渲染层。
 */
public class GenderClient extends GenderServer {

    public static final KeyBinding toggleEditGUI = new KeyBinding("key.wildfire_gender.gender_menu", Keyboard.KEY_G, "category.wildfire_gender.generic");

    public GenderClient() {
    }

    public void playSound(SoundEvent evt, SoundCategory cat, float vol, float pitch, EntityPlayer ent) {
   //     Minecraft.getMinecraft().getSoundHandler().playSound(new TICKAB(evt, cat, vol, pitch, ent));
    }

    @Override
    /**
     * 注册小键盘 7 和 GenderLayer，让玩家模型渲染女性胸部层。
     */
    public void register() {
        ClientRegistry.registerKeyBinding(toggleEditGUI);
        MinecraftForge.EVENT_BUS.register(new SteinEventHandler());

        for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            renderer.addLayer(new GenderLayer(renderer));
        }
    }

    @Override
    public void handleSync(final PacketSync message) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().player != null && message.isFor(Minecraft.getMinecraft().player.getUniqueID())) {
                    return;
                }
                message.applyToClient();
            }
        });
    }
}
