package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.script.registry.ScriptEntityAssignmentRegistry;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethodStatic;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "ChestCavity")
@ZenRegister
public final class ChestCavityZenApi {

    private ChestCavityZenApi() {
    }

    @ZenMethodStatic
    public static OrganDefinitionRepresentation defineOrgan(IItemStack stack) {
        return new OrganDefinitionRepresentation(stack);
    }

    @ZenMethodStatic
    public static ScoreRepresentation createScore(String id) {
        return new ScoreRepresentation(id);
    }

    @ZenMethodStatic
    public static AbilityRepresentation createAbility(String id) {
        return new AbilityRepresentation(id);
    }

    @ZenMethodStatic
    public static ChestCavityTypeRepresentation createChestCavityType(String id) {
        return new ChestCavityTypeRepresentation(id);
    }

    @ZenMethodStatic
    public static void assignEntity(String entityId, String typeId) {
        ScriptEntityAssignmentRegistry.assign(
                CtCompatUtil.requireId(entityId, "entity"),
                CtCompatUtil.requireId(typeId, "type"));
    }
}
