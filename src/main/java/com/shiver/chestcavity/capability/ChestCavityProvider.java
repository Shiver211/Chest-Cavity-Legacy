package com.shiver.chestcavity.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ChestCavityProvider implements ICapabilitySerializable<NBTTagCompound> {

    private final ChestCavityData instance;

    public ChestCavityProvider(EntityLivingBase owner) {
        ChestCavityCapability.ensureRegistered();
        instance = new ChestCavityData();
        instance.setOwner(owner);
        ChestCavityMutations.initialize(instance, owner.getUniqueID());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ChestCavityCapability.CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == ChestCavityCapability.CAPABILITY) {
            return ChestCavityCapability.CAPABILITY.cast(instance);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        instance.deserializeNBT(nbt);
    }
}
