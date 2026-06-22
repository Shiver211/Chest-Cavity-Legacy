package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * 负责根据实体与配置解析其应使用的胸腔类型。
 */
public final class OrganTypeResolver {

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganTypeResolver() {
    }

    /**
     * 判断一个胸腔是否已被明确分配到具体类型。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @return `true` 表示已分配类型。
     */
    public static boolean hasAssignedType(IChestCavity chestCavity) {
        return getAssignedType(chestCavity) != null;
    }

    /**
     * 返回胸腔应实际使用的类型；未分配时回退到默认类型。
     *
     * @param chestCavity 要解析的胸腔数据。
     * @return 胸腔类型。
     */
    public static ChestCavityType getType(IChestCavity chestCavity) {
        ChestCavityType assignedType = getAssignedType(chestCavity);
        if (assignedType != null) {
            return assignedType;
        }
        return DataLoaders.getType(CCConfig.getDefaultChestCavityId());
    }

    /**
     * 返回胸腔被直接分配到的类型，不做默认回退。
     *
     * @param chestCavity 要解析的胸腔数据。
     * @return 分配到的胸腔类型；如果没有则返回 `null`。
     */
    static ChestCavityType getAssignedType(IChestCavity chestCavity) {
        if (chestCavity == null) {
            return null;
        }
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner != null) {
            ResourceLocation entityId = owner instanceof EntityPlayer
                    ? new ResourceLocation("minecraft", "player")
                    : EntityList.getKey(owner);
            String typeId = DataLoaders.getAssignedTypeId(entityId);
            if (typeId != null && DataLoaders.getTypes().containsKey(typeId)) {
                return DataLoaders.getType(typeId);
            }
        }
        return null;
    }
}
