package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityRegistry {

    private static final Map<ResourceLocation, AbilityDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, AbilityDefinition>();

    private AbilityRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(AbilityDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static AbilityDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, AbilityDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
