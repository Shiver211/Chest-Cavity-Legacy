package com.shiver.chestcavity.client.abilitywheel;

import com.shiver.chestcavity.registry.CCOrganScores;

/**
 * 统一保存能力轮盘渲染与布局使用的常量。
 */
final class AbilityWheelConstants {

    static final String[] ABILITIES = {
            CCOrganScores.BUOYANT,
            CCOrganScores.FURNACE_POWERED,
            CCOrganScores.IRON_REPAIR,
            CCOrganScores.GRAZING,
            CCOrganScores.SILK,
            CCOrganScores.CREEPY,
            CCOrganScores.DRAGON_BOMBS,
            CCOrganScores.FORCEFUL_SPIT,
            CCOrganScores.PYROMANCY,
            CCOrganScores.GHASTLY,
            CCOrganScores.SHULKER_BULLETS
    };

    static final double TWO_PI = Math.PI * 2.0D;
    static final int RADIUS = 82;
    static final int INNER_RADIUS = 24;

    /**
     * 工具类，不允许外部实例化。
     */
    private AbilityWheelConstants() {
    }
}
