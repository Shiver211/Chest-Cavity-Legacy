package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.util.IEventHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.chestcavity.AbilityManager")
public final class CrTAbilityManager {

    private CrTAbilityManager() {
    }

    @ZenMethod
    public static void registerAbility(String scoreId, String displayName) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, displayName, (player, chestCavity) -> true);
    }

    @ZenMethod
    public static void registerAbility(String scoreId, String displayName, IEventHandler<CrTAbilityActivatedEvent> handler) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, displayName, (player, chestCavity) -> {
            if (handler != null) {
                handler.handle(new CrTAbilityActivatedEvent(player, scoreId, chestCavity.getOrganScore(scoreId)));
            }
            return true;
        });
    }
}
