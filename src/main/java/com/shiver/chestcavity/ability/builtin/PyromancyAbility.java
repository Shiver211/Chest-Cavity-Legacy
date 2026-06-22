package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

/**
 * 将火焰弹能力压入投射物队列并施加少量后坐力。
 */
final class PyromancyAbility implements ActiveOrganAbility {

    static final PyromancyAbility INSTANCE = new PyromancyAbility();

    private static final float EXHAUSTION = 0.1F;
    private static final double RECOIL = 0.2D;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private PyromancyAbility() {
    }

    /**
     * 按火焰分数排队发射对应数量的小型火球。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float pyromancy = chestCavity.getOrganScore(CCOrganScores.PYROMANCY);
        if (pyromancy <= 0.0F || player.isPotionActive(CCPotions.PYROMANCY_COOLDOWN)) {
            return false;
        }

        int fireballs = Math.max(1, (int) pyromancy);
        Vec3d look = player.getLookVec().normalize();
        if (look == Vec3d.ZERO) {
            return false;
        }

        for (int i = 0; i < fireballs; i++) {
            chestCavity.enqueueProjectileAbility(CCOrganScores.PYROMANCY);
        }

        player.addExhaustion(fireballs * EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.PYROMANCY_COOLDOWN,
                CCConfig.PYROMANCY_COOLDOWN, 0, false, false));
        player.motionX -= look.x * RECOIL;
        player.motionY -= look.y * RECOIL;
        player.motionZ -= look.z * RECOIL;
        player.velocityChanged = true;
        return true;
    }
}
