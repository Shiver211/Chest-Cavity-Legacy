package com.shiver.chestcavity.effect;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public final class BodyEventContext {

    private final EntityLivingBase entity;
    private final ChestCavityData chestCavity;
    private final ChestCavityRuntime runtime;

    public BodyEventContext(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        this.entity = entity;
        this.chestCavity = chestCavity;
        this.runtime = runtime;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public ChestCavityData getChestCavity() {
        return chestCavity;
    }

    public ChestCavityRuntime getRuntime() {
        return runtime;
    }

    public EntityPlayer getPlayerOrNull() {
        return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
    }
}
