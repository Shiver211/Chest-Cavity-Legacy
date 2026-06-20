package com.shiver.chestcavity.util;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.runtime.OrganInstance;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

public final class OrganCompatibilityUtil {

    private static final String COMPATIBILITY_TAG = "chestcavity:organ_compatibility";
    private static final String COMPATIBILITY_OWNER_KEY = "owner";
    private static final String COMPATIBILITY_NAME_KEY = "name";

    private OrganCompatibilityUtil() {
    }

    public static int getCompatibilityLevel(ChestCavityData chestCavity, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        if (EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, stack) > 0) {
            return 0;
        }

        int oNegative = EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack);
        int ownership = 0;
        NBTTagCompound tag = getCompatibilityTag(stack);
        if (tag == null) {
            ownership = 1;
        } else if (hasCompatibilityOwner(tag) && chestCavity != null
                && tag.getUniqueId(COMPATIBILITY_OWNER_KEY).equals(chestCavity.getCompatibilityId())) {
            ownership = 2;
        }
        return Math.max(oNegative, ownership);
    }

    public static boolean hasCompatibilityTag(ItemStack stack) {
        return getCompatibilityTag(stack) != null;
    }

    public static String getCompatibilityName(ItemStack stack) {
        NBTTagCompound tag = getCompatibilityTag(stack);
        return tag == null ? "" : tag.getString(COMPATIBILITY_NAME_KEY);
    }

    public static void assignOrganCompatibility(ChestCavityData chestCavity) {
        if (chestCavity == null) {
            return;
        }
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }

        ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(chestCavity);
        for (int i = 0; i < chestCavity.getSlotCount(); i++) {
            ItemStack stack = chestCavity.getOrgan(i);
            OrganInstance organ = chestCavity.getOrganInstance(i, type);
            if (organ.getData() != null && !organ.getData().isPseudoOrgan()) {
                setCompatibilityTag(chestCavity, stack, owner);
            }
        }

        if (type.isPlayerChestCavity()) {
            return;
        }

        Random random = owner.getRNG();
        int universalOrgans = 0;
        if (type.isBossChestCavity()) {
            universalOrgans = 3 + random.nextInt(2) + random.nextInt(2);
        } else if (random.nextFloat() < CCConfig.UNIVERSAL_DONOR_RATE) {
            universalOrgans = 1 + random.nextInt(3) + random.nextInt(3);
        }

        while (universalOrgans > 0) {
            int slot = random.nextInt(chestCavity.getSlotCount());
            ItemStack stack = chestCavity.getOrgan(slot);
            OrganInstance organ = chestCavity.getOrganInstance(slot, type);
            if (organ.getData() != null && !organ.getData().isPseudoOrgan()) {
                removeCompatibilityTag(stack);
            }
            universalOrgans--;
        }
    }

    public static void setCompatibilityTag(ChestCavityData chestCavity, ItemStack stack, EntityLivingBase owner) {
        if (chestCavity == null || stack == null || stack.isEmpty() || owner == null) {
            return;
        }

        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }

        NBTTagCompound compatibility = new NBTTagCompound();
        compatibility.setUniqueId(COMPATIBILITY_OWNER_KEY, chestCavity.getCompatibilityId());
        compatibility.setString(COMPATIBILITY_NAME_KEY, owner.getDisplayName().getUnformattedText());
        root.setTag(COMPATIBILITY_TAG, compatibility);
    }

    public static void removeCompatibilityTag(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTagCompound()) {
            stack.getTagCompound().removeTag(COMPATIBILITY_TAG);
        }
    }

    private static NBTTagCompound getCompatibilityTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(COMPATIBILITY_TAG, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        return root.getCompoundTag(COMPATIBILITY_TAG);
    }

    private static boolean hasCompatibilityOwner(NBTTagCompound tag) {
        return tag.hasKey(COMPATIBILITY_OWNER_KEY + "Most", Constants.NBT.TAG_LONG)
                && tag.hasKey(COMPATIBILITY_OWNER_KEY + "Least", Constants.NBT.TAG_LONG);
    }

}
