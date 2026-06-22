package com.shiver.chestcavity.client.abilitywheel;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责收集当前玩家可在能力轮盘中使用的能力列表。
 */
final class AbilityWheelAbilities {

    /**
     * 工具类，不允许外部实例化。
     */
    private AbilityWheelAbilities() {
    }

    /**
     * 返回当前玩家可用的全部能力标识。
     *
     * @param minecraft 客户端实例。
     * @return 可用能力列表。
     */
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
