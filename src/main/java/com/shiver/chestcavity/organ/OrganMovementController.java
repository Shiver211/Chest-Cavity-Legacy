package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

/**
 * 负责处理器官引发的位移与传送逻辑。
 */
final class OrganMovementController {

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganMovementController() {
    }

    /**
     * 在给定范围内多次尝试随机传送实体。
     *
     * @param entity 要传送的实体。
     * @param range 传送搜索范围。
     * @return `true` 表示至少一次传送成功。
     */
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
