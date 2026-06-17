package com.shiver.chestcavity.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityForcefulSpit extends EntityLlamaSpit {

    private EntityLivingBase forcefulOwner;
    private NBTTagCompound ownerNbt;

    public EntityForcefulSpit(World world) {
        super(world);
    }

    public EntityForcefulSpit(World world, EntityLivingBase owner) {
        super(world);
        this.forcefulOwner = owner;
        setPosition(owner.posX, owner.posY + owner.getEyeHeight() - 0.1D, owner.posZ);
        setSize(0.25F, 0.25F);
    }

    @Override
    public void onUpdate() {
        if (ownerNbt != null) {
            restoreOwnerFromSave();
        }
        super.onUpdate();
    }

    @Override
    public void onHit(RayTraceResult result) {
        if (result.entityHit != null && forcefulOwner != null) {
            result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, forcefulOwner).setProjectile(), 1.0F);
        }

        if (!world.isRemote) {
            setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Owner", 10)) {
            ownerNbt = compound.getCompoundTag("Owner");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (forcefulOwner != null) {
            NBTTagCompound ownerTag = new NBTTagCompound();
            ownerTag.setUniqueId("OwnerUUID", forcefulOwner.getUniqueID());
            compound.setTag("Owner", ownerTag);
        }
    }

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
