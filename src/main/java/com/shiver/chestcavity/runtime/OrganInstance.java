package com.shiver.chestcavity.runtime;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class OrganInstance {

    private final ItemStack stack;
    private final ResourceLocation itemId;
    private final OrganData data;
    private final float stackRatio;
    private final int compatibilityLevel;

    private OrganInstance(ItemStack stack, ResourceLocation itemId, OrganData data, float stackRatio, int compatibilityLevel) {
        this.stack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.itemId = itemId;
        this.data = data;
        this.stackRatio = stackRatio;
        this.compatibilityLevel = compatibilityLevel;
    }

    public static OrganInstance empty() {
        return new OrganInstance(ItemStack.EMPTY, null, null, 0.0F, 1);
    }

    public static OrganInstance of(ItemStack stack, OrganData data, int compatibilityLevel) {
        if (stack == null || stack.isEmpty()) {
            return empty();
        }
        Item item = stack.getItem();
        ResourceLocation itemId = item == null ? null : item.getRegistryName();
        float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
        return new OrganInstance(stack, itemId, data, stackRatio, compatibilityLevel);
    }

    public ItemStack getStack() {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    ItemStack getStackView() {
        return stack;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public OrganData getData() {
        return data;
    }

    public float getStackRatio() {
        return stackRatio;
    }

    public int getCompatibilityLevel() {
        return compatibilityLevel;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
