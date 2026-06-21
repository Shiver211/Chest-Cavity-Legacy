package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

final class GhastlyAbility implements ActiveOrganAbility {

    static final GhastlyAbility INSTANCE = new GhastlyAbility();

    private static final float EXHAUSTION = 0.3F;
    private static final double RECOIL = 0.8D;

    private GhastlyAbility() {
    }

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
