package com.shiver.chestcavity.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import com.shiver.chestcavity.recipe.SalvageRecipe;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class SalvageJeiPlugin implements IModPlugin {

    public static final String UID = "chestcavity:crafting_salvage";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new Category(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(Display.class, Wrapper::new, UID);
        registry.addRecipes(getSalvageDisplays(), UID);
    }

    public static class Display {
        private final List<List<ItemStack>> inputs;
        private final ItemStack output;

        public Display(List<List<ItemStack>> inputs, ItemStack output) {
            this.inputs = inputs;
            this.output = output;
        }

        public static Display ofRepeatedInput(List<ItemStack> input, int required, ItemStack output) {
            List<List<ItemStack>> inputs = new ArrayList<>();
            for (int i = 0; i < required; i++) {
                inputs.add(input);
            }
            return new Display(inputs, output);
        }
    }

    public static class Wrapper implements IRecipeWrapper {
        private final Display display;

        public Wrapper(Display display) {
            this.display = display;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInputLists(VanillaTypes.ITEM, display.inputs);
            ingredients.setOutput(VanillaTypes.ITEM, display.output);
        }
    }

    private static List<Display> getSalvageDisplays() {
        List<Display> displays = new ArrayList<Display>();
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe instanceof SalvageRecipe) {
                SalvageRecipe salvageRecipe = (SalvageRecipe) recipe;
                displays.add(Display.ofRepeatedInput(
                        asList(salvageRecipe.getInput().getMatchingStacks()),
                        salvageRecipe.getRequired(),
                        salvageRecipe.getRecipeOutput()));
            }
        }
        return displays;
    }

    private static List<ItemStack> asList(ItemStack[] stacks) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack stack : stacks) {
            list.add(stack);
        }
        return list;
    }

    private static class Category implements IRecipeCategory<Wrapper> {
        private final IDrawable background;

        private Category(IGuiHelper guiHelper) {
            this.background = guiHelper.createBlankDrawable(118, 56);
        }

        @Override
        public String getUid() {
            return UID;
        }

        @Override
        public String getTitle() {
            return I18n.format("jei.chestcavity.salvage_recipe");
        }

        @Override
        public String getModName() {
            return "Chest Cavity Legacy";
        }

        @Override
        public IDrawable getBackground() {
            return background;
        }

        @Override
        public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    layout.getItemStacks().init(y * 3 + x, true, 1 + x * 18, 1 + y * 18);
                }
            }
            layout.getItemStacks().init(9, false, 95, 19);
            layout.getItemStacks().set(ingredients);
        }
    }
}
