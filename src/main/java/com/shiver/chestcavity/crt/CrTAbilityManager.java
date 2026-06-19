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
    public static void registerAbility(String scoreId) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, (player, chestCavity) -> true);
    }

    @ZenMethod
    public static void registerAbility(String scoreId, IEventHandler<CrTAbilityActivatedEvent> handler) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, (player, chestCavity) -> {
            if (handler != null) {
                handler.handle(new CrTAbilityActivatedEvent(player, scoreId, chestCavity.getOrganScore(scoreId)));
            }
            return true;
        });
    }

    @ZenMethod
    public static void addToWheel(String scoreId, String name, int color) {
        ChestCavityApis.ABILITIES.addToWheel(scoreId, name, color);
    }

    @ZenMethod
    public static void removeFromWheel(String scoreId) {
        ChestCavityApis.ABILITIES.removeFromWheel(scoreId);
    }
}
