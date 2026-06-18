package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ChestCavityTypeRegistry;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ChestCavityTypeDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
import com.shiver.chestcavity.integration.crafttweaker.runtime.IngredientDefinition;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass(CtConstants.CT_NAMESPACE + "ChestCavityTypeRepresentation")
@ZenRegister
public class ChestCavityTypeRepresentation {

    private final ChestCavityTypeDefinition definition;

    @ZenProperty
    public boolean bossChestCavity;

    @ZenProperty
    public boolean playerChestCavity;

    @ZenProperty
    public float dropRateMultiplier = 1.0F;

    public ChestCavityTypeRepresentation(String id) {
        this.definition = new ChestCavityTypeDefinition(CtUtil.requireId(id, "type"));
    }

    @ZenMethod
    public void setOrgan(int slot, IItemStack stack) {
        CtUtil.validateSlot(slot);
        definition.setOrgan(slot, CtUtil.toMcStack(stack));
    }

    @ZenMethod
    public void setOrgan(int slot, IItemStack stack, int count) {
        CtUtil.validateSlot(slot);
        ItemStack mcStack = CtUtil.toMcStack(stack);
        CtUtil.validateCount(count, mcStack);
        definition.setOrgan(slot, mcStack, count);
    }

    @ZenMethod
    public void clearOrgan(int slot) {
        CtUtil.validateSlot(slot);
        definition.clearOrgan(slot);
    }

    @ZenMethod
    public void addBaseScore(String id, float value) {
        definition.addBaseScore(CtUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void addExceptionalOrgan(IIngredient ingredient, String scoreId, float value) {
        definition.addExceptionalOrgan(toScriptIngredient(ingredient), CtUtil.requireId(scoreId, "score"), value);
    }

    @ZenMethod
    public void forbidSlot(int slot) {
        CtUtil.validateSlot(slot);
        definition.forbidSlot(slot);
    }

    @ZenMethod
    public void register() {
        definition.setBossChestCavity(bossChestCavity);
        definition.setPlayerChestCavity(playerChestCavity);
        definition.setDropRateMultiplier(dropRateMultiplier);
        ChestCavityTypeRegistry.register(definition);
        CtUtil.logRegistration("Registered Chest Cavity script type %s", definition.getId());
    }

    private IngredientDefinition toScriptIngredient(IIngredient ingredient) {
        Object internal = CtUtil.unwrapIngredient(ingredient);
        if (internal instanceof Ingredient) {
            ItemStack[] matchingStacks = ((Ingredient) internal).getMatchingStacks();
            if (matchingStacks.length > 0 && !matchingStacks[0].isEmpty()) {
                return IngredientDefinition.ofItem(matchingStacks[0].getItem().getRegistryName());
            }
        }
        ItemStack[] stacks = CraftTweakerMC.getExamples(ingredient);
        if (stacks.length > 0 && !stacks[0].isEmpty()) {
            return IngredientDefinition.ofItem(stacks[0].getItem().getRegistryName());
        }
        throw new IllegalArgumentException("当前 exceptional organ 需要至少一个可解析的物品示例");
    }
}
