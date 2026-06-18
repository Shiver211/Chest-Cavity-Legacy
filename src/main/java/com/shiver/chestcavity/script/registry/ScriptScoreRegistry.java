package com.shiver.chestcavity.script.registry;

import com.shiver.chestcavity.script.model.ScriptScoreDefinition;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptScoreRegistry {

    private static final Map<ResourceLocation, ScriptScoreDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScriptScoreDefinition>();

    private ScriptScoreRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScriptScoreDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static ScriptScoreDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, ScriptScoreDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
