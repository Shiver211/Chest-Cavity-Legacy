package com.shiver.chestcavity.integration.crafttweaker.api;

import com.shiver.chestcavity.integration.crafttweaker.representation.OrganItemRepresentation;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethodStatic;

@ModOnly(CtConstants.CONTENT_TWEAKER_MOD_ID)
@ZenExpansion(CtConstants.CONTENT_TWEAKER_FACTORY)
@ZenRegister
public final class VanillaFactoryExpansion {

    private VanillaFactoryExpansion() {
    }

    @ZenMethodStatic
    public static OrganItemRepresentation createOrganItem(String id) {
        return new OrganItemRepresentation(id);
    }
}
