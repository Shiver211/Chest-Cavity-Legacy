package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.event.IEventHandle;
import crafttweaker.api.event.IEventManager;
import crafttweaker.util.IEventHandler;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenExpansion("crafttweaker.events.IEventManager")
public final class CrTChestCavityEventManager {

    private CrTChestCavityEventManager() {
    }

    @ZenMethod
    public static IEventHandle onAbilityActivated(IEventManager manager, IEventHandler<CrTAbilityActivatedEvent> handler) {
        return CrTChestCavityEvents.ABILITY_ACTIVATED.add(handler);
    }

    @ZenMethod
    public static IEventHandle onOrganEquipped(IEventManager manager, IEventHandler<CrTOrganEquippedEvent> handler) {
        return CrTChestCavityEvents.ORGAN_EQUIPPED.add(handler);
    }

    @ZenMethod
    public static IEventHandle onOrganUnequipped(IEventManager manager, IEventHandler<CrTOrganUnequippedEvent> handler) {
        return CrTChestCavityEvents.ORGAN_UNEQUIPPED.add(handler);
    }
}
