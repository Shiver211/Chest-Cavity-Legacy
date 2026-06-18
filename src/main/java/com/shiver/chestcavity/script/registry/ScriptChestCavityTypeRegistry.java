package com.shiver.chestcavity.script.registry;

import com.shiver.chestcavity.script.model.ScriptChestCavityTypeDefinition;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptChestCavityTypeRegistry {

    private static final Map<ResourceLocation, ScriptChestCavityTypeDefinition> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScriptChestCavityTypeDefinition>();

    private ScriptChestCavityTypeRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScriptChestCavityTypeDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static ScriptChestCavityTypeDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, ScriptChestCavityTypeDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
