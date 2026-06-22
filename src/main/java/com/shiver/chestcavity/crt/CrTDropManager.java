package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 向 ZenScript 暴露器官掉落表管理接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.DropManager")
public final class CrTDropManager {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTDropManager() {
    }

    /**
     * 为指定实体增加一个器官掉落候选项。
     *
     * @param entityId 实体注册名。
     * @param stack 掉落物品。
     * @param weight 掉落权重。
     */
    @ZenMethod
    public static void addOrganDrop(String entityId, IItemStack stack, int weight) {
        ChestCavityApis.DROPS.addOrganDrop(CrTUtil.id(entityId), CrTUtil.stack(stack), weight);
    }

    /**
     * 设置指定实体掉落表的触发概率。
     *
     * @param entityId 实体注册名。
     * @param value 触发概率。
     */
    @ZenMethod
    public static void setDropProbability(String entityId, float value) {
        ChestCavityApis.DROPS.setDropProbability(CrTUtil.id(entityId), value);
    }

    /**
     * 移除指定实体掉落表中的一个器官候选项。
     *
     * @param entityId 实体注册名。
     * @param stack 要移除的器官物品。
     */
    @ZenMethod
    public static void removeOrganDrop(String entityId, IItemStack stack) {
        ChestCavityApis.DROPS.removeOrganDrop(CrTUtil.id(entityId), CrTUtil.stack(stack));
    }

    /**
     * 清空指定实体的全部器官掉落配置。
     *
     * @param entityId 实体注册名。
     */
    @ZenMethod
    public static void removeAllOrganDrops(String entityId) {
        ChestCavityApis.DROPS.removeAllOrganDrops(CrTUtil.id(entityId));
    }
}
