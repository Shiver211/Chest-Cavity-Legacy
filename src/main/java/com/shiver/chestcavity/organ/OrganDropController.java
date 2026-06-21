package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.registry.CCEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class OrganDropController {

    private static final String BUTCHERING_TOOL_ORE = "chestcavity:butchering_tool";

    private OrganDropController() {
    }

    public static List<ItemStack> generateUnopenedOrganDrops(IChestCavity chestCavity, Random random, int baseLooting, EntityLivingBase killer) {
        List<ItemStack> loot = new ArrayList<ItemStack>();
        if (chestCavity == null || random == null) {
            return loot;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        if (type == DataLoaders.getFallbackType() || type.isPlayerChestCavity()) {
            return loot;
        }

        int looting = Math.max(0, baseLooting);
        boolean butcher = false;
        boolean malpractice = false;
        if (killer != null) {
            if (EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.TOMOPHOBIA, killer) > 0) {
                return loot;
            }
            looting += 2 * EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.SURGICAL, killer);

            ItemStack held = killer.getHeldItemMainhand();
            butcher = hasOreName(held, BUTCHERING_TOOL_ORE);
            if (butcher) {
                looting *= 10;
            }
            malpractice = EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, held) > 0;
        }

        if (type.isBossChestCavity()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 3 + random.nextInt(2 + looting) + random.nextInt(2 + looting), random, loot);
        } else if (random.nextFloat() < (CCConfig.UNIVERSAL_DONOR_RATE + CCConfig.ORGAN_BUNDLE_LOOTING_BOOST * looting) * type.getDropRateMultiplier()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 1 + random.nextInt(3) + random.nextInt(3), random, loot);
        }

        if (butcher) {
            // salvage recipes removed
        }
        if (malpractice) {
            processMalpractice(loot);
        }
        return loot;
    }

    public static List<ItemStack> removeUnboundOrgansForDeath(IChestCavity chestCavity) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        if (chestCavity == null || !chestCavity.isOpened()) {
            return drops;
        }

        for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
            ItemStack stack = chestCavity.getOrgan(slot);
            if (!stack.isEmpty() && ChestCavityHelper.getCompatibilityLevel(chestCavity, stack) < 2) {
                drops.add(stack.copy());
                chestCavity.setOrgan(slot, ItemStack.EMPTY);
            }
        }
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
        return drops;
    }

    private static void drawOrgansFromPile(List<ItemStack> organPile, int rolls, Random random, List<ItemStack> loot) {
        LinkedList<ItemStack> remaining = new LinkedList<ItemStack>();
        for (ItemStack stack : organPile) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }

        for (int i = 0; i < rolls && !remaining.isEmpty(); i++) {
            ItemStack rolledItem = remaining.remove(random.nextInt(remaining.size())).copy();
            int count = 1;
            if (rolledItem.getCount() > 1) {
                count += random.nextInt(rolledItem.getMaxStackSize());
            }
            rolledItem.setCount(count);
            loot.add(rolledItem);
        }
    }

    private static void processMalpractice(List<ItemStack> loot) {
        for (ItemStack stack : loot) {
            OrganData data = OrganManager.get(stack);
            if (data != null && !data.isPseudoOrgan()) {
                stack.addEnchantment(CCEnchantments.MALPRACTICE, 1);
            }
        }
    }

    private static boolean hasOreName(ItemStack stack, String name) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (name.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }
}
