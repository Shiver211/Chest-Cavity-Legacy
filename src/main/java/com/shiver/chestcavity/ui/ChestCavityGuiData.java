package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ChestCavityGuiData extends GuiData {

    private final int targetEntityId;

    public ChestCavityGuiData(EntityPlayer player, int targetEntityId) {
        super(player);
        this.targetEntityId = targetEntityId;
    }

    public int getTargetEntityId() {
        return targetEntityId;
    }

    public EntityLivingBase getTarget() {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        Entity entity = world.getEntityByID(targetEntityId);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }
}
