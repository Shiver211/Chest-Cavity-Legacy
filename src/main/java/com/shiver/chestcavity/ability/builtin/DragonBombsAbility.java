package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

final class DragonBombsAbility implements ActiveOrganAbility {

    static final DragonBombsAbility INSTANCE = new DragonBombsAbility();

    private static final float EXHAUSTION = 0.6F;
    private static final double RECOIL = 0.2D;

    private DragonBombsAbility() {
    }

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
