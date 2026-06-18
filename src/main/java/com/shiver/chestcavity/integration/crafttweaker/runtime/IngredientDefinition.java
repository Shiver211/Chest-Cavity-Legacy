package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientDefinition {

    private final ResourceLocation itemId;
    private final String oreName;

    private IngredientDefinition(ResourceLocation itemId, String oreName) {
        this.itemId = itemId;
        this.oreName = oreName;
    }

    public static IngredientDefinition ofItem(ResourceLocation itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        return new IngredientDefinition(itemId, null);
    }

    public static IngredientDefinition ofOre(String oreName) {
        if (oreName == null || oreName.isEmpty()) {
            throw new IllegalArgumentException("oreName cannot be empty");
        }
        return new IngredientDefinition(null, oreName);
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
