package com.shiver.chestcavity.api;

import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.util.ResourceLocation;

public final class EntityAssignmentApi {

    EntityAssignmentApi() {
    }

    public void register(ResourceLocation entityId, String typeId) {
        if (entityId != null && typeId != null && DataLoaders.isEntityPresent(entityId)) {
            DataLoaders.registerEntityAssignment(entityId, typeId);
        }
    }

    public void unregister(ResourceLocation entityId) {
        DataLoaders.unregisterEntityAssignment(entityId);
    }

    public String getAssignedType(ResourceLocation entityId) {
        return DataLoaders.getAssignedTypeId(entityId);
    }
}
