package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 定义一种胸腔类型应提供的默认器官配置和行为规则。
 */
public interface ChestCavityType {

    /**
     * 返回该胸腔类型的默认器官分数字典。
     *
     * @return 默认器官分数字典。
     */
    Map<String, Float> getDefaultOrganScores();

    /**
     * 返回指定分数项的默认值。
     *
     * @param id 分数标识。
     * @return 默认分值。
     */
    float getDefaultOrganScore(String id);

    /**
     * 返回该胸腔类型默认的器官布局。
     *
     * @return 默认胸腔物品栏。
     */
    ChestCavityInventory getDefaultChestCavity();

    /**
     * 判断指定槽位是否被该胸腔类型禁用。
     *
     * @param index 槽位索引。
     * @return `true` 表示该槽位不可用。
     */
    boolean isSlotForbidden(int index);

    /**
     * 用默认布局填充给定的胸腔物品栏。
     *
     * @param chestCavity 要填充的胸腔物品栏。
     */
    void fillChestCavityInventory(ChestCavityInventory chestCavity);

    /**
     * 将该类型的基础器官分数写入目标字典。
     *
     * @param organScores 要写入的分数字典。
     */
    void loadBaseOrganScores(Map<String, Float> organScores);

    /**
     * 尝试将普通物品视为特殊器官并返回对应数据。
     *
     * @param stack 要检查的物品堆。
     * @return 特殊器官数据；如果不匹配则返回普通器官数据或 `null`。
     */
    OrganData catchExceptionalOrgan(ItemStack stack);

    /**
     * 返回该胸腔类型在死亡时允许掉落的默认器官列表。
     *
     * @return 可掉落器官列表。
     */
    List<ItemStack> getDroppableOrgans();

    /**
     * 判断该胸腔类型是否属于 Boss。
     *
     * @return `true` 表示这是 Boss 胸腔。
     */
    boolean isBossChestCavity();

    /**
     * 返回该胸腔类型允许的心脏出血伤害上限。
     *
     * @return 心脏出血伤害上限。
     */
    default int getHeartBleedCap() {
        return isBossChestCavity() ? 5 : Integer.MAX_VALUE;
    }

    /**
     * 判断该胸腔类型是否属于玩家。
     *
     * @return `true` 表示这是玩家胸腔。
     */
    boolean isPlayerChestCavity();

    /**
     * 返回该胸腔类型的器官掉率倍率。
     *
     * @return 器官掉率倍率。
     */
    float getDropRateMultiplier();
}
