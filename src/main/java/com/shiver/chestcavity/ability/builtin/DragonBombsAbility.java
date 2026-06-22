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
 * 将龙弹能力压入投射物队列并施加冷却与后坐力。
 */
final class DragonBombsAbility implements ActiveOrganAbility {

    static final DragonBombsAbility INSTANCE = new DragonBombsAbility();

    private static final float EXHAUSTION = 0.6F;
    private static final double RECOIL = 0.2D;

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private DragonBombsAbility() {
    }

    /**
     * 按龙弹分数排队发射对应数量的龙息火球。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float dragonBombs = chestCavity.getOrganScore(CCOrganScores.DRAGON_BOMBS);
        if (dragonBombs <= 0.0F || player.isPotionActive(CCPotions.DRAGON_BOMB_COOLDOWN)) {
            return false;
        }

        Vec3d look = AbilityActivationHelper.getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int bombs = Math.max(1, (int) dragonBombs);
        for (int i = 0; i < bombs; i++) {
            chestCavity.enqueueProjectileAbility(CCOrganScores.DRAGON_BOMBS);
        }

        player.addExhaustion(bombs * EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.DRAGON_BOMB_COOLDOWN,
                CCConfig.DRAGON_BOMB_COOLDOWN, 0, false, false));
        AbilityActivationHelper.applyRecoil(player, look, RECOIL);
        return true;
    }
}
