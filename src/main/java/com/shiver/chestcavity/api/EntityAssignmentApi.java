package com.shiver.chestcavity.api;

import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.util.ResourceLocation;

/**
 * 对外暴露实体到胸腔类型的分配接口。
 */
public final class EntityAssignmentApi {

    /**
     * 仅允许通过统一 API 单例创建。
     */
    EntityAssignmentApi() {
    }

    /**
     * 为指定实体注册一个胸腔类型分配。
     *
     * @param entityId 实体注册名。
     * @param typeId 胸腔类型标识。
     */
    public void register(ResourceLocation entityId, String typeId) {
        final ResourceLocation targetEntityId = entityId;
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetEntityId, targetTypeId));
    }

    /**
     * 移除指定实体的胸腔类型分配。
     *
     * @param entityId 实体注册名。
     */
    public void unregister(ResourceLocation entityId) {
        final ResourceLocation targetEntityId = entityId;
        DataLoaders.applyRuntimeOverride(() -> DataLoaders.unregisterEntityAssignment(targetEntityId));
    }

    /**
     * 查询指定实体当前被分配到的胸腔类型。
     *
     * @param entityId 实体注册名。
     * @return 胸腔类型标识；如果未分配则返回 `null`。
     */
    public String getAssignedType(ResourceLocation entityId) {
        return DataLoaders.getAssignedTypeId(entityId);
    }

    /**
     * 在运行时数据环境中立即注册实体分配关系。
     *
     * @param entityId 实体注册名。
     * @param typeId 胸腔类型标识。
     */
    private void registerNow(ResourceLocation entityId, String typeId) {
        if (entityId != null && typeId != null && DataLoaders.isEntityPresent(entityId)) {
            DataLoaders.registerEntityAssignment(entityId, typeId);
        }
    }
}
