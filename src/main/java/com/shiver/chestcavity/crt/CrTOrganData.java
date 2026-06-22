package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.api.OrganDataView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

/**
 * 向 ZenScript 暴露器官数据注册与修改接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.OrganData")
public final class CrTOrganData {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTOrganData() {
    }

    /**
     * 注册一个普通器官数据定义。
     *
     * @param item 目标物品。
     * @param scores 器官分数字典。
     */
    @ZenMethod
    public static void register(IItemStack item, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.register(CrTUtil.itemId(item), CrTUtil.ensureFloatMap(scores));
    }

    /**
     * 注册一个伪器官数据定义。
     *
     * @param item 目标物品。
     * @param scores 器官分数字典。
     */
    @ZenMethod
    public static void registerPseudo(IItemStack item, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.registerPseudo(CrTUtil.itemId(item), CrTUtil.ensureFloatMap(scores));
    }

    /**
     * 为器官数据添加或覆盖一项分数。
     *
     * @param item 目标物品。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    @ZenMethod
    public static void addScore(IItemStack item, String scoreId, float value) {
        ChestCavityApis.ORGANS.addScore(CrTUtil.itemId(item), scoreId, value);
    }

    /**
     * 移除器官数据中的一项分数。
     *
     * @param item 目标物品。
     * @param scoreId 分数标识。
     */
    @ZenMethod
    public static void removeScore(IItemStack item, String scoreId) {
        ChestCavityApis.ORGANS.removeScore(CrTUtil.itemId(item), scoreId);
    }

    /**
     * 删除一个器官数据定义。
     *
     * @param item 目标物品。
     */
    @ZenMethod
    public static void remove(IItemStack item) {
        ChestCavityApis.ORGANS.remove(CrTUtil.itemId(item));
    }

    /**
     * 设置器官定义是否为伪器官。
     *
     * @param item 目标物品。
     * @param value 是否为伪器官。
     */
    @ZenMethod
    public static void setPseudo(IItemStack item, boolean value) {
        ChestCavityApis.ORGANS.setPseudo(CrTUtil.itemId(item), value);
    }

    /**
     * 查询指定物品对应的器官数据视图。
     *
     * @param item 目标物品。
     * @return 器官数据视图；如果不存在则返回 `null`。
     */
    @ZenMethod
    public static CrTOrganDataView get(IItemStack item) {
        OrganDataView view = ChestCavityApis.ORGANS.get(CrTUtil.itemId(item));
        return view == null ? null : new CrTOrganDataView(view);
    }
}
