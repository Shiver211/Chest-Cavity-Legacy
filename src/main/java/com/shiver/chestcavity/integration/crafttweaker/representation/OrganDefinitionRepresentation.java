package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.integration.crafttweaker.callback.OrganCallbacks;
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

    @ZenProperty
    public OrganCallbacks.CanInsert canInsert;

    @ZenProperty
    public OrganCallbacks.CanRemove canRemove;

    @ZenProperty
    public OrganCallbacks.OnInserted onInserted;

    @ZenProperty
    public OrganCallbacks.OnRemoved onRemoved;

    @ZenProperty
    public OrganCallbacks.OnTick onTick;

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
        OrganRegistry.register(new OrganDefinition(itemId, pseudoOrgan, organScores, canInsert, canRemove, onInserted, onRemoved, onTick));
        CtUtil.logRegistration("Registered Chest Cavity script organ for %s", itemId);
    }
}
