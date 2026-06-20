package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class SilkScoreEvents {

    private SilkScoreEvents() {
    }

    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        handleSilkInteract(event, event.getTarget());
    }

    private static void handleSilkInteract(PlayerInteractEvent.EntityInteract event, Entity target) {
        if (event.isCanceled() || event.getWorld().isRemote || !(target instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) target;
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (held.isEmpty()) {
            return;
        }

        if ((living instanceof EntityCow || living instanceof EntityMooshroom)
                && held.getItem() == Items.BUCKET
                && !living.isChild()
                && !event.getEntityPlayer().capabilities.isCreativeMode) {
            milkSilk(living);
            return;
        }

        if ((living instanceof EntitySheep || living instanceof EntityMooshroom)
                && held.getItem() == Items.SHEARS
                && !living.isChild()
                && (!(living instanceof EntitySheep) || !((EntitySheep) living).getSheared())) {
            shearSilk(living);
        }
    }

    private static boolean milkSilk(EntityLivingBase entity) {
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
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

    private static boolean shearSilk(EntityLivingBase entity) {
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
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
