package com.wildfire.common.main.capabilities;

import com.wildfire.api.IGenderArmor;
import com.wildfire.common.main.WildfireHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {

    @CapabilityInject(IGenderArmor.class)
    public static Capability<IGenderArmor> GENDER_ARMOR_CAPABILITY = null;



    public static void registerCapabilities() {
        WildfireHelper.register();
    }
}
