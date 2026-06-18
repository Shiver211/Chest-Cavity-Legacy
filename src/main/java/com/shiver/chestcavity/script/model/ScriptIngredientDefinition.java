package com.shiver.chestcavity.script.model;

import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptIngredientDefinition {

    private final ResourceLocation itemId;
    private final String oreName;

    private ScriptIngredientDefinition(ResourceLocation itemId, String oreName) {
        this.itemId = itemId;
        this.oreName = oreName;
    }

    public static ScriptIngredientDefinition ofItem(ResourceLocation itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        return new ScriptIngredientDefinition(itemId, null);
    }

    public static ScriptIngredientDefinition ofOre(String oreName) {
        if (oreName == null || oreName.isEmpty()) {
            throw new IllegalArgumentException("oreName cannot be empty");
        }
        return new ScriptIngredientDefinition(null, oreName);
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public String getOreName() {
        return oreName;
    }

    public boolean isItemIngredient() {
        return itemId != null;
    }

    public boolean isOreIngredient() {
        return oreName != null && !oreName.isEmpty();
    }

    public GeneratedChestCavityType.ExceptionalOrgan toExceptionalOrgan(Map<ResourceLocation, Float> scores) {
        Map<ResourceLocation, Float> copiedScores = new LinkedHashMap<ResourceLocation, Float>();
        if (scores != null) {
            copiedScores.putAll(scores);
        }

        Item item = null;
        if (itemId != null) {
            item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                throw new IllegalStateException("Item is not registered: " + itemId);
            }
        }
        return new GeneratedChestCavityType.ExceptionalOrgan(item, oreName, copiedScores);
    }
}
