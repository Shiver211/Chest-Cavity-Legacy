package com.shiver.chestcavity.ability.builtin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 提供主动能力触发时共用的朝向、后坐力和索敌辅助逻辑。
 */
final class AbilityActivationHelper {

    /**
     * 工具类，不允许外部实例化。
     */
    private AbilityActivationHelper() {
    }

    /**
     * 获取玩家当前视线方向的单位向量。
     *
     * @param player 发动能力的玩家。
     * @return 归一化后的朝向向量；如果朝向无效则返回 `null`。
     */
    static Vec3d getNormalizedLook(EntityPlayerMP player) {
        Vec3d look = player.getLookVec();
        if (look == null || look.lengthSquared() < 1.0E-4D) {
            return null;
        }
        return look.normalize();
    }

    /**
     * 对玩家施加与视线方向相反的后坐力。
     *
     * @param player 需要施加后坐力的玩家。
     * @param look 玩家当前视线方向。
     * @param recoil 后坐力强度。
     */
    static void applyRecoil(EntityPlayerMP player, Vec3d look, double recoil) {
        player.motionX -= look.x * recoil;
        player.motionY -= look.y * recoil;
        player.motionZ -= look.z * recoil;
        player.velocityChanged = true;
    }

    /**
     * 在给定范围内寻找距离玩家最近的有效目标。
     *
     * @param player 发动能力的玩家。
     * @param range 搜索半径。
     * @return 最近的有效目标；如果不存在则返回 `null`。
     */
    static EntityLivingBase findNearestTarget(EntityPlayerMP player, double range) {
        AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
        List<EntityLivingBase> targets = player.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (EntityLivingBase target : targets) {
            if (target == player || !target.isEntityAlive()) {
                continue;
            }
            if (target instanceof EntityPlayer && ((EntityPlayer) target).isSpectator()) {
                continue;
            }

            double distance = player.getDistanceSq(target);
            if (distance < nearestDistance) {
                nearest = target;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
