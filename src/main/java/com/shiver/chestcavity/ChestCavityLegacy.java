package com.shiver.chestcavity;

import com.shiver.chestcavity.capability.ChestCavityCapability;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.network.ChestCavityNetwork;


import com.shiver.chestcavity.proxy.CommonProxy;
import com.shiver.chestcavity.ui.ChestCavityGuiFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 模组主入口，负责初始化核心系统、配置和运行期数据。
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:modularui;required-after:crafttweaker;required-after:mixinbooter")
public class ChestCavityLegacy {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @SidedProxy(
            clientSide = "com.shiver.chestcavity.proxy.ClientProxy",
            serverSide = "com.shiver.chestcavity.proxy.ServerProxy"
    )
    public static CommonProxy PROXY = new CommonProxy();

    /**
     * 处理模组预初始化阶段，完成配置、能力、网络和界面工厂的注册。
     *
     * @param event Forge 预初始化事件。
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CCConfig.load(event.getSuggestedConfigurationFile());
        ChestCavityCapability.ensureRegistered();
        ChestCavityNetwork.register();
        ChestCavityGuiFactory.register();

        PROXY.preInit(event);
        LOGGER.info("{} core systems initialized.", Tags.MOD_NAME);
    }

    /**
     * Loads builtin chest-cavity data after items are registered.
     *
     * @param event Forge 初始化事件。
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DataLoaders.reload();
        PROXY.init(event);
        LOGGER.info("{} data loaded.", Tags.MOD_NAME);
    }
}
