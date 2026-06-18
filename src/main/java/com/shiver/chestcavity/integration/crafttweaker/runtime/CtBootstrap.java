package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.integration.crafttweaker.callback.AbilityCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.callback.ScoreCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.api.ChestCavity;
import com.shiver.chestcavity.integration.crafttweaker.api.VanillaFactoryExpansion;
import com.shiver.chestcavity.integration.crafttweaker.representation.AbilityRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ChestCavityTypeRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganDefinitionRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganItemContent;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganItemRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ScoreRepresentation;
import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.Loader;

public final class CtBootstrap {

    private static boolean initialized;

    private CtBootstrap() {
    }

    public static void preInit() {
        if (initialized || !Loader.isModLoaded(CtConstants.CRAFT_TWEAKER_MOD_ID)) {
            return;
        }
        initialized = true;

        CraftTweakerAPI.registerClass(ChestCavity.class);
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

        if (Loader.isModLoaded(CtConstants.CONTENT_TWEAKER_MOD_ID)) {
            CraftTweakerAPI.registerClass(VanillaFactoryExpansion.class);
            CraftTweakerAPI.registerClass(OrganItemRepresentation.class);
            CraftTweakerAPI.registerClass(OrganItemContent.class);
        }

        ChestCavityLegacy.LOGGER.info("Registered Chest Cavity CraftTweaker integration classes.");
    }
}
