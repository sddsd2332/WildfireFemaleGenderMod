package com.wildfire.main;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = WildfireGender.MODID)
/**
 * 模组声音注册：女性受伤音效等 SoundEvent。
 */
public class WildfireSounds {
    public static final SoundEvent FEMALE_HURT = create("female_hurt");

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(FEMALE_HURT);
    }

    private static SoundEvent create(String name) {
        ResourceLocation location = WildfireGender.rl(name);
        return new SoundEvent(location).setRegistryName(location);
    }
}
