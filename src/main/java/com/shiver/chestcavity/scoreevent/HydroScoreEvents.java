package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.util.EntityMovementUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public final class HydroScoreEvents {

    private HydroScoreEvents() {
    }

    public static void applyWaterSplash(Entity source) {
        if (source == null || source.world == null || source.world.isRemote) {
            return;
        }
        AxisAlignedBB box = source.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityLivingBase> entities = source.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase entity : entities) {
            if (source.getDistanceSq(entity) >= 16.0D) {
                continue;
            }
            ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
            if (chestCavity == null || !chestCavity.isOpened()) {
                continue;
            }
            float allergy = chestCavity.getOrganScore(CCOrganScores.HYDROALLERGENIC);
            if (allergy > 0.0F) {
                entity.attackEntityFrom(DamageSource.MAGIC, allergy / 26.0F);
            }
            float phobia = chestCavity.getOrganScore(CCOrganScores.HYDROPHOBIA);
            if (phobia > 0.0F) {
                EntityMovementUtil.attemptRandomTeleport(entity, phobia * 32.0F);
            }
        }
    }
}
