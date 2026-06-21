package com.shiver.chestcavity.client;

import com.shiver.chestcavity.api.ChestCavityApis;
import net.minecraft.client.resources.I18n;

final class AbilityWheelText {

    private AbilityWheelText() {
    }

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

    static boolean isNegativeScore(String id) {
        return id != null
                && (id.equals("metabolism")
                || id.equals("incompatibility")
                || id.equals("hydroallergenic")
                || id.equals("hydrophobia")
                || id.equals("withered"));
    }
}
