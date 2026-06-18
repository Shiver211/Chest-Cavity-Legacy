package com.shiver.chestcavity.script.registry;

import com.shiver.chestcavity.script.model.ScriptAbilityDefinition;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptAbilityRegistry {

    private static final Map<ResourceLocation, ScriptAbilityDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScriptAbilityDefinition>();

    private ScriptAbilityRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScriptAbilityDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static ScriptAbilityDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, ScriptAbilityDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
