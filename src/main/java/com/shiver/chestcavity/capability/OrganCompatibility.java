package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.registry.CCEnchantments;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

final class OrganCompatibility {

    private static final String COMPATIBILITY_TAG = "chestcavity:organ_compatibility";
    private static final String OWNER_KEY = "owner";
    private static final String NAME_KEY = "name";

    private OrganCompatibility() {
    }

    static int getLevel(IChestCavity chestCavity, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        if (net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, stack) > 0) {
            return 0;
        }

        int oNegative = net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack);
        int ownership = 0;
        NBTTagCompound tag = getTag(stack);
        if (tag == null) {
            ownership = 1;
        } else if (hasOwner(tag) && chestCavity != null && tag.getUniqueId(OWNER_KEY).equals(chestCavity.getCompatibilityId())) {
            ownership = 2;
        }
        return Math.max(oNegative, ownership);
    }

    static boolean hasTag(ItemStack stack) {
        return getTag(stack) != null;
    }

    static String getName(ItemStack stack) {
        NBTTagCompound tag = getTag(stack);
        return tag == null ? "" : tag.getString(NAME_KEY);
    }

    static void setTag(IChestCavity chestCavity, ItemStack stack, EntityLivingBase owner) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }

        NBTTagCompound compatibility = new NBTTagCompound();
        compatibility.setUniqueId(OWNER_KEY, chestCavity.getCompatibilityId());
        compatibility.setString(NAME_KEY, owner.getDisplayName().getUnformattedText());
        root.setTag(COMPATIBILITY_TAG, compatibility);
    }

    static void removeTag(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTagCompound()) {
            stack.getTagCompound().removeTag(COMPATIBILITY_TAG);
        }
    }

    private static NBTTagCompound getTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(COMPATIBILITY_TAG, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        return root.getCompoundTag(COMPATIBILITY_TAG);
    }

    private static boolean hasOwner(NBTTagCompound tag) {
        return tag.hasKey(OWNER_KEY + "Most", Constants.NBT.TAG_LONG)
                && tag.hasKey(OWNER_KEY + "Least", Constants.NBT.TAG_LONG);
    }
}
