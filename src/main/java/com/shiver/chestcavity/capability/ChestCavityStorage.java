package com.shiver.chestcavity.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * 负责胸腔能力与 NBT 之间的读写转换。
 */
public class ChestCavityStorage implements Capability.IStorage<IChestCavity> {

    /**
     * 将胸腔能力写出为 NBT。
     *
     * @param capability 当前写出的能力类型。
     * @param instance 要序列化的胸腔实例。
     * @param side 访问方向。
     * @return 写出的 NBT 数据。
     */
    @Override
    public NBTBase writeNBT(Capability<IChestCavity> capability, IChestCavity instance, EnumFacing side) {
        return instance.serializeNBT();
    }

    /**
     * 从 NBT 数据恢复胸腔能力实例。
     *
     * @param capability 当前读取的能力类型。
     * @param instance 要恢复的胸腔实例。
     * @param side 访问方向。
     * @param nbt 读取到的 NBT 数据。
     */
    @Override
    public void readNBT(Capability<IChestCavity> capability, IChestCavity instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
