package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityMutations;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import com.shiver.chestcavity.util.OrganCompatibilityUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class DropScoreEvents {

    private static final String BUTCHERING_TOOL_ORE = "chestcavity:butchering_tool";

    private DropScoreEvents() {
    }

    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity == null) {
            return;
        }

        addApiDrops(event);

        if (chestCavity.isOpened()) {
            removeTakenWitherStar(event, chestCavity);
            for (ItemStack stack : removeUnboundOrgansForDeath(chestCavity)) {
                addDrop(event, stack);
            }
            return;
        }

        Entity trueSource = event.getSource() == null ? null : event.getSource().getTrueSource();
        EntityLivingBase killer = trueSource instanceof EntityLivingBase ? (EntityLivingBase) trueSource : null;
        List<ItemStack> generatedLoot = generateUnopenedOrganDrops(
                chestCavity,
                event.getEntityLiving().world.rand,
                event.getLootingLevel(),
                killer);
        for (ItemStack stack : generatedLoot) {
            addDrop(event, stack);
        }
    }

    private static List<ItemStack> generateUnopenedOrganDrops(ChestCavityData chestCavity, Random random,
                                                              int baseLooting, EntityLivingBase killer) {
        List<ItemStack> loot = new ArrayList<>();
        if (chestCavity == null || random == null) {
            return loot;
        }

        ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(chestCavity);
        if (type == DataLoaders.getFallbackType() || type.isPlayerChestCavity()) {
            return loot;
        }

        int looting = Math.max(0, baseLooting);
        boolean malpractice = false;
        if (killer != null) {
            if (EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.TOMOPHOBIA, killer) > 0) {
                return loot;
            }
            looting += 2 * EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.SURGICAL, killer);

            ItemStack held = killer.getHeldItemMainhand();
            if (hasOreName(held, BUTCHERING_TOOL_ORE)) {
                looting *= 10;
            }
            malpractice = EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, held) > 0;
        }

        if (type.isBossChestCavity()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 3 + random.nextInt(2 + looting) + random.nextInt(2 + looting), random, loot);
        } else if (random.nextFloat() < (CCConfig.UNIVERSAL_DONOR_RATE + CCConfig.ORGAN_BUNDLE_LOOTING_BOOST * looting) * type.getDropRateMultiplier()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 1 + random.nextInt(3) + random.nextInt(3), random, loot);
        }

        if (malpractice) {
            processMalpractice(loot);
        }
        return loot;
    }

    private static List<ItemStack> removeUnboundOrgansForDeath(ChestCavityData chestCavity) {
        List<ItemStack> drops = new ArrayList<>();
        if (chestCavity == null || !chestCavity.isOpened()) {
            return drops;
        }

        ChestCavityMutations.apply(chestCavity, ChestCavityMutations.SyncMode.OWNER, mutation -> {
            for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
                ItemStack stack = chestCavity.getOrgan(slot);
                if (!stack.isEmpty() && OrganCompatibilityUtil.getCompatibilityLevel(chestCavity, stack) < 2) {
                    drops.add(stack.copy());
                    mutation.setOrganSilently(slot, ItemStack.EMPTY);
                }
            }
        });
        return drops;
    }

    private static void drawOrgansFromPile(List<ItemStack> organPile, int rolls, Random random, List<ItemStack> loot) {
        LinkedList<ItemStack> remaining = new LinkedList<>();
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
            OrganData data = OrganData.get(stack);
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

    private static void addApiDrops(LivingDropsEvent event) {
        ResourceLocation entityId = EntityList.getKey(event.getEntityLiving());
        if (entityId == null) {
            return;
        }
        for (ItemStack stack : ChestCavityApis.DROPS.generateDrops(entityId, event.getEntityLiving(), event.getEntityLiving().world.rand)) {
            addDrop(event, stack);
        }
    }

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        event.getDrops().add(new EntityItem(event.getEntityLiving().world,
                event.getEntityLiving().posX,
                event.getEntityLiving().posY,
                event.getEntityLiving().posZ,
                stack));
    }

    private static void removeTakenWitherStar(LivingDropsEvent event, ChestCavityData chestCavity) {
        if (!(event.getEntityLiving() instanceof EntityWither) || containsOrgan(chestCavity, Items.NETHER_STAR)) {
            return;
        }
        for (Iterator<EntityItem> iterator = event.getDrops().iterator(); iterator.hasNext();) {
            EntityItem drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (!stack.isEmpty() && stack.getItem() == Items.NETHER_STAR) {
                iterator.remove();
            }
        }
    }

    private static boolean containsOrgan(ChestCavityData chestCavity, net.minecraft.item.Item item) {
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
