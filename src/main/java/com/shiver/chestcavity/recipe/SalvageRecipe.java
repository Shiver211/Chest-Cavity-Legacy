package com.shiver.chestcavity.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class SalvageRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final Ingredient input;
    private final int required;
    private final ItemStack output;

    public SalvageRecipe(Ingredient input, int required, ItemStack output) {
        this.input = input;
        this.required = Math.max(1, required);
        this.output = output.copy();
    }

    public Ingredient getInput() {
        return input;
    }

    public int getRequired() {
        return required;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return !getResultForInputCount(countMatching(inv)).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return getResultForInputCount(countMatching(inv));
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= required;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output.copy();
    }

    public ItemStack getResultForInputCount(int inputCount) {
        if (inputCount <= 0 || inputCount % required != 0) {
            return ItemStack.EMPTY;
        }

        int outputCount = (inputCount / required) * output.getCount();
        if (outputCount <= 0 || outputCount > output.getMaxStackSize()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = output.copy();
        result.setCount(outputCount);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < required; i++) {
            ingredients.add(input);
        }
        return ingredients;
    }

    private int countMatching(InventoryCrafting inv) {
        int count = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!input.apply(stack)) {
                return 0;
            }
            count++;
        }
        return count > 0 && count % required == 0 ? count : 0;
    }
}
