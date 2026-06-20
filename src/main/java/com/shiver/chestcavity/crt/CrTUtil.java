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

import java.util.LinkedHashMap;
import java.util.Map;

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

    static ResourceLocation itemId(IItemStack item) {
        ItemStack internal = stack(item);
        if (internal.isEmpty()) {
            return null;
        }
        return internal.getItem().getRegistryName();
    }

    /**
     * Safely converts a Map's values to Float.
     * ZenScript decimal literals default to double, so CrT2's type converter
     * may pass Double values in a Map declared as Map&lt;String, Float&gt;.
     * This method handles the conversion via Number.floatValue().
     */
    static Map<String, Float> ensureFloatMap(Map<String, Float> scores) {
        if (scores == null || scores.isEmpty()) {
            return scores;
        }
        Map<String, Float> result = new LinkedHashMap<String, Float>();
        for (Map.Entry<String, ?> entry : ((Map<String, ?>) scores).entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Number) {
                result.put(entry.getKey(), ((Number) val).floatValue());
            }
        }
        return result;
    }
}
