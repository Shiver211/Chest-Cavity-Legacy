package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.registry.CCOrganScores;

public final class BuiltinOrganAbilities {

    private BuiltinOrganAbilities() {
    }

    public static void registerAll() {
        ActiveOrganAbilities.register(CCOrganScores.FURNACE_POWERED, FurnacePoweredAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.GRAZING, GrazingAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.IRON_REPAIR, IronRepairAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.BUOYANT, BuoyantAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.CREEPY, CreepyAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.PYROMANCY, PyromancyAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.DRAGON_BOMBS, DragonBombsAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.FORCEFUL_SPIT, ForcefulSpitAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.GHASTLY, GhastlyAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.SHULKER_BULLETS, ShulkerBulletsAbility.INSTANCE);
        ActiveOrganAbilities.register(CCOrganScores.SILK, SilkAbility.INSTANCE);
    }
}
