package com.shiver.chestcavity.client.abilitywheel;

import com.shiver.chestcavity.api.ChestCavityApis;
import net.minecraft.client.resources.I18n;

/**
 * 负责把能力与分数标识转换为界面显示文本。
 */
final class AbilityWheelText {

    /**
     * 工具类，不允许外部实例化。
     */
    private AbilityWheelText() {
    }

    /**
     * 返回能力轮盘中展示的能力名称。
     *
     * @param id 能力标识。
     * @return 本地化后的能力名称。
     */
    static String getAbilityName(String id) {
        if (id == null) {
            return "";
        }
        String displayName = ChestCavityApis.SCORES.getDisplayName(id);
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        return I18n.format("key.chestcavity." + id);
    }

    /**
     * 返回分数面板中展示的分数名称。
     *
     * @param id 分数标识。
     * @return 本地化后的分数名称。
     */
    static String getScoreName(String id) {
        if (id == null) {
            return "";
        }
        String displayName = ChestCavityApis.SCORES.getDisplayName(id);
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        String key = "organscore.chestcavity." + id;
        if (I18n.hasKey(key)) {
            return I18n.format(key, "").trim();
        }
        String abilityKey = "key.chestcavity." + id;
        if (I18n.hasKey(abilityKey)) {
            return I18n.format(abilityKey);
        }
        return id;
    }

    /**
     * 判断某项分数是否属于“越低越好”的负向分数。
     *
     * @param id 分数标识。
     * @return `true` 表示这是负向分数。
     */
    static boolean isNegativeScore(String id) {
        return id != null
                && (id.equals("metabolism")
                || id.equals("incompatibility")
                || id.equals("hydroallergenic")
                || id.equals("hydrophobia")
                || id.equals("withered"));
    }
}
