package com.shiver.chestcavity.script.representation;

import com.shiver.chestcavity.script.model.ScriptChestCavityTypeDefinition;
import com.shiver.chestcavity.script.model.ScriptIngredientDefinition;
import com.shiver.chestcavity.script.registry.ScriptChestCavityTypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChestCavityTypeRepresentation {

    private final ScriptChestCavityTypeDefinition definition;

    public ChestCavityTypeRepresentation(String id) {
        this(new ResourceLocation(id));
    }

    public ChestCavityTypeRepresentation(ResourceLocation id) {
        definition = new ScriptChestCavityTypeDefinition(id);
    }

    public ScriptChestCavityTypeDefinition getDefinition() {
        return definition;
    }

    public void setOrgan(int slot, ItemStack stack) {
        definition.setOrgan(slot, stack);
    }

    public void setOrgan(int slot, ItemStack stack, int count) {
        definition.setOrgan(slot, stack, count);
    }

    public void clearOrgan(int slot) {
        definition.clearOrgan(slot);
    }

    public void addBaseScore(String id, float value) {
        definition.addBaseScore(new ResourceLocation(id), value);
    }

    public void addBaseScore(ResourceLocation id, float value) {
        definition.addBaseScore(id, value);
    }

    public void addExceptionalOrgan(ScriptIngredientDefinition ingredient, String scoreId, float value) {
        definition.addExceptionalOrgan(ingredient, new ResourceLocation(scoreId), value);
    }

    public void addExceptionalOrgan(ScriptIngredientDefinition ingredient, ResourceLocation scoreId, float value) {
        definition.addExceptionalOrgan(ingredient, scoreId, value);
    }

    public void addExceptionalOrgan(ScriptIngredientDefinition ingredient, Map<ResourceLocation, Float> scores) {
        definition.addExceptionalOrgan(ingredient, new LinkedHashMap<ResourceLocation, Float>(scores));
    }

    public void forbidSlot(int slot) {
        definition.forbidSlot(slot);
    }

    public boolean isBossChestCavity() {
        return definition.isBossChestCavity();
    }

    public void setBossChestCavity(boolean bossChestCavity) {
        definition.setBossChestCavity(bossChestCavity);
    }

    public boolean isPlayerChestCavity() {
        return definition.isPlayerChestCavity();
    }

    public void setPlayerChestCavity(boolean playerChestCavity) {
        definition.setPlayerChestCavity(playerChestCavity);
    }

    public float getDropRateMultiplier() {
        return definition.getDropRateMultiplier();
    }

    public void setDropRateMultiplier(float dropRateMultiplier) {
        definition.setDropRateMultiplier(dropRateMultiplier);
    }

    public ScriptChestCavityTypeDefinition register() {
        ScriptChestCavityTypeRegistry.register(definition);
        return definition;
    }
}
