package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScoreRegistry {

    private static final Map<ResourceLocation, ScoreDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScoreDefinition>();

    private ScoreRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScoreDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static ScoreDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, ScoreDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
