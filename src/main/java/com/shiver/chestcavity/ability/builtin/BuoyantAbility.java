package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 消耗空气值把玩家向下压出浮力的主动能力。
 */
final class BuoyantAbility implements ActiveOrganAbility {

    static final BuoyantAbility INSTANCE = new BuoyantAbility();

    private static final float AIR_COST = 4.5F;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private BuoyantAbility() {
    }

    /**
     * 消耗空气换取一次向下的浮力推进。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        if (player.getAir() <= 0) {
            return false;
        }

        float buoyancy = chestCavity.getOrganScore(CCOrganScores.BUOYANT);
        if (buoyancy <= 0.0F) {
            return false;
        }

        float airLoss = buoyancy * AIR_COST + chestCavity.getLungRemainder();
        int wholeAirLoss = (int) airLoss;
        chestCavity.setLungRemainder(airLoss - wholeAirLoss);
        if (wholeAirLoss <= 0) {
            return false;
        }

        player.setAir(Math.max(0, player.getAir() - wholeAirLoss));
        player.motionY -= Math.min(0.5D, buoyancy * CCConfig.BUOYANCY_LIFT * AIR_COST);
        player.velocityChanged = true;
        return true;
    }
}
