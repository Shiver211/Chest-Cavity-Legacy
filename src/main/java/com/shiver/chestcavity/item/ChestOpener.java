package com.shiver.chestcavity.item;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.ui.ChestCavityUiBridge;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ChestOpener extends Item {

    public ChestOpener() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (openChestCavity(playerIn, playerIn, false)) {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        if (!worldIn.isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation("message.chestcavity.chest_opener_unavailable"), true);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    public boolean openChestCavity(EntityPlayer player, EntityLivingBase target) {
        return openChestCavity(player, target, true);
    }

    public boolean openChestCavity(EntityPlayer player, @Nullable EntityLivingBase target, boolean shouldKnockback) {
        if (!canOpen(player, target)) {
            return false;
        }

        if (target.world.isRemote) {
            return true;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(target);
        if (chestCavity == null) {
            return false;
        }

        if (chestCavity.getOrganScore(com.shiver.chestcavity.registry.CCOrganScores.EASE_OF_ACCESS) <= 0.0F) {
            DamageSource damageSource = shouldKnockback && target != player
                    ? DamageSource.causePlayerDamage(player)
                    : DamageSource.GENERIC;
            target.attackEntityFrom(damageSource, 4.0F);
        } else {
            target.world.playSound(null, target.posX, target.posY, target.posZ,
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.75F, 1.0F);
        }

        if (!target.isEntityAlive()) {
            return true;
        }

        ChestCavityHelper.openChestCavity(chestCavity);
        if (player instanceof EntityPlayerMP) {
            ChestCavityUiBridge.open((EntityPlayerMP) player, target);
        }

        player.getCooldownTracker().setCooldown(this, 2);
        if (shouldKnockback && target != player) {
            target.knockBack(player, 0.2F, player.posX - target.posX, player.posZ - target.posZ);
        }
        return true;
    }

    private boolean canOpen(EntityPlayer player, @Nullable EntityLivingBase target) {
        if (!(player != null
                && target != null
                && target.isEntityAlive()
                && player.getDistanceSq(target) <= ChestCavityUiBridge.MAX_INTERACT_DISTANCE_SQ
                && target.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty())) {
            return false;
        }

        if (target instanceof EntityPlayer && target != player && !CCConfig.CAN_OPEN_OTHER_PLAYERS) {
            return false;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(target);
        if (!ChestCavityHelper.hasAssignedChestCavityType(chestCavity)) {
            return false;
        }
        return target == player || ChestCavityHelper.isOpenable(chestCavity);
    }
}
