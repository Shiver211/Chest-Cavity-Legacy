package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
import com.shiver.chestcavity.integration.crafttweaker.runtime.OrganDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.OrganRegistry;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@ZenClass(CtConstants.CT_NAMESPACE + "OrganDefinitionRepresentation")
@ZenRegister
public class OrganDefinitionRepresentation {

    private final IItemStack stack;

    @ZenProperty
    public boolean pseudoOrgan;

    private final Map<ResourceLocation, Float> organScores = new LinkedHashMap<ResourceLocation, Float>();

    public OrganDefinitionRepresentation(IItemStack stack) {
        this.stack = stack;
    }

    @ZenMethod
    public void addScore(String id, float value) {
        organScores.put(CtUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void bindAbility(String id, float value) {
        organScores.put(CtUtil.requireId(id, "ability"), value);
    }

    @ZenMethod
    public void register() {
        ResourceLocation itemId = CtUtil.itemId(stack);
        if (itemId == null) {
            throw new IllegalArgumentException("defineOrgan 需要有效的物品");
        }
        OrganRegistry.register(new OrganDefinition(itemId, pseudoOrgan, organScores));
        CtUtil.logRegistration("Registered Chest Cavity script organ for %s", itemId);
    }
}
