package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OrganRegistry {

    private static final Map<ResourceLocation, OrganDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, OrganDefinition>();

    private OrganRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(OrganDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getItemId(), definition);
    }

    public static OrganDefinition get(ResourceLocation itemId) {
        return itemId == null ? null : DEFINITIONS.get(itemId);
    }

    public static OrganDefinition get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        return get(stack.getItem().getRegistryName());
    }

    public static OrganData getOrganData(ItemStack stack) {
        OrganDefinition definition = get(stack);
        return definition == null ? null : definition.toOrganData();
    }

    public static Map<ResourceLocation, OrganDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
