package com.shiver.chestcavity.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

public class SalvageRecipeFactory implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        Ingredient input = parseIngredient(context, JsonUtils.getJsonObject(json, "ingredient"));
        int required = JsonUtils.getInt(json, "required", 1);
        ItemStack output = parseResult(context, json.get("result"), JsonUtils.getInt(json, "count", 1));
        return new SalvageRecipe(input, required, output);
    }

    private Ingredient parseIngredient(JsonContext context, JsonObject json) {
        if (json.has("tag")) {
            return new OreIngredient(JsonUtils.getString(json, "tag"));
        }
        return CraftingHelper.getIngredient(json, context);
    }

    private ItemStack parseResult(JsonContext context, JsonElement result, int count) {
        if (result == null) {
            throw new JsonSyntaxException("Missing result for salvage recipe");
        }
        if (result.isJsonObject()) {
            JsonObject resultObject = result.getAsJsonObject();
            ItemStack stack = CraftingHelper.getItemStack(resultObject, context);
            if (!resultObject.has("count")) {
                stack.setCount(count);
            }
            return stack;
        }

        String itemName = context.appendModId(JsonUtils.getString(result, "result"));
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + itemName + "'");
        }
        return new ItemStack(item, count);
    }
}
