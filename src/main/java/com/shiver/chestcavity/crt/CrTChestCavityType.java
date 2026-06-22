package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

/**
 * 向 ZenScript 暴露胸腔类型管理接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.ChestCavityType")
public final class CrTChestCavityType {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTChestCavityType() {
    }

    /**
     * 注册一个新的胸腔类型。
     *
     * @param typeId 类型标识。
     */
    @ZenMethod
    public static void register(String typeId) {
        ChestCavityApis.TYPES.register(typeId);
    }

    /**
     * 为胸腔类型添加一项基础分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    @ZenMethod
    public static void addBaseScore(String typeId, String scoreId, float value) {
        ChestCavityApis.TYPES.addBaseScore(typeId, scoreId, value);
    }

    /**
     * 移除胸腔类型中的一项基础分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     */
    @ZenMethod
    public static void removeBaseScore(String typeId, String scoreId) {
        ChestCavityApis.TYPES.removeBaseScore(typeId, scoreId);
    }

    /**
     * 设置胸腔类型中某个默认槽位的物品。
     *
     * @param typeId 类型标识。
     * @param index 槽位索引。
     * @param stack 要放入的物品。
     */
    @ZenMethod
    public static void setSlot(String typeId, int index, IItemStack stack) {
        ChestCavityApis.TYPES.setSlot(typeId, index, CrTUtil.stack(stack));
    }

    /**
     * 清空胸腔类型中的默认槽位布局。
     *
     * @param typeId 类型标识。
     */
    @ZenMethod
    public static void clearSlots(String typeId) {
        ChestCavityApis.TYPES.clearSlots(typeId);
    }

    /**
     * 增加一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    @ZenMethod
    public static void addForbiddenSlot(String typeId, int slot) {
        ChestCavityApis.TYPES.addForbiddenSlot(typeId, slot);
    }

    /**
     * 移除一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    @ZenMethod
    public static void removeForbiddenSlot(String typeId, int slot) {
        ChestCavityApis.TYPES.removeForbiddenSlot(typeId, slot);
    }

    /**
     * 设置胸腔类型的器官掉率倍率。
     *
     * @param typeId 类型标识。
     * @param value 掉率倍率。
     */
    @ZenMethod
    public static void setDropRateMultiplier(String typeId, float value) {
        ChestCavityApis.TYPES.setDropRateMultiplier(typeId, value);
    }

    /**
     * 设置胸腔类型是否为 Boss 胸腔。
     *
     * @param typeId 类型标识。
     * @param value 是否为 Boss 胸腔。
     */
    @ZenMethod
    public static void setBossChestCavity(String typeId, boolean value) {
        ChestCavityApis.TYPES.setBossChestCavity(typeId, value);
    }

    /**
     * 设置胸腔类型是否为玩家胸腔。
     *
     * @param typeId 类型标识。
     * @param value 是否为玩家胸腔。
     */
    @ZenMethod
    public static void setPlayerChestCavity(String typeId, boolean value) {
        ChestCavityApis.TYPES.setPlayerChestCavity(typeId, value);
    }

    /**
     * 添加一条按物品匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param item 物品。
     * @param scores 特殊分数字典。
     */
    @ZenMethod
    public static void addExceptionalOrgan(String typeId, IItemStack item, Map<String, Float> scores) {
        ItemStack stack = CrTUtil.stack(item);
        ChestCavityApis.TYPES.addExceptionalOrgan(typeId, stack.isEmpty() ? null : stack.getItem(), CrTUtil.ensureFloatMap(scores));
    }

    /**
     * 添加一条按矿辞匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param oreName 矿辞名称。
     * @param scores 特殊分数字典。
     */
    @ZenMethod
    public static void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        ChestCavityApis.TYPES.addExceptionalOrganByOre(typeId, oreName, CrTUtil.ensureFloatMap(scores));
    }

    /**
     * 清空胸腔类型中的全部特殊器官规则。
     *
     * @param typeId 类型标识。
     */
    @ZenMethod
    public static void clearExceptionalOrgans(String typeId) {
        ChestCavityApis.TYPES.clearExceptionalOrgans(typeId);
    }

    /**
     * 判断胸腔类型是否为 Boss 胸腔。
     *
     * @param typeId 类型标识。
     * @return `true` 表示是 Boss 胸腔。
     */
    @ZenMethod
    public static boolean isBossChestCavity(String typeId) {
        return ChestCavityApis.TYPES.isBossChestCavity(typeId);
    }

    /**
     * 判断胸腔类型是否为玩家胸腔。
     *
     * @param typeId 类型标识。
     * @return `true` 表示是玩家胸腔。
     */
    @ZenMethod
    public static boolean isPlayerChestCavity(String typeId) {
        return ChestCavityApis.TYPES.isPlayerChestCavity(typeId);
    }

    /**
     * 返回胸腔类型的器官掉率倍率。
     *
     * @param typeId 类型标识。
     * @return 器官掉率倍率。
     */
    @ZenMethod
    public static float getDropRateMultiplier(String typeId) {
        return ChestCavityApis.TYPES.getDropRateMultiplier(typeId);
    }
}
