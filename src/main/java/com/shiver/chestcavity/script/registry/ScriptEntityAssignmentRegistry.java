package com.shiver.chestcavity.script.registry;

import com.shiver.chestcavity.script.model.ScriptEntityAssignment;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptEntityAssignmentRegistry {

    private static final Map<ResourceLocation, ScriptEntityAssignment> DEFINITIONS = new LinkedHashMap<ResourceLocation, ScriptEntityAssignment>();

    private ScriptEntityAssignmentRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(ScriptEntityAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("assignment cannot be null");
        }
        DEFINITIONS.put(assignment.getEntityId(), assignment);
    }

    public static void assign(ResourceLocation entityId, ResourceLocation typeId) {
        register(new ScriptEntityAssignment(entityId, typeId));
    }

    public static ScriptEntityAssignment get(ResourceLocation entityId) {
        return entityId == null ? null : DEFINITIONS.get(entityId);
    }

    public static ResourceLocation getAssignedTypeId(ResourceLocation entityId) {
        ScriptEntityAssignment assignment = get(entityId);
        return assignment == null ? null : assignment.getTypeId();
    }

    public static Map<ResourceLocation, ScriptEntityAssignment> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
