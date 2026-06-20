package com.shiver.chestcavity.api;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityApi {

    private static final String COOLDOWNS_TAG = "ChestCavityAbilityCooldowns";

    private final Map<String, AbilityWheelEntry> wheelEntries = new LinkedHashMap<>();

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
            int cooldown = Math.max(0, cooldownTicks);
            wheelEntries.put(scoreId, new AbilityWheelEntry(scoreId, cooldown));
            ActiveOrganAbilities.register(scoreId, (player, chestCavity) -> activateWithCooldown(player, chestCavity, scoreId, cooldown, handler));
        }
    }

    public Map<String, AbilityWheelEntry> getWheelEntries() {
        return Collections.unmodifiableMap(wheelEntries);
    }

    private boolean activateWithCooldown(EntityPlayerMP player, IChestCavity chestCavity, String scoreId, int cooldownTicks, ActiveOrganAbilityHandler handler) {
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

        private AbilityWheelEntry(String scoreId, int cooldownTicks) {
            this.scoreId = scoreId;
            this.cooldownTicks = cooldownTicks;
        }

        public String getScoreId() {
            return scoreId;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }
    }
}
