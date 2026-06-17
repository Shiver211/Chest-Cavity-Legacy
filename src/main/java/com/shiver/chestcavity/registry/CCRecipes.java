package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.recipe.InfuseVenomGlandFactory;
import com.shiver.chestcavity.recipe.SalvageRecipeFactory;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.IForgeRegistry;

public final class CCRecipes {

    private static boolean factoriesRegistered;

    private CCRecipes() {
    }

    public static void register(IForgeRegistry<IRecipe> registry) {
        registerFactories();
    }

    public static void registerFactories() {
        if (factoriesRegistered) {
            return;
        }
        CraftingHelper.register(new ResourceLocation(Tags.MOD_ID, "crafting_salvage"), new SalvageRecipeFactory());
        CraftingHelper.register(new ResourceLocation(Tags.MOD_ID, "crafting_special_infuse_venom_gland"), new InfuseVenomGlandFactory());
        factoriesRegistered = true;
    }
}
