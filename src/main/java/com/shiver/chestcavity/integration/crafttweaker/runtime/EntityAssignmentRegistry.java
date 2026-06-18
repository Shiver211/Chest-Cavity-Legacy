package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityAssignmentRegistry {

    private static final Map<ResourceLocation, EntityAssignment> DEFINITIONS = new LinkedHashMap<ResourceLocation, EntityAssignment>();

    private EntityAssignmentRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(EntityAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("assignment cannot be null");
        }
        DEFINITIONS.put(assignment.getEntityId(), assignment);
    }

    public static void assign(ResourceLocation entityId, ResourceLocation typeId) {
        register(new EntityAssignment(entityId, typeId));
    }

    public static EntityAssignment get(ResourceLocation entityId) {
        return entityId == null ? null : DEFINITIONS.get(entityId);
    }

    public static ResourceLocation getAssignedTypeId(ResourceLocation entityId) {
        EntityAssignment assignment = get(entityId);
        return assignment == null ? null : assignment.getTypeId();
    }

    public static Map<ResourceLocation, EntityAssignment> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }
}
