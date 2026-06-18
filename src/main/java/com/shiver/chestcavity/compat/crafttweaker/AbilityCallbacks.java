package com.shiver.chestcavity.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import stanhebben.zenscript.annotations.ZenClass;

public final class AbilityCallbacks {

    private AbilityCallbacks() {
    }

    @ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "abilities.OnActivate")
    @ZenRegister
    public interface OnActivate {
        boolean handle(IPlayer player, String abilityId, float value);
    }
}
