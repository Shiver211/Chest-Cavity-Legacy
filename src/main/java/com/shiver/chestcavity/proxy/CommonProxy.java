package com.shiver.chestcavity.proxy;

import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageMovementConfigSync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void handleChestCavitySync(MessageChestCavitySync message) {
    }

    public void handleOrganDataSync(MessageOrganDataSync message) {
    }

    public void handleMovementConfigSync(MessageMovementConfigSync message) {
    }
}

