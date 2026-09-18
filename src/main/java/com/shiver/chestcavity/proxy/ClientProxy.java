package com.shiver.chestcavity.proxy;

import com.shiver.chestcavity.client.CCKeyBindings;
import com.shiver.chestcavity.network.ClientNetworkHooks;
import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageMovementConfigSync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 模组客户端代理，处理客户端专属初始化（按键绑定等）及网络同步处理。
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
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

    @Override
    public void handleMovementConfigSync(MessageMovementConfigSync message) {
        ClientNetworkHooks.handleMovementConfigSync(message);
    }
}

