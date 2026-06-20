package com.shiver.chestcavity.util;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.content.ContentRegistry;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.SlotRule;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class ChestCavityTypeUtil {

    private ChestCavityTypeUtil() {
    }

    public static boolean hasAssignedChestCavityType(ChestCavityData chestCavity) {
        return getAssignedChestCavityType(chestCavity) != null;
    }

    public static ChestCavityType getChestCavityType(ChestCavityData chestCavity) {
        ChestCavityType assignedType = getAssignedChestCavityType(chestCavity);
        if (assignedType != null) {
            return assignedType;
        }
        return DataLoaders.getType(CCConfig.getDefaultChestCavityId());
    }

    public static ChestCavityType getAssignedChestCavityType(ChestCavityData chestCavity) {
        if (chestCavity == null) {
            return null;
        }
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner != null) {
            ResourceLocation entityId = owner instanceof EntityPlayer
                    ? new ResourceLocation("minecraft", "player")
                    : EntityList.getKey(owner);
            String typeId = DataLoaders.getAssignedTypeId(entityId);
            if (typeId != null && DataLoaders.getTypes().containsKey(typeId)) {
                return DataLoaders.getType(typeId);
            }
        }
        return null;
    }

    public static boolean isSlotForbidden(ChestCavityData chestCavity, int slot) {
        return getSlotRule(chestCavity, slot).isForbidden();
    }

    public static boolean canPlaceOrgan(ChestCavityData chestCavity, int slot, ItemStack stack) {
        if (chestCavity == null) {
            return stack == null || stack.isEmpty();
        }
        SlotRule rule = getSlotRule(chestCavity, slot);
        if (stack == null || stack.isEmpty()) {
            return !rule.isForbidden();
        }
        ChestCavityType type = getChestCavityType(chestCavity);
        OrganData data = type == null ? null : type.catchExceptionalOrgan(stack);
        if (data == null) {
            data = OrganData.fromStack(stack);
        }
        return rule.canPlace(stack, data);
    }

    public static int getSlotLimit(ChestCavityData chestCavity, int slot) {
        SlotRule rule = getSlotRule(chestCavity, slot);
        return rule.isForbidden() ? 0 : rule.getMaxStackSize();
    }

    public static SlotRule getSlotRule(ChestCavityData chestCavity, int slot) {
        if (chestCavity == null) {
            return SlotRule.FORBIDDEN;
        }
        ChestLayoutDef layout = getChestLayout(chestCavity);
        if (slot < 0 || slot >= layout.getSlotCount()) {
            return SlotRule.FORBIDDEN;
        }
        ChestCavityType type = getChestCavityType(chestCavity);
        SlotRule rule = layout.getSlotRule(slot);
        if (type != null && type.isSlotForbidden(slot)) {
            rule = SlotRule.merge(rule, SlotRule.FORBIDDEN);
        }
        return rule;
    }

    public static ChestLayoutDef getChestLayout(ChestCavityData chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
        return ContentRegistry.getCompiled().getLayout(type == null ? null : type.getLayoutId());
    }
}
