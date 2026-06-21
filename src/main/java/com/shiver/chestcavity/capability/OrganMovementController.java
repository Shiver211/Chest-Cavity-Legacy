package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

final class OrganMovementController {

    private OrganMovementController() {
    }

    static boolean attemptRandomTeleport(EntityLivingBase entity, float range) {
        if (entity.world.isRemote || !entity.isEntityAlive()) {
            return false;
        }

        for (int i = 0; i < Math.max(1, CCConfig.MAX_TELEPORT_ATTEMPTS); i++) {
            double x = entity.posX + (entity.getRNG().nextDouble() - 0.5D) * range;
            double y = Math.max(1.0D, entity.posY + (entity.getRNG().nextDouble() - 0.5D) * range);
            double z = entity.posZ + (entity.getRNG().nextDouble() - 0.5D) * range;
            if (entity.attemptTeleport(x, y, z)) {
                entity.world.playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }
}
