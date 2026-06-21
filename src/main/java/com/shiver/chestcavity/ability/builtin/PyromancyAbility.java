package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

final class PyromancyAbility implements ActiveOrganAbility {

    static final PyromancyAbility INSTANCE = new PyromancyAbility();

    private static final float EXHAUSTION = 0.1F;
    private static final double RECOIL = 0.2D;

    private PyromancyAbility() {
    }

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
