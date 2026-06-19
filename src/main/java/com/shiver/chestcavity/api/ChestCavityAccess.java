package com.shiver.chestcavity.api;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public final class ChestCavityAccess {

    ChestCavityAccess() {
    }

    public ChestCavityView get(Entity entity) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        return chestCavity == null ? null : new ChestCavityView(chestCavity);
    }

    public ChestCavityView get(EntityLivingBase entity) {
        return get((Entity) entity);
    }
}
