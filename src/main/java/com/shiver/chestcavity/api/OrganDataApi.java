package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对外暴露器官数据的注册、修改和查询接口。
 */
public final class OrganDataApi {

    /**
     * 仅允许通过统一 API 单例创建。
     */
    OrganDataApi() {
    }

    /**
     * 注册一个普通器官数据定义。
     *
     * @param itemId 物品注册名。
     * @param scores 器官分数字典。
     */
    public void register(ResourceLocation itemId, Map<String, Float> scores) {
        final ResourceLocation targetItemId = itemId;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetItemId, targetScores, false));
    }

    /**
     * 注册一个伪器官数据定义。
     *
     * @param itemId 物品注册名。
     * @param scores 器官分数字典。
     */
    public void registerPseudo(ResourceLocation itemId, Map<String, Float> scores) {
        final ResourceLocation targetItemId = itemId;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetItemId, targetScores, true));
    }

    /**
     * 为指定器官数据添加或覆盖一项分数。
     *
     * @param itemId 物品注册名。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    public void addScore(ResourceLocation itemId, String scoreId, float value) {
        final ResourceLocation targetItemId = itemId;
        final String targetScoreId = scoreId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> addScoreNow(targetItemId, targetScoreId, targetValue));
    }

    /**
     * 移除指定器官数据中的一项分数。
     *
     * @param itemId 物品注册名。
     * @param scoreId 分数标识。
     */
    public void removeScore(ResourceLocation itemId, String scoreId) {
        final ResourceLocation targetItemId = itemId;
        final String targetScoreId = scoreId;
        DataLoaders.applyRuntimeOverride(() -> removeScoreNow(targetItemId, targetScoreId));
    }

    /**
     * 删除一个器官数据定义。
     *
     * @param itemId 物品注册名。
     */
    public void remove(ResourceLocation itemId) {
        final ResourceLocation targetItemId = itemId;
        DataLoaders.applyRuntimeOverride(() -> OrganData.unregister(targetItemId));
    }

    /**
     * 设置一个器官定义是否视为伪器官。
     *
     * @param itemId 物品注册名。
     * @param value 是否为伪器官。
     */
    public void setPseudo(ResourceLocation itemId, boolean value) {
        final ResourceLocation targetItemId = itemId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setPseudoNow(targetItemId, targetValue));
    }

    /**
     * 查询指定物品对应的器官数据视图。
     *
     * @param itemId 物品注册名。
     * @return 器官数据视图；如果不存在则返回 `null`。
     */
    public OrganDataView get(ResourceLocation itemId) {
        OrganData data = OrganData.get(itemId);
        return data == null ? null : new OrganDataView(data);
    }

    /**
     * 在当前运行时环境中立即注册一个器官定义。
     *
     * @param itemId 物品注册名。
     * @param scores 器官分数字典。
     * @param pseudo 是否为伪器官。
     */
    private void registerNow(ResourceLocation itemId, Map<String, Float> scores, boolean pseudo) {
        if (itemId == null) {
            return;
        }
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudo);
        data.setOrganScores(copyScores(scores));
        OrganData.register(itemId, data);
    }

    /**
     * 在运行时立即为指定器官定义添加一项分数。
     *
     * @param itemId 物品注册名。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    private void addScoreNow(ResourceLocation itemId, String scoreId, float value) {
        if (itemId == null || scoreId == null) {
            return;
        }
        OrganData data = getOrCreate(itemId);
        data.getOrganScores().put(scoreId, value);
    }

    /**
     * 在运行时立即移除指定器官定义中的一项分数。
     *
     * @param itemId 物品注册名。
     * @param scoreId 分数标识。
     */
    private void removeScoreNow(ResourceLocation itemId, String scoreId) {
        OrganData data = OrganData.get(itemId);
        if (data != null && scoreId != null) {
            data.getOrganScores().remove(scoreId);
        }
    }

    /**
     * 在运行时立即设置器官定义的伪器官标记。
     *
     * @param itemId 物品注册名。
     * @param value 是否为伪器官。
     */
    private void setPseudoNow(ResourceLocation itemId, boolean value) {
        if (itemId != null) {
            getOrCreate(itemId).setPseudoOrgan(value);
        }
    }

    /**
     * 获取一个已存在的器官定义；如果不存在则创建空定义。
     *
     * @param itemId 物品注册名。
     * @return 对应的器官数据对象。
     */
    private OrganData getOrCreate(ResourceLocation itemId) {
        OrganData data = OrganData.get(itemId);
        if (data == null) {
            data = new OrganData();
            OrganData.register(itemId, data);
        }
        return data;
    }

    /**
     * 复制一份分数字典，避免外部修改内部状态。
     *
     * @param scores 原始分数字典。
     * @return 复制后的分数字典。
     */
    private Map<String, Float> copyScores(Map<String, Float> scores) {
        Map<String, Float> copy = new LinkedHashMap<String, Float>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
