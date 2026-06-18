package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChestCavityTypeRegistry {

    private static final Map<ResourceLocation, ChestCavityTypeDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ChestCavityTypeDefinition>();

    private ChestCavityTypeRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ChestCavityTypeDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static ChestCavityTypeDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, ChestCavityTypeDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
