package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.Map;

/**
 * 暴露给 ZenScript 的胸腔视图包装类。
 */
@ZenRegister
@ZenClass("mods.chestcavity.IChestCavity")
public final class CrTChestCavity {

    private final ChestCavityView view;

    /**
     * 使用内部胸腔视图创建脚本包装对象。
     *
     * @param view 内部胸腔视图。
     */
    CrTChestCavity(ChestCavityView view) {
        this.view = view;
    }

    /**
     * 判断胸腔是否已经打开。
     *
     * @return `true` 表示胸腔已打开。
     */
    @ZenGetter("isOpened")
    public boolean isOpened() {
        return view.isOpened();
    }

    /**
     * 返回胸腔槽位总数。
     *
     * @return 槽位总数。
     */
    @ZenGetter("slotCount")
    public int getSlotCount() {
        return view.getSlotCount();
    }

    /**
     * 返回指定器官分数的当前值。
     *
     * @param scoreId 分数标识。
     * @return 当前分值。
     */
    @ZenMethod
    public float getOrganScore(String scoreId) {
        return view.getOrganScore(scoreId);
    }

    /**
     * 返回全部器官分数字典。
     *
     * @return 器官分数字典。
     */
    @ZenMethod
    public Map<String, Float> getOrganScores() {
        return view.getOrganScores();
    }

    /**
     * 判断胸腔中是否存在指定器官物品。
     *
     * @param item 目标器官物品。
     * @return `true` 表示存在。
     */
    @ZenMethod
    public boolean hasOrgan(IItemStack item) {
        return view.hasOrgan(CrTUtil.itemId(item));
    }

    /**
     * 返回指定器官物品在胸腔中的总数量。
     *
     * @param item 目标器官物品。
     * @return 器官总数量。
     */
    @ZenMethod
    public int getOrganCount(IItemStack item) {
        return view.getOrganCount(CrTUtil.itemId(item));
    }

    /**
     * 返回指定器官物品所在的全部槽位。
     *
     * @param item 目标器官物品。
     * @return 槽位索引数组。
     */
    @ZenMethod
    public int[] getOrganSlots(IItemStack item) {
        return view.getOrganSlots(CrTUtil.itemId(item));
    }

    /**
     * 返回指定槽位中的器官物品。
     *
     * @param slot 槽位索引。
     * @return 器官物品。
     */
    @ZenMethod
    public IItemStack getOrgan(int slot) {
        return CrTUtil.stack(view.getOrgan(slot));
    }

    /**
     * 设置指定槽位中的器官物品。
     *
     * @param slot 槽位索引。
     * @param stack 要放入的器官物品。
     */
    @ZenMethod
    public void setOrgan(int slot, IItemStack stack) {
        view.setOrgan(slot, CrTUtil.stack(stack));
    }

    /**
     * 重新计算胸腔中的全部器官分数。
     */
    @ZenMethod
    public void recalculateScores() {
        view.recalculateScores();
    }

    /**
     * 打开当前胸腔。
     */
    @ZenMethod
    public void openChestCavity() {
        view.openChestCavity();
    }
}
