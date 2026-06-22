package com.shiver.chestcavity.chest;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * 表示胸腔内部器官槽位的基础物品栏实现。
 */
public class ChestCavityInventory extends InventoryBasic {

    public static final int DEFAULT_SIZE = 27;

    /**
     * 创建一个默认大小的胸腔物品栏。
     */
    public ChestCavityInventory() {
        this(DEFAULT_SIZE);
    }

    /**
     * 创建一个指定大小的胸腔物品栏。
     *
     * @param size 槽位数量。
     */
    public ChestCavityInventory(int size) {
        super("container.chestcavity", false, size);
    }

    /**
     * 返回物品栏槽位数量。
     *
     * @return 槽位数量。
     */
    public int size() {
        return getSizeInventory();
    }

    /**
     * 返回指定槽位中的物品。
     *
     * @param index 槽位索引。
     * @return 槽位中的物品。
     */
    public ItemStack getStack(int index) {
        return getStackInSlot(index);
    }

    /**
     * 设置指定槽位中的物品。
     *
     * @param index 槽位索引。
     * @param stack 要放入的物品。
     */
    public void setStack(int index, ItemStack stack) {
        setInventorySlotContents(index, stack == null ? ItemStack.EMPTY : stack);
    }

    /**
     * 从 NBT 列表中恢复物品栏内容。
     *
     * @param tags 序列化后的物品列表。
     */
    public void readTags(NBTTagList tags) {
        clear();
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound itemTag = tags.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < size()) {
                setStack(slot, new ItemStack(itemTag));
            }
        }
    }

    /**
     * 将当前物品栏内容写出为 NBT 列表。
     *
     * @return 序列化后的物品列表。
     */
    public NBTTagList getTags() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                stack.writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        return list;
    }

    /**
     * 清空全部槽位中的物品。
     */
    @Override
    public void clear() {
        for (int i = 0; i < size(); i++) {
            super.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }
}
