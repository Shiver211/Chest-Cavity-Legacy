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

/**
 * 提供 CraftTweaker 对象与原版对象之间的转换辅助方法。
 */
final class CrTUtil {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTUtil() {
    }

    /**
     * 把字符串形式的资源标识转换为 `ResourceLocation`。
     *
     * @param id 字符串形式的资源标识。
     * @return 转换后的资源标识；如果输入为空则返回 `null`。
     */
    static ResourceLocation id(String id) {
        return id == null || id.isEmpty() ? null : new ResourceLocation(id);
    }

    /**
     * 把 CraftTweaker 物品堆转换为原版 `ItemStack` 副本。
     *
     * @param stack CraftTweaker 物品堆。
     * @return 原版 `ItemStack` 副本。
     */
    static ItemStack stack(IItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Object internal = stack.getInternal();
        return internal instanceof ItemStack ? ((ItemStack) internal).copy() : ItemStack.EMPTY;
    }

    /**
     * 把原版 `ItemStack` 转换为 CraftTweaker 物品堆。
     *
     * @param stack 原版物品堆。
     * @return CraftTweaker 物品堆。
     */
    static IItemStack stack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return MCItemStack.EMPTY;
        }
        return new MCItemStack(stack.copy());
    }

    /**
     * 从 CraftTweaker 实体对象中提取原版活体实体。
     *
     * @param entity CraftTweaker 实体。
     * @return 原版活体实体；如果不匹配则返回 `null`。
     */
    static EntityLivingBase living(IEntity entity) {
        if (entity == null) {
            return null;
        }
        Object internal = entity.getInternal();
        return internal instanceof EntityLivingBase ? (EntityLivingBase) internal : null;
    }

    /**
     * 把原版活体实体转换为 CraftTweaker 实体包装对象。
     *
     * @param entity 原版活体实体。
     * @return CraftTweaker 实体包装对象。
     */
    static IEntityLivingBase living(EntityLivingBase entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof EntityPlayer) {
            return new MCPlayer((EntityPlayer) entity);
        }
        return new MCEntityLivingBase(entity);
    }

    /**
     * 把原版玩家实体转换为 CraftTweaker 玩家包装对象。
     *
     * @param entity 原版活体实体。
     * @return CraftTweaker 玩家包装对象；如果不是玩家则返回 `null`。
     */
    static IPlayer player(EntityLivingBase entity) {
        return entity instanceof EntityPlayer ? new MCPlayer((EntityPlayer) entity) : null;
    }

    /**
     * 从 CraftTweaker 实体对象中提取底层原版实体。
     *
     * @param entity CraftTweaker 实体。
     * @return 原版实体；如果不匹配则返回 `null`。
     */
    static Entity internalEntity(IEntity entity) {
        if (entity == null) {
            return null;
        }
        Object internal = entity.getInternal();
        return internal instanceof Entity ? (Entity) internal : null;
    }

    /**
     * 从 CraftTweaker 物品堆中读取物品注册名。
     *
     * @param item CraftTweaker 物品堆。
     * @return 物品注册名；如果物品无效则返回 `null`。
     */
    static ResourceLocation itemId(IItemStack item) {
        ItemStack internal = stack(item);
        if (internal.isEmpty()) {
            return null;
        }
        return internal.getItem().getRegistryName();
    }

    /**
     * 安全地把一个分数字典中的值统一转换为 `Float`。
     *
     * @param scores 可能混入 `Double` 的分数字典。
     * @return 值已转换为 `Float` 的新字典。
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
