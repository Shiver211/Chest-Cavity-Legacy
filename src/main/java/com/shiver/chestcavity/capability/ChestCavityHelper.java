package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.network.ChestCavityNetwork;

import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public final class ChestCavityHelper {

    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation("chestcavity", "chest_cavity");

    private ChestCavityHelper() {
    }

    public static Optional<ChestCavityData> get(Entity entity) {
        ChestCavityCapability.ensureRegistered();
        if (!(entity instanceof EntityLivingBase) || ChestCavityCapability.CAPABILITY == null) {
            return Optional.empty();
        }

        ChestCavityData chestCavity = entity.getCapability(ChestCavityCapability.CAPABILITY, null);
        if (chestCavity != null) {
            chestCavity.setOwner((EntityLivingBase) entity);
            return Optional.of(chestCavity);
        }
        return Optional.empty();
    }

    public static ChestCavityData getOrNull(Entity entity) {
        Optional<ChestCavityData> chestCavity = get(entity);
        return chestCavity.orElse(null);
    }

    public static void tick(EntityLivingBase entity, ChestCavityData chestCavity) {
        if (chestCavity != null && chestCavity.isRuntimeDirty()) {
            chestCavity.refreshRuntimeIfDirty();
        }

        if (!entity.world.isRemote) {
            clampHealthToMax(entity);
        }
    }

    public static void copy(EntityLivingBase original, EntityLivingBase replacement) {
        ChestCavityData oldCavity = getOrNull(original);
        ChestCavityData newCavity = getOrNull(replacement);
        if (oldCavity != null && newCavity != null) {
            newCavity.copyFrom(oldCavity);
            newCavity.setOwner(replacement);
        }
    }

    public static boolean activateScore(EntityPlayerMP player, ChestCavityData chestCavity, String scoreId) {
        if (player == null || chestCavity == null || scoreId == null) {
            return false;
        }
        return ChestCavityApis.ABILITIES.hasActiveAbility(scoreId)
                && ChestCavityApis.ABILITIES.activate(player, chestCavity, scoreId);
    }

    public static boolean isOpenable(ChestCavityData chestCavity) {
        if (chestCavity == null) {
            return false;
        }
        if (ChestCavityTypeUtil.getAssignedChestCavityType(chestCavity) == null) {
            return false;
        }

        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null || !owner.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST).isEmpty()) {
            return false;
        }

        boolean weakEnough = owner.getHealth() <= CCConfig.CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD
                || owner.getHealth() <= owner.getMaxHealth() * CCConfig.CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD;
        boolean easyAccess = chestCavity.getOrganScore(CCOrganScores.EASE_OF_ACCESS) > 0.0F;
        return weakEnough || easyAccess;
    }

    private static void clampHealthToMax(EntityLivingBase entity) {
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

}
