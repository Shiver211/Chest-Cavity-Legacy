package com.shiver.chestcavity.api;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对外暴露胸腔数据的安全包装视图。
 */
public final class ChestCavityView {

    private final IChestCavity chestCavity;

    /**
     * 使用内部胸腔数据创建一个只读/受控视图。
     *
     * @param chestCavity 被包装的胸腔数据。
     */
    ChestCavityView(IChestCavity chestCavity) {
        this.chestCavity = chestCavity;
    }

    /**
     * 返回胸腔所属实体。
     *
     * @return 胸腔持有者。
     */
    public EntityLivingBase getOwner() {
        return chestCavity.getOwner();
    }

    /**
     * 判断胸腔是否已经被打开。
     *
     * @return `true` 表示胸腔已打开。
     */
    public boolean isOpened() {
        return chestCavity.isOpened();
    }

    /**
     * 返回胸腔槽位总数。
     *
     * @return 槽位总数。
     */
    public int getSlotCount() {
        return chestCavity.getSlotCount();
    }

    /**
     * 返回指定器官分数的当前值。
     *
     * @param scoreId 分数标识。
     * @return 当前分值。
     */
    public float getOrganScore(String scoreId) {
        return chestCavity.getOrganScore(scoreId);
    }

    /**
     * 返回全部器官分数的只读视图。
     *
     * @return 器官分数字典。
     */
    public Map<String, Float> getOrganScores() {
        return Collections.unmodifiableMap(chestCavity.getOrganScores());
    }

    /**
     * 判断胸腔中是否存在指定物品对应的器官。
     *
     * @param itemId 物品注册名。
     * @return `true` 表示存在至少一个。
     */
    public boolean hasOrgan(ResourceLocation itemId) {
        return getOrganCount(itemId) > 0;
    }

    /**
     * 统计指定器官物品在胸腔中的总数量。
     *
     * @param itemId 物品注册名。
     * @return 器官总数量。
     */
    public int getOrganCount(ResourceLocation itemId) {
        int count = 0;
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (matches(stack, itemId)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 返回所有匹配指定器官物品的槽位索引。
     *
     * @param itemId 物品注册名。
     * @return 匹配到的槽位索引数组。
     */
    public int[] getOrganSlots(ResourceLocation itemId) {
        List<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < chestCavity.getSlotCount(); i++) {
            if (matches(chestCavity.getOrgan(i), itemId)) {
                slots.add(i);
            }
        }
        int[] result = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            result[i] = slots.get(i);
        }
        return result;
    }

    /**
     * 返回指定槽位中的器官副本。
     *
     * @param slot 槽位索引。
     * @return 槽位中的器官副本。
     */
    public ItemStack getOrgan(int slot) {
        ItemStack stack = chestCavity.getOrgan(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    /**
     * 设置指定槽位中的器官，并立即重算分数。
     *
     * @param slot 槽位索引。
     * @param stack 要放入的器官物品。
     */
    public void setOrgan(int slot, ItemStack stack) {
        ChestCavityHelper.setOrganAndRecalculate(chestCavity, slot, stack == null ? ItemStack.EMPTY : stack.copy());
    }

    /**
     * 直接设置一项器官分数并同步变更。
     *
     * @param scoreId 分数标识。
     * @param value 新的分数值。
     */
    public void setOrganScore(String scoreId, float value) {
        chestCavity.setOrganScore(scoreId, value);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    /**
     * 为一项器官分数叠加值并同步变更。
     *
     * @param scoreId 分数标识。
     * @param value 要累加的分数值。
     */
    public void addOrganScore(String scoreId, float value) {
        chestCavity.addOrganScore(scoreId, value);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    /**
     * 重新计算全部器官分数并同步变更。
     */
    public void recalculateScores() {
        ChestCavityHelper.recalculateOrganScores(chestCavity);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    /**
     * 打开胸腔并执行打开后的附带处理。
     */
    public void openChestCavity() {
        ChestCavityHelper.openChestCavity(chestCavity);
    }

    /**
     * 返回心脏出血计时器。
     *
     * @return 心脏出血计时器。
     */
    public int getHeartBleedTimer() {
        return chestCavity.getHeartBleedTimer();
    }

    /**
     * 设置心脏出血计时器。
     *
     * @param value 新的计时值。
     */
    public void setHeartBleedTimer(int value) {
        chestCavity.setHeartBleedTimer(value);
    }

    /**
     * 返回血液中毒计时器。
     *
     * @return 血液中毒计时器。
     */
    public int getBloodPoisonTimer() {
        return chestCavity.getBloodPoisonTimer();
    }

    /**
     * 设置血液中毒计时器。
     *
     * @param value 新的计时值。
     */
    public void setBloodPoisonTimer(int value) {
        chestCavity.setBloodPoisonTimer(value);
    }

    /**
     * 返回肝脏计时器。
     *
     * @return 肝脏计时器。
     */
    public int getLiverTimer() {
        return chestCavity.getLiverTimer();
    }

    /**
     * 设置肝脏计时器。
     *
     * @param value 新的计时值。
     */
    public void setLiverTimer(int value) {
        chestCavity.setLiverTimer(value);
    }

    /**
     * 返回熔炉进度。
     *
     * @return 熔炉进度。
     */
    public int getFurnaceProgress() {
        return chestCavity.getFurnaceProgress();
    }

    /**
     * 设置熔炉进度。
     *
     * @param value 新的进度值。
     */
    public void setFurnaceProgress(int value) {
        chestCavity.setFurnaceProgress(value);
    }

    /**
     * 返回光合作用进度。
     *
     * @return 光合作用进度。
     */
    public int getPhotosynthesisProgress() {
        return chestCavity.getPhotosynthesisProgress();
    }

    /**
     * 设置光合作用进度。
     *
     * @param value 新的进度值。
     */
    public void setPhotosynthesisProgress(int value) {
        chestCavity.setPhotosynthesisProgress(value);
    }

    /**
     * 返回当前连接的末地水晶实体 ID。
     *
     * @return 末地水晶实体 ID。
     */
    public int getConnectedCrystalId() {
        return chestCavity.getConnectedCrystalId();
    }

    /**
     * 设置当前连接的末地水晶实体 ID。
     *
     * @param value 末地水晶实体 ID。
     */
    public void setConnectedCrystalId(int value) {
        chestCavity.setConnectedCrystalId(value);
    }

    /**
     * 返回底层真实胸腔数据对象。
     *
     * @return 内部胸腔数据。
     */
    public IChestCavity getInternal() {
        return chestCavity;
    }

    /**
     * 判断一个物品堆是否与目标物品注册名匹配。
     *
     * @param stack 要检查的物品堆。
     * @param itemId 目标物品注册名。
     * @return `true` 表示匹配成功。
     */
    private boolean matches(ItemStack stack, ResourceLocation itemId) {
        if (stack == null || stack.isEmpty() || itemId == null) {
            return false;
        }
        Item item = stack.getItem();
        return item != null && itemId.equals(item.getRegistryName());
    }
}
