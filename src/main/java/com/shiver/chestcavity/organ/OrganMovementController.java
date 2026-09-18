package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

/**
 * 负责处理器官引发的位移、重力、浮力与传送逻辑。
 */
public final class OrganMovementController {

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
    public static boolean attemptRandomTeleport(EntityLivingBase entity, float range) {
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

    /**
     * 按浮力分数在空中施加向上速度。客户端和服务端都要调用，避免玩家橡皮筋。
     *
     * @param entity 目标实体。
     * @param chestCavity 实体胸腔数据。
     */
    public static void applyBuoyancy(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity == null || chestCavity == null || !chestCavity.isOpened()) {
            return;
        }
        if (entity.onGround || entity.hasNoGravity()) {
            return;
        }
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying) {
            return;
        }

        double lift = buoyancyLift(entity, chestCavity);
        if (lift != 0.0D) {
            entity.motionY += lift;
        }
    }

    /**
     * 按轻量化分数缩放重力加速度。
     *
     * @param entity 目标实体。
     * @param gravity 原版重力常量。
     * @return 缩放后的重力。
     */
    public static double applyLightweightToGravity(EntityLivingBase entity, double gravity) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return gravity;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float diff = chestCavity.getOrganScore(CCOrganScores.LIGHTWEIGHT)
                - type.getDefaultOrganScore(CCOrganScores.LIGHTWEIGHT);
        return OrganFormulas.applyLightweightToGravity(gravity, diff, CCConfig.LIGHTWIEGHT_FACTOR);
    }

    /**
     * 按轻量化和浮力缩放本 tick 用于累计摔落距离的位移。
     *
     * @param entity 目标实体。
     * @param heightDifference 原版竖直位移（下落为负）。
     * @return 修正后的竖直位移。
     */
    public static double applyFallDistance(EntityLivingBase entity, double heightDifference) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened() || heightDifference >= 0.0D) {
            return heightDifference;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float lightweightDiff = chestCavity.getOrganScore(CCOrganScores.LIGHTWEIGHT)
                - type.getDefaultOrganScore(CCOrganScores.LIGHTWEIGHT);
        return OrganFormulas.applyOrgansToFallDistance(
                heightDifference,
                lightweightDiff,
                CCConfig.LIGHTWIEGHT_FACTOR,
                buoyancyLift(entity, chestCavity));
    }

    private static double buoyancyLift(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float buoyancyDiff = chestCavity.getOrganScore(CCOrganScores.BUOYANT)
                - type.getDefaultOrganScore(CCOrganScores.BUOYANT);
        float airRatio = entity.getAir() / (float) OrganFormulas.MAX_AIR;
        return OrganFormulas.buoyancyLift(buoyancyDiff, airRatio, CCConfig.BUOYANCY_LIFT);
    }
}
