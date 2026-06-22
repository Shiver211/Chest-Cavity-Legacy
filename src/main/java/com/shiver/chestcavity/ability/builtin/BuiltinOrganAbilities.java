package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.registry.CCOrganScores;

/**
 * 负责注册模组内置的全部主动器官能力。
 */
public final class BuiltinOrganAbilities {

    /**
     * 工具类，不允许外部实例化。
     */
    private BuiltinOrganAbilities() {
    }

    /**
     * 将所有内置主动能力注册到总表中。
     */
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
