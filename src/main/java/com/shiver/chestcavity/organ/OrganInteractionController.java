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

/**
 * 负责水、丝等与世界交互相关的器官逻辑。
 */
public final class OrganInteractionController {

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganInteractionController() {
    }

    /**
     * 对水瓶爆炸附近的实体应用亲水与恐水相关效果。
     *
     * @param source 水源实体。
     */
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

    /**
     * 尝试从目标实体身上“挤出”丝。
     *
     * @param entity 目标实体。
     * @return `true` 表示成功产出丝或丝块。
     */
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

    /**
     * 尝试通过剪切直接从目标实体身上取出丝材料。
     *
     * @param entity 目标实体。
     * @return `true` 表示成功产出掉落物。
     */
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

    /**
     * 按丝分数在实体背后生成蛛网、羊毛或线，并消耗饥饿值。
     *
     * @param entity 目标实体。
     * @param silkScore 丝分数。
     * @return `true` 表示成功生成了至少一个产物。
     */
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
