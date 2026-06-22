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
 * 将强力吐息能力压入投射物队列并施加轻微后坐力。
 */
final class ForcefulSpitAbility implements ActiveOrganAbility {

    static final ForcefulSpitAbility INSTANCE = new ForcefulSpitAbility();

    private static final float EXHAUSTION = 0.1F;
    private static final double RECOIL = 0.1D;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private ForcefulSpitAbility() {
    }

    /**
     * 按吐息分数排队发射对应数量的吐息投射物。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float forcefulSpit = chestCavity.getOrganScore(CCOrganScores.FORCEFUL_SPIT);
        if (forcefulSpit <= 0.0F || player.isPotionActive(CCPotions.FORCEFUL_SPIT_COOLDOWN)) {
            return false;
        }

        Vec3d look = AbilityActivationHelper.getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int projectiles = Math.max(1, (int) forcefulSpit);
        for (int i = 0; i < projectiles; i++) {
            chestCavity.enqueueProjectileAbility(CCOrganScores.FORCEFUL_SPIT);
        }

        player.addExhaustion(projectiles * EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.FORCEFUL_SPIT_COOLDOWN,
                CCConfig.FORCEFUL_SPIT_COOLDOWN, 0, false, false));
        AbilityActivationHelper.applyRecoil(player, look, RECOIL);
        return true;
    }
}
