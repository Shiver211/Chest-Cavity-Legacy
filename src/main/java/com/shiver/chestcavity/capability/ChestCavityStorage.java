package com.shiver.chestcavity.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class ChestCavityStorage implements Capability.IStorage<IChestCavity> {

    @Override
    public NBTBase writeNBT(Capability<IChestCavity> capability, IChestCavity instance, EnumFacing side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IChestCavity> capability, IChestCavity instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
