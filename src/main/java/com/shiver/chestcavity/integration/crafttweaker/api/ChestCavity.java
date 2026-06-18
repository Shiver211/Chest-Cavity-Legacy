package com.shiver.chestcavity.integration.crafttweaker.api;

import com.shiver.chestcavity.integration.crafttweaker.representation.AbilityRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ChestCavityTypeRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.OrganDefinitionRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.representation.ScoreRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
import com.shiver.chestcavity.integration.crafttweaker.runtime.EntityAssignmentRegistry;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethodStatic;

@ZenClass(CtConstants.CT_NAMESPACE + "ChestCavity")
@ZenRegister
public final class ChestCavity {

    private ChestCavity() {
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
        EntityAssignmentRegistry.assign(
                CtUtil.requireId(entityId, "entity"),
                CtUtil.requireId(typeId, "type"));
    }
}
