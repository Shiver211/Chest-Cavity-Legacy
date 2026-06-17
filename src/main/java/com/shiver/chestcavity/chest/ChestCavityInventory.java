package com.shiver.chestcavity.chest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ChestCavityInventory extends InventoryBasic {

    public static final int DEFAULT_SIZE = 27;

    private ChestCavityInstance instance;

    public ChestCavityInventory() {
        this(DEFAULT_SIZE, null);
    }

    public ChestCavityInventory(int size, ChestCavityInstance instance) {
        super("container.chestcavity", false, size);
        this.instance = instance;
    }

    public ChestCavityInstance getInstance() {
        return instance;
    }

    public void setInstance(ChestCavityInstance instance) {
        this.instance = instance;
    }

    public int size() {
        return getSizeInventory();
    }

    public ItemStack getStack(int index) {
        return getStackInSlot(index);
    }

    public void setStack(int index, ItemStack stack) {
        setInventorySlotContents(index, stack == null ? ItemStack.EMPTY : stack);
    }

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

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (instance == null || instance.getOwner() == null) {
            return true;
        }
        return instance.getOwner().isEntityAlive() && player.getDistance(instance.getOwner()) < 8.0F;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size(); i++) {
            super.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }
}
