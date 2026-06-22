package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

/**
 * 将潜影贝子弹能力压入投射物队列的主动能力。
 */
final class ShulkerBulletsAbility implements ActiveOrganAbility {

    static final ShulkerBulletsAbility INSTANCE = new ShulkerBulletsAbility();

    private static final float EXHAUSTION = 0.3F;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private ShulkerBulletsAbility() {
    }

    /**
     * 在找到目标时排队发射对应数量的潜影贝子弹。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float shulkerBullets = chestCavity.getOrganScore(CCOrganScores.SHULKER_BULLETS);
        if (shulkerBullets <= 0.0F || player.isPotionActive(CCPotions.SHULKER_BULLET_COOLDOWN)) {
            return false;
        }

        EntityLivingBase target = AbilityActivationHelper.findNearestTarget(player, CCConfig.SHULKER_BULLET_TARGETING_RANGE);
        if (target == null) {
            return false;
        }

        int bullets = Math.max(1, (int) shulkerBullets);
        for (int i = 0; i < bullets; i++) {
            chestCavity.enqueueProjectileAbility(CCOrganScores.SHULKER_BULLETS);
        }

        player.addExhaustion(bullets * EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.SHULKER_BULLET_COOLDOWN,
                CCConfig.SHULKER_BULLET_COOLDOWN, 0, false, false));
        return true;
    }
}
