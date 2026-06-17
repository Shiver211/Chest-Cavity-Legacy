package com.shiver.chestcavity.recipe;

import com.shiver.chestcavity.registry.CCItems;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class InfuseVenomGlandRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return findResult(inv) != ItemStack.EMPTY;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return findResult(inv);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private ItemStack findResult(InventoryCrafting inv) {
        ItemStack gland = ItemStack.EMPTY;
        ItemStack potion = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == CCItems.VENOM_GLAND) {
                if (!gland.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                gland = stack;
            } else if (isPotion(stack.getItem())) {
                if (!potion.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                potion = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (gland.isEmpty() || potion.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = gland.copy();
        result.setCount(1);
        PotionUtils.addPotionToItemStack(result, PotionUtils.getPotionFromItem(potion));
        PotionUtils.appendEffects(result, PotionUtils.getFullEffectsFromItem(potion));
        return result;
    }

    private boolean isPotion(Item item) {
        return item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }
}
