package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

final class IronRepairAbility implements ActiveOrganAbility {

    static final IronRepairAbility INSTANCE = new IronRepairAbility();

    private IronRepairAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float ironRepair = chestCavity.getOrganScore(CCOrganScores.IRON_REPAIR)
                - ChestCavityHelper.getChestCavityType(chestCavity).getDefaultOrganScore(CCOrganScores.IRON_REPAIR);
        if (ironRepair <= 0.0F
                || player.isPotionActive(CCPotions.IRON_REPAIR_COOLDOWN)
                || player.getHealth() >= player.getMaxHealth()) {
            return false;
        }

        ItemStack material = findIronRepairMaterial(player);
        if (material == null) {
            return false;
        }

        float healAmount = player.getMaxHealth() * CCConfig.IRON_REPAIR_PERCENT * getIronRepairMaterialValue(material);
        player.heal(healAmount);
        player.addPotionEffect(new PotionEffect(CCPotions.IRON_REPAIR_COOLDOWN,
                Math.max(1, Math.round(CCConfig.IRON_REPAIR_COOLDOWN / ironRepair)), 0, false, false));
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.75F, 1.0F);
        material.shrink(1);
        return true;
    }

    private static ItemStack findIronRepairMaterial(EntityPlayerMP player) {
        ItemStack mainHand = findIronRepairMaterial(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findIronRepairMaterial(player, EnumHand.OFF_HAND) : mainHand;
    }

    private static ItemStack findIronRepairMaterial(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }

        return isIronRepairMaterial(stack) ? stack : null;
    }

    private static boolean isIronRepairMaterial(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.IRON_NUGGET || item == Items.IRON_INGOT || item == Item.getItemFromBlock(Blocks.IRON_BLOCK);
    }

    private static float getIronRepairMaterialValue(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.IRON_NUGGET) {
            return 1.0F / 9.0F;
        }
        if (item == Item.getItemFromBlock(Blocks.IRON_BLOCK)) {
            return 9.0F;
        }
        return 1.0F;
    }
}
