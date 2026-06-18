package com.shiver.chestcavity.script.registry;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.script.model.ScriptOrganDefinition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptOrganRegistry {

    private static final Map<ResourceLocation, ScriptOrganDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScriptOrganDefinition>();

    private ScriptOrganRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScriptOrganDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getItemId(), definition);
    }

    public static ScriptOrganDefinition get(ResourceLocation itemId) {
        return itemId == null ? null : DEFINITIONS.get(itemId);
    }

    public static ScriptOrganDefinition get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        return get(stack.getItem().getRegistryName());
    }

    public static OrganData getOrganData(ItemStack stack) {
        ScriptOrganDefinition definition = get(stack);
        return definition == null ? null : definition.toOrganData();
    }

    public static Map<ResourceLocation, ScriptOrganDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
