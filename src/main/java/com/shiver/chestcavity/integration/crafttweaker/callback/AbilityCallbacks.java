package com.shiver.chestcavity.integration.crafttweaker.callback;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import stanhebben.zenscript.annotations.ZenClass;

public final class AbilityCallbacks {

    private AbilityCallbacks() {
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnActivate")
    @ZenRegister
    public interface OnActivate {
        boolean handle(IPlayer player, String abilityId, float value);
    }
}
