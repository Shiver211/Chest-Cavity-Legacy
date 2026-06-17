package com.shiver.chestcavity;

import com.shiver.chestcavity.capability.ChestCavityCapability;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.registry.CCRecipes;
import com.shiver.chestcavity.ui.ChestCavityGuiFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:modularui")
public class ChestCavityLegacy {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private File gameDir;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        gameDir = event.getModConfigurationDirectory().getParentFile();
        CCConfig.load(event.getSuggestedConfigurationFile());
        ChestCavityCapability.ensureRegistered();
        ChestCavityNetwork.register();
        ChestCavityGuiFactory.register();
        CCRecipes.registerFactories();

        if (event.getSide().isClient()) {
            registerClientKeyBindings();
        }
        LOGGER.info("{} core systems initialized.", Tags.MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DataLoaders.reload(gameDir);
        LOGGER.info("{} data loaded.", Tags.MOD_NAME);
    }

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
