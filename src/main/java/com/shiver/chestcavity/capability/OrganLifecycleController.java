package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class OrganLifecycleController {

    private OrganLifecycleController() {
    }

    static void setOrganAndRecalculate(IChestCavity chestCavity, int slot, ItemStack stack) {
        ItemStack oldStack = chestCavity.getOrgan(slot);
        ItemStack oldCopy = oldStack.isEmpty() ? ItemStack.EMPTY : oldStack.copy();
        ItemStack newCopy = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        chestCavity.setOrgan(slot, stack);
        publishOrganChange(chestCavity, slot, oldCopy, newCopy);
        applyAndSyncScoreChanges(chestCavity);
    }

    static void openChestCavity(IChestCavity chestCavity) {
        if (!chestCavity.isOpened()) {
            ChestCavityType type = OrganTypeResolver.getAssignedType(chestCavity);
            if (type == null) {
                return;
            }
            for (int i = 0; i < chestCavity.getSlotCount() && i < type.getDefaultChestCavity().size(); i++) {
                ItemStack stack = type.getDefaultChestCavity().getStack(i);
                chestCavity.setOrgan(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            chestCavity.setOpened(true);
            chestCavity.clearProjectileQueue();
            OrganTickController.disconnectCrystal(chestCavity);
            setOrganCompatibility(chestCavity);
            ChestCavityHelper.recalculateOrganScores(chestCavity);
            applyAndSyncScoreChanges(chestCavity);
            EntityLivingBase owner = chestCavity.getOwner();
            if (owner != null && !owner.world.isRemote) {
                ChestCavityNetwork.sendChestCavitySync(owner);
            }
        }
    }

    static void copy(EntityLivingBase original, EntityLivingBase replacement, boolean wasDeath) {
        IChestCavity oldCavity = ChestCavityHelper.getOrNull(original);
        IChestCavity newCavity = ChestCavityHelper.getOrNull(replacement);
        if (oldCavity != null && newCavity != null) {
            newCavity.copyFrom(oldCavity);
            newCavity.setOwner(replacement);
            if (wasDeath && replacement instanceof EntityPlayer) {
                resetPlayerChestCavityAfterDeath(oldCavity, newCavity);
            }
        }
    }

    static void destroyOrgansWithScore(IChestCavity chestCavity, String scoreId) {
        if (chestCavity == null || scoreId == null) {
            return;
        }
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        boolean changed = false;
        for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
            ItemStack stack = chestCavity.getOrgan(slot);
            OrganData data = OrganDataResolver.resolve(type, stack);
            if (data != null && data.getOrganScores().containsKey(scoreId)) {
                chestCavity.setOrgan(slot, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            applyAndSyncScoreChanges(chestCavity);
        }
    }

    static boolean isOpenable(IChestCavity chestCavity) {
        if (chestCavity == null) {
            return false;
        }
        if (OrganTypeResolver.getAssignedType(chestCavity) == null) {
            return false;
        }

        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null || !owner.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST).isEmpty()) {
            return false;
        }

        ChestCavityHelper.recalculateOrganScores(chestCavity);
        boolean weakEnough = owner.getHealth() <= com.shiver.chestcavity.config.CCConfig.CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD
                || owner.getHealth() <= owner.getMaxHealth() * com.shiver.chestcavity.config.CCConfig.CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD;
        boolean easyAccess = chestCavity.getOrganScore(CCOrganScores.EASE_OF_ACCESS) > 0.0F;
        return weakEnough || easyAccess;
    }

    static void applyAndSyncScoreChanges(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }

        if (!owner.world.isRemote) {
            OrganAttributeController.apply(owner, chestCavity);
        }

        if (ChestCavityHelper.hasScoreChanges(chestCavity)) {
            chestCavity.copyCurrentScoresToOld();
            if (!owner.world.isRemote) {
                ChestCavityNetwork.sendChestCavitySync(owner);
            }
        }
    }

    private static void setOrganCompatibility(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        for (int i = 0; i < chestCavity.getSlotCount(); i++) {
            ItemStack stack = chestCavity.getOrgan(i);
            OrganData data = OrganDataResolver.resolve(type, stack);
            if (data != null && !data.isPseudoOrgan()) {
                OrganCompatibility.setTag(chestCavity, stack, owner);
            }
        }

        if (type.isPlayerChestCavity()) {
            return;
        }

        java.util.Random random = owner.getRNG();
        int universalOrgans = 0;
        if (type.isBossChestCavity()) {
            universalOrgans = 3 + random.nextInt(2) + random.nextInt(2);
        } else if (random.nextFloat() < com.shiver.chestcavity.config.CCConfig.UNIVERSAL_DONOR_RATE) {
            universalOrgans = 1 + random.nextInt(3) + random.nextInt(3);
        }

        while (universalOrgans > 0) {
            ItemStack stack = chestCavity.getOrgan(random.nextInt(chestCavity.getSlotCount()));
            OrganData data = OrganDataResolver.resolve(type, stack);
            if (data != null && !data.isPseudoOrgan()) {
                OrganCompatibility.removeTag(stack);
            }
            universalOrgans--;
        }
    }

    private static void resetPlayerChestCavityAfterDeath(IChestCavity oldCavity, IChestCavity newCavity) {
        if (com.shiver.chestcavity.config.CCConfig.KEEP_CHEST_CAVITY) {
            ChestCavityHelper.recalculateOrganScores(newCavity);
            applyAndSyncScoreChanges(newCavity);
            return;
        }

        Map<Integer, ItemStack> preserved = new LinkedHashMap<Integer, ItemStack>();
        if (oldCavity.isOpened()) {
            for (int slot = 0; slot < oldCavity.getSlotCount(); slot++) {
                ItemStack stack = oldCavity.getOrgan(slot);
                if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack) >= 2) {
                    preserved.put(slot, stack.copy());
                }
            }
        }

        newCavity.setCompatibilityId(UUID.randomUUID());
        if (newCavity.isOpened()) {
            ChestCavityType type = ChestCavityHelper.getChestCavityType(newCavity);
            for (int slot = 0; slot < newCavity.getSlotCount(); slot++) {
                ItemStack stack = slot < type.getDefaultChestCavity().size()
                        ? type.getDefaultChestCavity().getStack(slot)
                        : ItemStack.EMPTY;
                newCavity.setOrgan(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            setOrganCompatibility(newCavity);
            for (Map.Entry<Integer, ItemStack> entry : preserved.entrySet()) {
                if (entry.getKey() >= 0 && entry.getKey() < newCavity.getSlotCount()) {
                    newCavity.setOrgan(entry.getKey(), entry.getValue());
                }
            }
        }
        ChestCavityHelper.recalculateOrganScores(newCavity);
        applyAndSyncScoreChanges(newCavity);
    }

    private static void publishOrganChange(IChestCavity chestCavity, int slot, ItemStack oldStack, ItemStack newStack) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        if (oldStack != null && !oldStack.isEmpty()) {
            OrganData oldData = OrganDataResolver.resolve(type, oldStack);
            CrTChestCavityEvents.publishOrganUnequipped(owner, slot, oldStack, oldData != null && oldData.isPseudoOrgan());
        }
        if (newStack != null && !newStack.isEmpty()) {
            OrganData newData = OrganDataResolver.resolve(type, newStack);
            CrTChestCavityEvents.publishOrganEquipped(owner, slot, newStack, newData != null && newData.isPseudoOrgan());
        }
    }
}
