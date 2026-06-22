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
 * 将恶魂火球能力压入投射物队列并施加强后坐力。
 */
final class GhastlyAbility implements ActiveOrganAbility {

    static final GhastlyAbility INSTANCE = new GhastlyAbility();

    private static final float EXHAUSTION = 0.3F;
    private static final double RECOIL = 0.8D;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private GhastlyAbility() {
    }

    /**
     * 按恶魂分数排队发射对应数量的大型火球。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float ghastly = chestCavity.getOrganScore(CCOrganScores.GHASTLY);
        if (ghastly <= 0.0F || player.isPotionActive(CCPotions.GHASTLY_COOLDOWN)) {
            return false;
        }

        Vec3d look = AbilityActivationHelper.getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int fireballs = Math.max(1, (int) ghastly);
        for (int i = 0; i < fireballs; i++) {
            chestCavity.enqueueProjectileAbility(CCOrganScores.GHASTLY);
        }

        player.addExhaustion(fireballs * EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.GHASTLY_COOLDOWN,
                CCConfig.GHASTLY_COOLDOWN, 0, false, false));
        AbilityActivationHelper.applyRecoil(player, look, RECOIL);
        return true;
    }
}
