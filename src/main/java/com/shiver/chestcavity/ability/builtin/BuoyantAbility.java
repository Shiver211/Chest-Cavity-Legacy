package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.organ.OrganFormulas;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Spends air using breath recovery, matching the original exhale ability.
 */
final class BuoyantAbility implements ActiveOrganAbility {

    static final BuoyantAbility INSTANCE = new BuoyantAbility();

    private BuoyantAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        if (player.getAir() <= 0) {
            return false;
        }

        float recovery = chestCavity.getOrganScore(CCOrganScores.BREATH_RECOVERY);
        if (recovery <= 0.0F) {
            return false;
        }

        OrganFormulas.AirLoss loss = OrganFormulas.buoyantExhale(recovery, chestCavity.getLungRemainder());
        chestCavity.setLungRemainder(loss.remainder);
        if (loss.wholeAir <= 0) {
            return false;
        }

        player.setAir(Math.max(0, player.getAir() - loss.wholeAir));
        return true;
    }
}
