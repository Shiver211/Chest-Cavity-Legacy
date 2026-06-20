package com.shiver.chestcavity.proxy;

import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void handleChestCavitySync(MessageChestCavitySync message) {
    }

    public void handleOrganDataSync(MessageOrganDataSync message) {
    }
}
