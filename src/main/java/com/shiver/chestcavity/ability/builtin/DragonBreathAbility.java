package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

/**
 * Queues a dragon-breath cloud in front of the player.
 */
final class DragonBreathAbility implements ActiveOrganAbility {

    static final DragonBreathAbility INSTANCE = new DragonBreathAbility();

    private static final float EXHAUSTION = 0.6F;

    private DragonBreathAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float breath = chestCavity.getOrganScore(CCOrganScores.DRAGON_BREATH);
        if (breath <= 0.0F || player.isPotionActive(CCPotions.DRAGON_BREATH_COOLDOWN)) {
            return false;
        }

        player.addExhaustion(breath * EXHAUSTION);
        chestCavity.enqueueProjectileAbility(CCOrganScores.DRAGON_BREATH);
        player.addPotionEffect(new PotionEffect(CCPotions.DRAGON_BREATH_COOLDOWN,
                CCConfig.DRAGON_BREATH_COOLDOWN, 0, false, false));
        return true;
    }
}
