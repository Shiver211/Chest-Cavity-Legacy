package com.shiver.chestcavity.proxy;

import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageMovementConfigSync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 模组通用代理，提供服务端及通用的生命周期与网络消息处理实现。
 */
public class CommonProxy {

    /**
     * 预初始化阶段钩子。
     *
     * @param event 预初始化事件。
     */
    public void preInit(FMLPreInitializationEvent event) {
    }

    /**
     * 初始化阶段钩子。
     *
     * @param event 初始化事件。
     */
    public void init(FMLInitializationEvent event) {
    }

    /**
     * 齿轮初始化/后初始化阶段钩子。
     *
     * @param event 后初始化事件。
     */
    public void postInit(FMLPostInitializationEvent event) {
    }

    /**
     * 处理胸腔数据同步网络消息。服务端为空操作。
     *
     * @param message 胸腔数据同步消息。
     */
    public void handleChestCavitySync(MessageChestCavitySync message) {
    }

    /**
     * 处理器官数据同步网络消息。服务端为空操作。
     *
     * @param message 器官数据同步消息。
     */
    public void handleOrganDataSync(MessageOrganDataSync message) {
    }

    /**
     * 处理运动配置同步网络消息。服务端为空操作。
     *
     * @param message 运动配置同步消息。
     */
    public void handleMovementConfigSync(MessageMovementConfigSync message) {
    }
}

