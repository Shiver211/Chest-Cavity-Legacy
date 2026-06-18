package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import com.shiver.chestcavity.script.model.ScriptChestCavityTypeDefinition;
import com.shiver.chestcavity.script.model.ScriptIngredientDefinition;
import com.shiver.chestcavity.script.registry.ScriptChestCavityTypeRegistry;
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

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "ChestCavityTypeRepresentation")
@ZenRegister
public class ChestCavityTypeRepresentation {

    private final ScriptChestCavityTypeDefinition definition;

    @ZenProperty
    public boolean bossChestCavity;

    @ZenProperty
    public boolean playerChestCavity;

    @ZenProperty
    public float dropRateMultiplier = 1.0F;

    public ChestCavityTypeRepresentation(String id) {
        this.definition = new ScriptChestCavityTypeDefinition(CtCompatUtil.requireId(id, "type"));
    }

    @ZenMethod
    public void setOrgan(int slot, IItemStack stack) {
        CtCompatUtil.validateSlot(slot);
        definition.setOrgan(slot, CtCompatUtil.toMcStack(stack));
    }

    @ZenMethod
    public void setOrgan(int slot, IItemStack stack, int count) {
        CtCompatUtil.validateSlot(slot);
        ItemStack mcStack = CtCompatUtil.toMcStack(stack);
        CtCompatUtil.validateCount(count, mcStack);
        definition.setOrgan(slot, mcStack, count);
    }

    @ZenMethod
    public void clearOrgan(int slot) {
        CtCompatUtil.validateSlot(slot);
        definition.clearOrgan(slot);
    }

    @ZenMethod
    public void addBaseScore(String id, float value) {
        definition.addBaseScore(CtCompatUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void addExceptionalOrgan(IIngredient ingredient, String scoreId, float value) {
        definition.addExceptionalOrgan(toScriptIngredient(ingredient), CtCompatUtil.requireId(scoreId, "score"), value);
    }

    @ZenMethod
    public void forbidSlot(int slot) {
        CtCompatUtil.validateSlot(slot);
        definition.forbidSlot(slot);
    }

    @ZenMethod
    public void register() {
        definition.setBossChestCavity(bossChestCavity);
        definition.setPlayerChestCavity(playerChestCavity);
        definition.setDropRateMultiplier(dropRateMultiplier);
        ScriptChestCavityTypeRegistry.register(definition);
        CtCompatUtil.logRegistration("Registered Chest Cavity script type %s", definition.getId());
    }

    public ResourceLocation getId() {
        return definition.getId();
    }

    private ScriptIngredientDefinition toScriptIngredient(IIngredient ingredient) {
        Object internal = CtCompatUtil.unwrapIngredient(ingredient);
        if (internal instanceof Ingredient) {
            ItemStack[] matchingStacks = ((Ingredient) internal).getMatchingStacks();
            if (matchingStacks.length > 0 && !matchingStacks[0].isEmpty()) {
                return ScriptIngredientDefinition.ofItem(matchingStacks[0].getItem().getRegistryName());
            }
        }
        ItemStack[] stacks = CraftTweakerMC.getExamples(ingredient);
        if (stacks.length > 0 && !stacks[0].isEmpty()) {
            return ScriptIngredientDefinition.ofItem(stacks[0].getItem().getRegistryName());
        }
        throw new IllegalArgumentException("当前 exceptional organ 需要至少一个可解析的物品示例");
    }
}
