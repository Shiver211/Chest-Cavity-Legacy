package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.script.model.ScriptOrganDefinition;
import com.shiver.chestcavity.script.registry.ScriptOrganRegistry;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "OrganDefinitionRepresentation")
@ZenRegister
public class OrganDefinitionRepresentation extends AbstractOrganRepresentation {

    private final IItemStack stack;

    public OrganDefinitionRepresentation(IItemStack stack) {
        this.stack = stack;
    }

    @ZenMethod
    public void register() {
        ResourceLocation itemId = CtCompatUtil.itemId(stack);
        if (itemId == null) {
            throw new IllegalArgumentException("defineOrgan 需要有效的物品");
        }
        ScriptOrganDefinition definition = new ScriptOrganDefinition(itemId, pseudoOrgan, getScoresView());
        ScriptOrganRegistry.register(definition);
        CtCompatUtil.logRegistration("Registered Chest Cavity script organ for %s", itemId);
    }
}
