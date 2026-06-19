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
        if (scoreId != null && handler != null) {
            ActiveOrganAbilities.register(scoreId, (player, chestCavity) -> handler.activate(player, new ChestCavityView(chestCavity)));
        }
    }

    public void addToWheel(String scoreId, String name, int color) {
        if (scoreId != null) {
            wheelEntries.put(scoreId, new AbilityWheelEntry(scoreId, name, color));
        }
    }

    public void removeFromWheel(String scoreId) {
        if (scoreId != null) {
            wheelEntries.remove(scoreId);
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
        private final String name;
        private final int color;

        private AbilityWheelEntry(String scoreId, String name, int color) {
            this.scoreId = scoreId;
            this.name = name;
            this.color = color;
        }

        public String getScoreId() {
            return scoreId;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }
    }
}
