package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.compat.contenttweaker.OrganItemContent;
import com.shiver.chestcavity.compat.contenttweaker.OrganItemRepresentation;
import com.shiver.chestcavity.compat.contenttweaker.OrganVanillaFactoryExpansion;
import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.Loader;

public final class CraftTweakerCompatBootstrap {

    private static boolean initialized;

    private CraftTweakerCompatBootstrap() {
    }

    public static void preInit() {
        if (initialized || !Loader.isModLoaded(ChestCavityCtConstants.CRAFT_TWEAKER_MOD_ID)) {
            return;
        }

        initialized = true;
        registerCommonClasses();

        if (Loader.isModLoaded(ChestCavityCtConstants.CONTENT_TWEAKER_MOD_ID)) {
            registerContentTweakerClasses();
        }

        ChestCavityLegacy.LOGGER.info("Registered Chest Cavity CraftTweaker compat classes.");
    }

    private static void registerCommonClasses() {
        CraftTweakerAPI.registerClass(ChestCavityZenApi.class);
        CraftTweakerAPI.registerClass(OrganDefinitionRepresentation.class);
        CraftTweakerAPI.registerClass(ScoreRepresentation.class);
        CraftTweakerAPI.registerClass(AbilityRepresentation.class);
        CraftTweakerAPI.registerClass(ChestCavityTypeRepresentation.class);

        CraftTweakerAPI.registerClass(ScoreCallbacks.OnScoreChanged.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnServerTick.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnClientTick.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnBreakSpeed.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnIncomingDamage.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnAttackTarget.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnJump.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnEat.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnPotionIncoming.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.OnActivate.class);
    }

    private static void registerContentTweakerClasses() {
        CraftTweakerAPI.registerClass(OrganVanillaFactoryExpansion.class);
        CraftTweakerAPI.registerClass(OrganItemRepresentation.class);
        CraftTweakerAPI.registerClass(OrganItemContent.class);
    }
}
