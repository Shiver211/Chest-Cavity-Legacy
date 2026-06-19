package com.shiver.chestcavity.api;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityApi {

    private final Map<String, AbilityWheelEntry> wheelEntries = new LinkedHashMap<String, AbilityWheelEntry>();

    AbilityApi() {
    }

    public void registerAbility(String scoreId, ActiveOrganAbilityHandler handler) {
        registerAbility(scoreId, null, handler);
    }

    public void registerAbility(String scoreId, String displayName, ActiveOrganAbilityHandler handler) {
        if (scoreId != null && handler != null) {
            ChestCavityApis.SCORES.addScore(scoreId, displayName);
            wheelEntries.put(scoreId, new AbilityWheelEntry(scoreId));
            ActiveOrganAbilities.register(scoreId, (player, chestCavity) -> handler.activate(player, new ChestCavityView(chestCavity)));
        }
    }

    public Map<String, AbilityWheelEntry> getWheelEntries() {
        return Collections.unmodifiableMap(wheelEntries);
    }

    public interface ActiveOrganAbilityHandler {
        boolean activate(EntityPlayerMP player, ChestCavityView chestCavity);
    }

    public static final class AbilityWheelEntry {
        private final String scoreId;

        private AbilityWheelEntry(String scoreId) {
            this.scoreId = scoreId;
        }

        public String getScoreId() {
            return scoreId;
        }
    }
}
