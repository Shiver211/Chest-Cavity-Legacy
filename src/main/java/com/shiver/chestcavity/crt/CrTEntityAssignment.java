package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.chestcavity.EntityAssignment")
public final class CrTEntityAssignment {

    private CrTEntityAssignment() {
    }

    @ZenMethod
    public static void register(String entityId, String typeId) {
        ChestCavityApis.ENTITY_ASSIGNMENTS.register(CrTUtil.id(entityId), typeId);
    }

    @ZenMethod
    public static void unregister(String entityId) {
        ChestCavityApis.ENTITY_ASSIGNMENTS.unregister(CrTUtil.id(entityId));
    }

    @ZenMethod
    public static String getAssignedType(String entityId) {
        return ChestCavityApis.ENTITY_ASSIGNMENTS.getAssignedType(CrTUtil.id(entityId));
    }
}
