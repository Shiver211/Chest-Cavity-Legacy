package com.shiver.chestcavity.script.representation;

import com.shiver.chestcavity.script.model.ScriptOrganDefinition;
import com.shiver.chestcavity.script.registry.ScriptOrganRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrganDefinitionRepresentation {

    private final ResourceLocation itemId;
    private boolean pseudoOrgan;
    private final Map<ResourceLocation, Float> organScores = new LinkedHashMap<ResourceLocation, Float>();

    public OrganDefinitionRepresentation(ItemStack stack) {
        this(resolveItemId(stack));
    }

    public OrganDefinitionRepresentation(ResourceLocation itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        this.itemId = itemId;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    public void setPseudoOrgan(boolean pseudoOrgan) {
        this.pseudoOrgan = pseudoOrgan;
    }

    public void addScore(String id, float value) {
        organScores.put(new ResourceLocation(id), value);
    }

    public void addScore(ResourceLocation id, float value) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        organScores.put(id, value);
    }

    public void bindAbility(String id, float value) {
        addScore(id, value);
    }

    public void bindAbility(ResourceLocation id, float value) {
        addScore(id, value);
    }

    public ScriptOrganDefinition build() {
        return new ScriptOrganDefinition(itemId, pseudoOrgan, organScores);
    }

    public ScriptOrganDefinition register() {
        ScriptOrganDefinition definition = build();
        ScriptOrganRegistry.register(definition);
        return definition;
    }

    private static ResourceLocation resolveItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            throw new IllegalArgumentException("stack must contain a registered item");
        }
        return stack.getItem().getRegistryName();
    }
}
