package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

final class ForcefulSpitAbility implements ActiveOrganAbility {

    static final ForcefulSpitAbility INSTANCE = new ForcefulSpitAbility();

    private static final float EXHAUSTION = 0.1F;
    private static final double RECOIL = 0.1D;

    private ForcefulSpitAbility() {
    }

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
