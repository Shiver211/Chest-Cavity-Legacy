package com.shiver.chestcavity;

import com.shiver.chestcavity.capability.ChestCavityCapability;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.network.ChestCavityNetwork;


import com.shiver.chestcavity.ui.ChestCavityGuiFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * 模组主入口，负责初始化核心系统、配置和运行期数据。
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:modularui;required-after:crafttweaker")
public class ChestCavityLegacy {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private File gameDir;

    /**
     * 处理模组预初始化阶段，完成配置、能力、网络和界面工厂的注册。
     *
     * @param event Forge 预初始化事件。
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        gameDir = event.getModConfigurationDirectory().getParentFile();
        CCConfig.load(event.getSuggestedConfigurationFile());
        ChestCavityCapability.ensureRegistered();
        ChestCavityNetwork.register();
        ChestCavityGuiFactory.register();


        if (event.getSide().isClient()) {
            registerClientKeyBindings();
        }
        LOGGER.info("{} core systems initialized.", Tags.MOD_NAME);
    }

    /**
     * 处理模组初始化阶段，按当前游戏目录重新加载胸腔相关数据。
     *
     * @param event Forge 初始化事件。
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DataLoaders.reload(gameDir);
        LOGGER.info("{} data loaded.", Tags.MOD_NAME);
    }

    /**
     * 仅在客户端环境下通过反射注册按键绑定，避免服务端加载客户端类。
     */
    private static void registerClientKeyBindings() {
        try {
            Class.forName("com.shiver.chestcavity.client.CCKeyBindings")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to register Chest Cavity keybindings.", e);
        }
    }

}
