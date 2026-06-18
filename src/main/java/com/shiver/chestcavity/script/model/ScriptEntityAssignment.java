package com.shiver.chestcavity.script.model;

import net.minecraft.util.ResourceLocation;

public class ScriptEntityAssignment {

    private final ResourceLocation entityId;
    private final ResourceLocation typeId;

    public ScriptEntityAssignment(ResourceLocation entityId, ResourceLocation typeId) {
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
