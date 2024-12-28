package com.wildfire.client.keybinding;

import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.common.main.WildfireGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings extends KeyHandler {


    public static final String keybindCategory = WildfireGender.MOD_NAME;

    public static final KeyBinding toggleEditGUI = new KeyBinding("key.wildfire_gender.gender_menu", Keyboard.KEY_G, keybindCategory);

    private static Builder BINDINGS = new Builder().addBinding(toggleEditGUI, false);

    public KeyBindings() {
        super(BINDINGS);
        ClientRegistry.registerKeyBinding(toggleEditGUI);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(InputEvent event) {
        keyTick();
    }


    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (kb == toggleEditGUI) {
            if (minecraft.currentScreen == null && minecraft.player != null) {
                minecraft.displayGuiScreen(new WardrobeBrowserScreen(minecraft.player.getUUID(minecraft.player.getGameProfile())));
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {

    }
}
