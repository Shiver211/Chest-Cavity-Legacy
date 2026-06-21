package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayerMP;

final class BuoyantAbility implements ActiveOrganAbility {

    static final BuoyantAbility INSTANCE = new BuoyantAbility();

    private static final float AIR_COST = 4.5F;

    private BuoyantAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        if (player.getAir() <= 0) {
            return false;
        }

        float buoyancy = chestCavity.getOrganScore(CCOrganScores.BUOYANT);
        if (buoyancy <= 0.0F) {
            return false;
        }

        float airLoss = buoyancy * AIR_COST + chestCavity.getLungRemainder();
        int wholeAirLoss = (int) airLoss;
        chestCavity.setLungRemainder(airLoss - wholeAirLoss);
        if (wholeAirLoss <= 0) {
            return false;
        }

        player.setAir(Math.max(0, player.getAir() - wholeAirLoss));
        player.motionY -= Math.min(0.5D, buoyancy * CCConfig.BUOYANCY_LIFT * AIR_COST);
        player.velocityChanged = true;
        return true;
    }
}
