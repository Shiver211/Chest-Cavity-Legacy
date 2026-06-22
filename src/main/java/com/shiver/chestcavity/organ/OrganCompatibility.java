package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.registry.CCEnchantments;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * 负责器官兼容性标签的读写与兼容等级判定。
 */
public final class OrganCompatibility {

    private static final String COMPATIBILITY_TAG = "chestcavity:organ_compatibility";
    private static final String OWNER_KEY = "owner";
    private static final String NAME_KEY = "name";

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganCompatibility() {
    }

    /**
     * 返回一个器官物品相对于当前胸腔的兼容等级。
     *
     * @param chestCavity 目标胸腔数据。
     * @param stack 要检查的器官物品。
     * @return 兼容等级。
     */
    public static int getLevel(IChestCavity chestCavity, ItemStack stack) {
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

    /**
     * 判断物品上是否存在兼容性标签。
     *
     * @param stack 要检查的器官物品。
     * @return `true` 表示存在兼容性标签。
     */
    public static boolean hasTag(ItemStack stack) {
        return getTag(stack) != null;
    }

    /**
     * 返回兼容性标签中记录的拥有者名称。
     *
     * @param stack 要读取的器官物品。
     * @return 记录的名称；如果没有标签则返回空字符串。
     */
    public static String getName(ItemStack stack) {
        NBTTagCompound tag = getTag(stack);
        return tag == null ? "" : tag.getString(NAME_KEY);
    }

    /**
     * 把当前胸腔的兼容性信息写入器官物品。
     *
     * @param chestCavity 胸腔数据。
     * @param stack 要写入标签的器官物品。
     * @param owner 器官拥有者实体。
     */
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

    /**
     * 从器官物品上移除兼容性标签。
     *
     * @param stack 要处理的器官物品。
     */
    static void removeTag(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTagCompound()) {
            stack.getTagCompound().removeTag(COMPATIBILITY_TAG);
        }
    }

    /**
     * 读取器官物品上的兼容性标签。
     *
     * @param stack 要读取的器官物品。
     * @return 兼容性标签；如果不存在则返回 `null`。
     */
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

    /**
     * 判断兼容性标签中是否记录了拥有者 UUID。
     *
     * @param tag 兼容性标签。
     * @return `true` 表示存在拥有者 UUID。
     */
    private static boolean hasOwner(NBTTagCompound tag) {
        return tag.hasKey(OWNER_KEY + "Most", Constants.NBT.TAG_LONG)
                && tag.hasKey(OWNER_KEY + "Least", Constants.NBT.TAG_LONG);
    }
}
