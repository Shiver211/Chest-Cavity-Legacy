package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

public class EntityAssignment {

    private final ResourceLocation entityId;
    private final ResourceLocation typeId;

    public EntityAssignment(ResourceLocation entityId, ResourceLocation typeId) {
        if (entityId == null) {
            throw new IllegalArgumentException("entityId cannot be null");
        }
        if (typeId == null) {
            throw new IllegalArgumentException("typeId cannot be null");
        }
        this.entityId = entityId;
        this.typeId = typeId;
    }

    public ResourceLocation getEntityId() {
        return entityId;
    }

    public ResourceLocation getTypeId() {
        return typeId;
    }
}
