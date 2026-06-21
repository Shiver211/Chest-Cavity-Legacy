package com.shiver.chestcavity.client.abilitywheel;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

final class AbilityWheelAbilities {

    private AbilityWheelAbilities() {
    }

    static List<String> getAvailableAbilities(Minecraft minecraft) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(minecraft.player);
        List<String> result = new ArrayList<String>();
        if (chestCavity == null) {
            return result;
        }

        for (String ability : AbilityWheelConstants.ABILITIES) {
            if (chestCavity.getOrganScore(ability) > 0.0F) {
                result.add(ability);
            }
        }
        for (String ability : ChestCavityApis.ABILITIES.getWheelEntries().keySet()) {
            if (chestCavity.getOrganScore(ability) > 0.0F && !result.contains(ability)) {
                result.add(ability);
            }
        }
        return result;
    }
}
