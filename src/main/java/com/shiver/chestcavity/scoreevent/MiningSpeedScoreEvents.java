package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class MiningSpeedScoreEvents {

    private MiningSpeedScoreEvents() {
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(event.getEntityPlayer());
        if (chestCavity != null) {
            event.setNewSpeed(event.getNewSpeed() * getMiningSpeedMultiplier(chestCavity));
        }
    }

    private static float getMiningSpeedMultiplier(ChestCavityData chestCavity) {
        ChestCavityRuntime runtime = chestCavity.getRuntime();
        float multiplier = 1.0F + runtime.getScoreValue(CCOrganScores.MINING_SPEED);
        if (runtime.getBaselineScoreValue(CCOrganScores.NERVES) != 0.0F) {
            multiplier += runtime.getDeltaScoreValue(CCOrganScores.NERVES) * CCConfig.NERVES_HASTE;
        }
        return Math.max(0.0F, multiplier);
    }
}
