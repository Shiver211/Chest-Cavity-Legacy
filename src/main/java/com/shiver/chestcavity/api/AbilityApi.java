package com.shiver.chestcavity.api;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityApi {

    private static final String COOLDOWNS_TAG = "ChestCavityAbilityCooldowns";

    private final Map<String, AbilityWheelEntry> wheelEntries = new LinkedHashMap<>();
    private final Map<String, ActiveOrganAbility> activeAbilities = new LinkedHashMap<>();

    AbilityApi() {
    }

    public void registerAbility(String scoreId, ActiveOrganAbilityHandler handler) {
        registerAbility(scoreId, null, 0, handler);
    }

    public void registerAbility(String scoreId, String displayName, ActiveOrganAbilityHandler handler) {
        registerAbility(scoreId, displayName, 0, handler);
    }

    public void registerAbility(String scoreId, String displayName, int cooldownTicks, ActiveOrganAbilityHandler handler) {
        if (scoreId != null && handler != null) {
            ChestCavityApis.SCORES.addScore(scoreId, displayName);
            registerWheelEntry(scoreId, displayName, cooldownTicks, true);
            int cooldown = Math.max(0, cooldownTicks);
            registerActiveAbility(scoreId, (player, chestCavity) -> activateWithCooldown(player, chestCavity, scoreId, cooldown, handler));
        }
    }

    public void registerActiveAbility(String scoreId, ActiveOrganAbility ability) {
        if (scoreId != null && ability != null) {
            activeAbilities.put(scoreId, ability);
        }
    }

    public boolean hasActiveAbility(String scoreId) {
        return activeAbilities.containsKey(scoreId);
    }

    public boolean activate(EntityPlayerMP player, ChestCavityData chestCavity, String scoreId) {
        ActiveOrganAbility ability = activeAbilities.get(scoreId);
        if (ability == null) {
            ChestCavityLegacy.LOGGER.debug("Ignoring unknown active organ ability {}.", scoreId);
            return false;
        }
        if (chestCavity.getOrganScore(scoreId) <= 0.0F) {
            ChestCavityLegacy.LOGGER.debug("Ignoring inactive organ ability {} for {}.", scoreId, player.getName());
            return false;
        }
        boolean activated = ability.activate(player, chestCavity);
        if (activated) {
            CrTChestCavityEvents.publishAbilityActivated(player, scoreId, chestCavity.getOrganScore(scoreId));
        }
        return activated;
    }

    public void registerWheelEntry(String scoreId, String displayName, int cooldownTicks) {
        registerWheelEntry(scoreId, displayName, cooldownTicks, true);
    }

    public void registerPassiveWheelEntry(String scoreId, String displayName, int cooldownTicks) {
        registerWheelEntry(scoreId, displayName, cooldownTicks, false);
    }

    public void registerWheelEntry(String scoreId, String displayName, int cooldownTicks, boolean active) {
        if (scoreId != null) {
            ChestCavityApis.SCORES.addScore(scoreId, displayName);
            wheelEntries.put(scoreId, new AbilityWheelEntry(scoreId, Math.max(0, cooldownTicks), active));
        }
    }

    public Map<String, AbilityWheelEntry> getWheelEntries() {
        return Collections.unmodifiableMap(wheelEntries);
    }

    private boolean activateWithCooldown(EntityPlayerMP player, ChestCavityData chestCavity, String scoreId, int cooldownTicks, ActiveOrganAbilityHandler handler) {
        if (player == null || isOnCooldown(player, scoreId)) {
            return false;
        }
        boolean activated = handler.activate(player, new ChestCavityView(chestCavity));
        if (activated) {
            setCooldown(player, scoreId, cooldownTicks);
        }
        return activated;
    }

    private boolean isOnCooldown(EntityPlayerMP player, String scoreId) {
        NBTTagCompound cooldowns = player.getEntityData().getCompoundTag(COOLDOWNS_TAG);
        long expiresAt = cooldowns.getLong(scoreId);
        long now = player.world.getTotalWorldTime();
        if (expiresAt > now) {
            return true;
        }
        if (expiresAt > 0L) {
            cooldowns.removeTag(scoreId);
            player.getEntityData().setTag(COOLDOWNS_TAG, cooldowns);
        }
        return false;
    }

    private void setCooldown(EntityPlayerMP player, String scoreId, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return;
        }
        NBTTagCompound cooldowns = player.getEntityData().getCompoundTag(COOLDOWNS_TAG);
        cooldowns.setLong(scoreId, player.world.getTotalWorldTime() + cooldownTicks);
        player.getEntityData().setTag(COOLDOWNS_TAG, cooldowns);
    }

    public interface ActiveOrganAbilityHandler {
        boolean activate(EntityPlayerMP player, ChestCavityView chestCavity);
    }

    public static final class AbilityWheelEntry {
        private final String scoreId;
        private final int cooldownTicks;
        private final boolean active;

        private AbilityWheelEntry(String scoreId, int cooldownTicks, boolean active) {
            this.scoreId = scoreId;
            this.cooldownTicks = cooldownTicks;
            this.active = active;
        }

        public String getScoreId() {
            return scoreId;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }

        public boolean isActive() {
            return active;
        }
    }
}
