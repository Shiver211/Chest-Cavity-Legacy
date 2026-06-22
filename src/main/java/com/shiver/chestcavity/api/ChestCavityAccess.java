package com.shiver.chestcavity.api;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * 提供从实体安全获取胸腔视图的外部访问入口。
 */
public final class ChestCavityAccess {

    /**
     * 仅允许通过统一 API 单例创建。
     */
    ChestCavityAccess() {
    }

    /**
     * 从任意实体获取胸腔视图。
     *
     * @param entity 要查询的实体。
     * @return 胸腔视图；如果实体不支持则返回 `null`。
     */
    public ChestCavityView get(Entity entity) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        return chestCavity == null ? null : new ChestCavityView(chestCavity);
    }

    /**
     * 从活体实体获取胸腔视图。
     *
     * @param entity 要查询的实体。
     * @return 胸腔视图；如果实体不支持则返回 `null`。
     */
    public ChestCavityView get(EntityLivingBase entity) {
        return get((Entity) entity);
    }
}
