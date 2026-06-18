package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.integration.crafttweaker.callback.AbilityCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.callback.OrganCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.callback.ScoreCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.api.ChestCavity;
import com.shiver.chestcavity.integration.crafttweaker.api.VanillaFactoryExpansion;
import com.shiver.chestcavity.integration.crafttweaker.context.AttackContext;
import com.shiver.chestcavity.integration.crafttweaker.context.BreakSpeedContext;
import com.shiver.chestcavity.integration.crafttweaker.context.DamageContext;
import com.shiver.chestcavity.integration.crafttweaker.context.EatContext;
import com.shiver.chestcavity.integration.crafttweaker.context.JumpContext;
import com.shiver.chestcavity.integration.crafttweaker.context.OrganInsertContext;
import com.shiver.chestcavity.integration.crafttweaker.context.OrganRemoveContext;
import com.shiver.chestcavity.integration.crafttweaker.context.OrganTickContext;
import com.shiver.chestcavity.integration.crafttweaker.context.PotionContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreChangeContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreTickContext;
import com.shiver.chestcavity.integration.crafttweaker.context.SkillActivateContext;
import com.shiver.chestcavity.integration.crafttweaker.context.SkillUseContext;
import com.shiver.chestcavity.integration.crafttweaker.representation.AbilityRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ChestCavityTypeRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganDefinitionRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganItemContent;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganItemRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ScoreRepresentation;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.OnRegister;
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
        CraftTweakerAPI.registerClass(ScriptDataRuntime.class);
        CraftTweakerAPI.registerClass(AttackContext.class);
        CraftTweakerAPI.registerClass(BreakSpeedContext.class);
        CraftTweakerAPI.registerClass(DamageContext.class);
        CraftTweakerAPI.registerClass(EatContext.class);
        CraftTweakerAPI.registerClass(JumpContext.class);
        CraftTweakerAPI.registerClass(OrganInsertContext.class);
        CraftTweakerAPI.registerClass(OrganRemoveContext.class);
        CraftTweakerAPI.registerClass(OrganTickContext.class);
        CraftTweakerAPI.registerClass(PotionContext.class);
        CraftTweakerAPI.registerClass(ScoreChangeContext.class);
        CraftTweakerAPI.registerClass(ScoreTickContext.class);
        CraftTweakerAPI.registerClass(SkillActivateContext.class);
        CraftTweakerAPI.registerClass(SkillUseContext.class);

        CraftTweakerAPI.registerClass(ScoreCallbacks.OnBecameActiveContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnBecameInactiveContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnValueChangedContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnServerTickContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnClientTickContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnBreakSpeedContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnIncomingDamageContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnAttackTargetContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnJumpContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnEatContext.class);
        CraftTweakerAPI.registerClass(ScoreCallbacks.OnPotionIncomingContext.class);

        CraftTweakerAPI.registerClass(AbilityCallbacks.OnActivateContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.CanActivateContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.GetCooldownContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.GetCostContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.OnActivateServerContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.OnActivateClientContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.OnActiveTickContext.class);
        CraftTweakerAPI.registerClass(AbilityCallbacks.OnEndContext.class);

        CraftTweakerAPI.registerClass(OrganCallbacks.CanInsert.class);
        CraftTweakerAPI.registerClass(OrganCallbacks.CanRemove.class);
        CraftTweakerAPI.registerClass(OrganCallbacks.OnInserted.class);
        CraftTweakerAPI.registerClass(OrganCallbacks.OnRemoved.class);
        CraftTweakerAPI.registerClass(OrganCallbacks.OnTick.class);

        if (Loader.isModLoaded(CtConstants.CONTENT_TWEAKER_MOD_ID)) {
            CraftTweakerAPI.registerClass(VanillaFactoryExpansion.class);
            CraftTweakerAPI.registerClass(OrganItemRepresentation.class);
            CraftTweakerAPI.registerClass(OrganItemContent.class);
        }

        ChestCavityLegacy.LOGGER.info("Registered Chest Cavity CraftTweaker integration classes.");
    }

    @OnRegister
    public static void onReload() {
        ScoreRegistry.clear();
        AbilityRegistry.clear();
        OrganRegistry.clear();
        ChestCavityTypeRegistry.clear();
        EntityAssignmentRegistry.clear();
    }
}
