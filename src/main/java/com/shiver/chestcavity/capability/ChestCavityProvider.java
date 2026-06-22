package com.shiver.chestcavity.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/**
 * 为实体提供胸腔能力实例，并负责其 NBT 序列化接入。
 */
public class ChestCavityProvider implements ICapabilitySerializable<NBTTagCompound> {

    private final IChestCavity instance;

    /**
     * 为指定实体创建胸腔能力提供器。
     *
     * @param owner 持有该能力的实体。
     */
    public ChestCavityProvider(EntityLivingBase owner) {
        ChestCavityCapability.ensureRegistered();
        instance = new ChestCavityData();
        instance.setOwner(owner);
        instance.setCompatibilityId(owner.getUniqueID());
        instance.copyCurrentScoresToOld();
    }

    /**
     * 判断当前查询的能力是否为胸腔能力。
     *
     * @param capability 被查询的能力类型。
     * @param facing 查询方向。
     * @return `true` 表示支持该能力。
     */
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ChestCavityCapability.CAPABILITY;
    }

    /**
     * 返回胸腔能力实例。
     *
     * @param capability 被查询的能力类型。
     * @param facing 查询方向。
     * @param <T> 能力泛型类型。
     * @return 胸腔能力实例；如果能力不匹配则返回 `null`。
     */
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == ChestCavityCapability.CAPABILITY) {
            return ChestCavityCapability.CAPABILITY.cast(instance);
        }
        return null;
    }

    /**
     * 将当前胸腔能力实例序列化为 NBT。
     *
     * @return 序列化后的 NBT 数据。
     */
    @Override
    public NBTTagCompound serializeNBT() {
        return instance.serializeNBT();
    }

    /**
     * 使用 NBT 数据恢复当前胸腔能力实例。
     *
     * @param nbt 序列化后的 NBT 数据。
     */
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        instance.deserializeNBT(nbt);
    }
}
