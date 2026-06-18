package com.shiver.chestcavity.script.representation;

import com.shiver.chestcavity.script.model.ScriptEntityAssignment;
import com.shiver.chestcavity.script.registry.ScriptEntityAssignmentRegistry;
import net.minecraft.util.ResourceLocation;

public class EntityAssignmentRepresentation {

    private ResourceLocation entityId;
    private ResourceLocation typeId;

    public EntityAssignmentRepresentation(String entityId, String typeId) {
        this(new ResourceLocation(entityId), new ResourceLocation(typeId));
    }

    public EntityAssignmentRepresentation(ResourceLocation entityId, ResourceLocation typeId) {
        this.entityId = entityId;
        this.typeId = typeId;
    }

    public ResourceLocation getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = new ResourceLocation(entityId);
    }

    public void setEntityId(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    public ResourceLocation getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = new ResourceLocation(typeId);
    }

    public void setTypeId(ResourceLocation typeId) {
        this.typeId = typeId;
    }

    public ScriptEntityAssignment build() {
        return new ScriptEntityAssignment(entityId, typeId);
    }

    public ScriptEntityAssignment register() {
        ScriptEntityAssignment assignment = build();
        ScriptEntityAssignmentRegistry.register(assignment);
        return assignment;
    }
}
