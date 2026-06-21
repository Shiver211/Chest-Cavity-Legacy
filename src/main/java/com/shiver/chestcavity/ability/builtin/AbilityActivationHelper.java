package com.shiver.chestcavity.ability.builtin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.List;

final class AbilityActivationHelper {

    private AbilityActivationHelper() {
    }

    static Vec3d getNormalizedLook(EntityPlayerMP player) {
        Vec3d look = player.getLookVec();
        if (look == null || look.lengthSquared() < 1.0E-4D) {
            return null;
        }
        return look.normalize();
    }

    static void applyRecoil(EntityPlayerMP player, Vec3d look, double recoil) {
        player.motionX -= look.x * recoil;
        player.motionY -= look.y * recoil;
        player.motionZ -= look.z * recoil;
        player.velocityChanged = true;
    }

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
