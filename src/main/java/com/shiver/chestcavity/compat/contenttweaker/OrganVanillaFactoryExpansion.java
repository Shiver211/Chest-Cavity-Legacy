package com.shiver.chestcavity.compat.contenttweaker;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethodStatic;

import com.shiver.chestcavity.compat.crafttweaker.ChestCavityCtConstants;

@ModOnly(ChestCavityCtConstants.CONTENT_TWEAKER_MOD_ID)
@ZenExpansion(ChestCavityCtConstants.CONTENT_TWEAKER_FACTORY)
@ZenRegister
public final class OrganVanillaFactoryExpansion {

    private OrganVanillaFactoryExpansion() {
    }

    @ZenMethodStatic
    public static OrganItemRepresentation createOrganItem(String id) {
        return new OrganItemRepresentation(id);
    }
}
