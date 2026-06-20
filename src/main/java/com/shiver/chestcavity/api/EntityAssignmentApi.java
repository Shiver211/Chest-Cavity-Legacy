package com.shiver.chestcavity.api;

import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.util.ResourceLocation;

public final class EntityAssignmentApi {

    EntityAssignmentApi() {
    }

    public void register(ResourceLocation entityId, String typeId) {
        final ResourceLocation targetEntityId = entityId;
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetEntityId, targetTypeId));
    }

    public void unregister(ResourceLocation entityId) {
        final ResourceLocation targetEntityId = entityId;
        DataLoaders.applyRuntimeOverride(() -> DataLoaders.unregisterEntityAssignment(targetEntityId));
    }

    public String getAssignedType(ResourceLocation entityId) {
        return DataLoaders.getAssignedTypeId(entityId);
    }

    private void registerNow(ResourceLocation entityId, String typeId) {
        if (entityId != null && typeId != null && DataLoaders.isEntityPresent(entityId)) {
            DataLoaders.registerEntityAssignment(entityId, typeId);
        }
    }
}
