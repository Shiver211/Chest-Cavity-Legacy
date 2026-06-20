package com.shiver.chestcavity.proxy;

import com.shiver.chestcavity.client.CCKeyBindings;
import com.shiver.chestcavity.network.ClientNetworkHooks;
import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CCKeyBindings.register();
    }

    @Override
    public void handleChestCavitySync(MessageChestCavitySync message) {
        ClientNetworkHooks.handleChestCavitySync(message);
    }

    @Override
    public void handleOrganDataSync(MessageOrganDataSync message) {
        ClientNetworkHooks.handleOrganDataSync(message);
    }
}
