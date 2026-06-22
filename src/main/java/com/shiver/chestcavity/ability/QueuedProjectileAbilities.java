package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.entity.EntityForcefulSpit;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 负责把排队中的投射物能力转换成实际实体。
 */
final class QueuedProjectileAbilities {

    private static final float FORCEFUL_SPIT_VELOCITY = 2.0F;

    /**
     * 工具类，不允许外部实例化。
     */
    private QueuedProjectileAbilities() {
    }

    /**
     * 按能力标识生成对应的投射物实体。
     *
     * @param entity 发动能力的实体。
     * @param abilityId 要执行的能力标识。
     * @return `true` 表示投射物成功生成。
     */
    static boolean fire(EntityLivingBase entity, String abilityId) {
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }

        EntityPlayerMP player = (EntityPlayerMP) entity;
        if (CCOrganScores.PYROMANCY.equals(abilityId)) {
            return spawnPyromancyFireball(player);
        }
        if (CCOrganScores.DRAGON_BOMBS.equals(abilityId)) {
            return spawnDragonBomb(player);
        }
        if (CCOrganScores.FORCEFUL_SPIT.equals(abilityId)) {
            return spawnForcefulSpit(player);
        }
        if (CCOrganScores.GHASTLY.equals(abilityId)) {
            return spawnGhastlyFireball(player);
        }
        if (CCOrganScores.SHULKER_BULLETS.equals(abilityId)) {
            return spawnShulkerBullet(player);
        }
        return false;
    }

    /**
     * 生成一枚小型火球。
     *
     * @param player 发动能力的玩家。
     * @return `true` 表示火球成功生成。
     */
    private static boolean spawnPyromancyFireball(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntitySmallFireball fireball = new EntitySmallFireball(player.world, player, look.x, look.y, look.z);
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    /**
     * 生成一枚龙息火球。
     *
     * @param player 发动能力的玩家。
     * @return `true` 表示火球成功生成。
     */
    private static boolean spawnDragonBomb(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityDragonFireball fireball = new EntityDragonFireball(player.world, player, look.x, look.y, look.z);
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    /**
     * 生成一枚强力吐息投射物。
     *
     * @param player 发动能力的玩家。
     * @return `true` 表示投射物成功生成。
     */
    private static boolean spawnForcefulSpit(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityForcefulSpit spit = new EntityForcefulSpit(player.world, player);
        spit.setPosition(player.posX + look.x, player.posY + player.getEyeHeight() - 0.1D, player.posZ + look.z);
        spit.shoot(look.x, look.y, look.z, FORCEFUL_SPIT_VELOCITY, 0.0F);
        return player.world.spawnEntity(spit);
    }

    /**
     * 生成一枚大型恶魂火球。
     *
     * @param player 发动能力的玩家。
     * @return `true` 表示火球成功生成。
     */
    private static boolean spawnGhastlyFireball(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityLargeFireball fireball = new EntityLargeFireball(player.world, player, look.x, look.y, look.z);
        fireball.explosionPower = 1;
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    /**
     * 生成一枚自动追踪最近目标的潜影贝子弹。
     *
     * @param player 发动能力的玩家。
     * @return `true` 表示子弹成功生成。
     */
    private static boolean spawnShulkerBullet(EntityPlayerMP player) {
        EntityLivingBase target = findNearestTarget(player, CCConfig.SHULKER_BULLET_TARGETING_RANGE);
        if (target == null) {
            return false;
        }
        EntityShulkerBullet bullet = new EntityShulkerBullet(player.world, player, target, EnumFacing.Axis.Y);
        return player.world.spawnEntity(bullet);
    }

    /**
     * 获取玩家视线方向的单位向量。
     *
     * @param player 发动能力的玩家。
     * @return 归一化后的朝向向量；如果朝向无效则返回 `null`。
     */
    private static Vec3d getNormalizedLook(EntityPlayerMP player) {
        Vec3d look = player.getLookVec();
        if (look == null || look.lengthSquared() < 1.0E-4D) {
            return null;
        }
        return look.normalize();
    }

    /**
     * 将投射物起点设置到玩家眼前。
     *
     * @param player 发动能力的玩家。
     * @param projectile 要放置的投射物实体。
     * @param look 玩家当前视线方向。
     */
    private static void setProjectileStart(EntityPlayerMP player, Entity projectile, Vec3d look) {
        projectile.setPosition(player.posX + look.x,
                player.posY + player.getEyeHeight() - 0.1D,
                player.posZ + look.z);
    }

    /**
     * 在给定范围内寻找距离玩家最近的有效目标。
     *
     * @param player 发动能力的玩家。
     * @param range 搜索半径。
     * @return 最近的有效目标；如果不存在则返回 `null`。
     */
    private static EntityLivingBase findNearestTarget(EntityPlayerMP player, double range) {
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
