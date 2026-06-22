package com.shiver.chestcavity.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * 强力吐息能力生成的自定义投射物实体。
 */
public class EntityForcefulSpit extends EntityLlamaSpit {

    private EntityLivingBase forcefulOwner;
    private NBTTagCompound ownerNbt;

    /**
     * 创建一个空的强力吐息实体，供反序列化使用。
     *
     * @param world 实体所在世界。
     */
    public EntityForcefulSpit(World world) {
        super(world);
    }

    /**
     * 创建一个带拥有者的强力吐息实体。
     *
     * @param world 实体所在世界。
     * @param owner 发射吐息的拥有者。
     */
    public EntityForcefulSpit(World world, EntityLivingBase owner) {
        super(world);
        this.forcefulOwner = owner;
        setPosition(owner.posX, owner.posY + owner.getEyeHeight() - 0.1D, owner.posZ);
        setSize(0.25F, 0.25F);
    }

    /**
     * 每 tick 更新实体，并在需要时恢复拥有者引用。
     */
    @Override
    public void onUpdate() {
        if (ownerNbt != null) {
            restoreOwnerFromSave();
        }
        super.onUpdate();
    }

    /**
     * 处理吐息命中后的伤害与销毁逻辑。
     *
     * @param result 命中结果。
     */
    @Override
    public void onHit(RayTraceResult result) {
        if (result.entityHit != null && forcefulOwner != null) {
            result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, forcefulOwner).setProjectile(), 1.0F);
        }

        if (!world.isRemote) {
            setDead();
        }
    }

    /**
     * 从 NBT 中读取吐息实体数据。
     *
     * @param compound 序列化后的实体数据。
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Owner", 10)) {
            ownerNbt = compound.getCompoundTag("Owner");
        }
    }

    /**
     * 将吐息实体数据写入 NBT。
     *
     * @param compound 要写入的 NBT。
     */
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (forcefulOwner != null) {
            NBTTagCompound ownerTag = new NBTTagCompound();
            ownerTag.setUniqueId("OwnerUUID", forcefulOwner.getUniqueID());
            compound.setTag("Owner", ownerTag);
        }
    }

    /**
     * 根据保存的 UUID 在世界中重新绑定吐息拥有者。
     */
    private void restoreOwnerFromSave() {
        if (ownerNbt != null && ownerNbt.hasUniqueId("OwnerUUID")) {
            UUID uuid = ownerNbt.getUniqueId("OwnerUUID");
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityLivingBase && entity.getUniqueID().equals(uuid)) {
                    forcefulOwner = (EntityLivingBase) entity;
                    break;
                }
            }
        }
        ownerNbt = null;
    }
}
