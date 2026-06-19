package com.shiver.chestcavity.crt;

import crafttweaker.api.entity.IEntity;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.player.IPlayer;
import crafttweaker.mc1120.entity.MCEntityLivingBase;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.mc1120.player.MCPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

final class CrTUtil {

    private CrTUtil() {
    }

    static ResourceLocation id(String id) {
        return id == null || id.isEmpty() ? null : new ResourceLocation(id);
    }

    static ItemStack stack(IItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Object internal = stack.getInternal();
        return internal instanceof ItemStack ? ((ItemStack) internal).copy() : ItemStack.EMPTY;
    }

    static IItemStack stack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return MCItemStack.EMPTY;
        }
        return new MCItemStack(stack.copy());
    }

    static EntityLivingBase living(IEntity entity) {
        if (entity == null) {
            return null;
        }
        Object internal = entity.getInternal();
        return internal instanceof EntityLivingBase ? (EntityLivingBase) internal : null;
    }

    static IEntityLivingBase living(EntityLivingBase entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof EntityPlayer) {
            return new MCPlayer((EntityPlayer) entity);
        }
        return new MCEntityLivingBase(entity);
    }

    static IPlayer player(EntityLivingBase entity) {
        return entity instanceof EntityPlayer ? new MCPlayer((EntityPlayer) entity) : null;
    }

    static Entity internalEntity(IEntity entity) {
        if (entity == null) {
            return null;
        }
        Object internal = entity.getInternal();
        return internal instanceof Entity ? (Entity) internal : null;
    }
}
