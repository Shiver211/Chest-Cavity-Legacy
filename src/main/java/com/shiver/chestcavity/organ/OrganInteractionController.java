package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class OrganInteractionController {

    private OrganInteractionController() {
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
            IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
            if (chestCavity == null || !chestCavity.isOpened()) {
                continue;
            }
            float allergy = chestCavity.getOrganScore(CCOrganScores.HYDROALLERGENIC);
            if (allergy > 0.0F) {
                entity.attackEntityFrom(DamageSource.MAGIC, allergy / 26.0F);
            }
            float phobia = chestCavity.getOrganScore(CCOrganScores.HYDROPHOBIA);
            if (phobia > 0.0F) {
                OrganMovementController.attemptRandomTeleport(entity, phobia * 32.0F);
            }
        }
    }

    public static boolean milkSilk(EntityLivingBase entity) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()
                || chestCavity.getOrganScore(CCOrganScores.SILK) <= 0.0F
                || entity.isPotionActive(CCPotions.SILK_COOLDOWN)) {
            return false;
        }
        boolean spun = spinWeb(entity, chestCavity.getOrganScore(CCOrganScores.SILK));
        if (spun) {
            entity.addPotionEffect(new PotionEffect(CCPotions.SILK_COOLDOWN,
                    CCConfig.SILK_COOLDOWN, 0, false, false));
        }
        return spun;
    }

    public static boolean shearSilk(EntityLivingBase entity) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return false;
        }
        float silk = chestCavity.getOrganScore(CCOrganScores.SILK);
        if (silk <= 0.0F) {
            return false;
        }

        boolean dropped = false;
        int webs = (int) silk / 2;
        if (webs > 0) {
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ,
                    new ItemStack(Blocks.WEB, webs)));
            dropped = true;
        }
        if (silk % 2.0F >= 1.0F) {
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ,
                    new ItemStack(Items.STRING)));
            dropped = true;
        }
        return dropped;
    }

    private static boolean spinWeb(EntityLivingBase entity, float silkScore) {
        int exhaustionCost = 0;
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getFoodStats().getFoodLevel() < 6) {
            return false;
        }

        if (silkScore >= 2.0F) {
            BlockPos pos = new BlockPos(entity).offset(entity.getHorizontalFacing().getOpposite());
            if (entity.world.isAirBlock(pos)) {
                if (silkScore >= 3.0F && entity.world.setBlockState(pos, Blocks.WOOL.getDefaultState(), 2)) {
                    exhaustionCost = 16;
                    silkScore -= 3.0F;
                } else if (entity.world.setBlockState(pos, Blocks.WEB.getDefaultState(), 2)) {
                    exhaustionCost = 8;
                    silkScore -= 2.0F;
                }
            }
        }

        while (silkScore >= 1.0F) {
            silkScore -= 1.0F;
            exhaustionCost += 4;
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY + 0.5D, entity.posZ,
                    new ItemStack(Items.STRING)));
        }
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).addExhaustion(exhaustionCost);
        }
        return exhaustionCost > 0;
    }
}
