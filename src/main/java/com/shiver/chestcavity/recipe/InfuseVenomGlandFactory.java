package com.shiver.chestcavity.recipe;

import com.google.gson.JsonObject;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class InfuseVenomGlandFactory implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        return new InfuseVenomGlandRecipe();
    }
}
