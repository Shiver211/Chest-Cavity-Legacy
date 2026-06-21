package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public final class OrganTypeResolver {

    private OrganTypeResolver() {
    }

    public static boolean hasAssignedType(IChestCavity chestCavity) {
        return getAssignedType(chestCavity) != null;
    }

    public static ChestCavityType getType(IChestCavity chestCavity) {
        ChestCavityType assignedType = getAssignedType(chestCavity);
        if (assignedType != null) {
            return assignedType;
        }
        return DataLoaders.getType(CCConfig.getDefaultChestCavityId());
    }

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
