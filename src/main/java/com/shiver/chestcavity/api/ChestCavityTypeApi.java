package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对外暴露胸腔类型的注册、修改和查询接口。
 */
public final class ChestCavityTypeApi {

    /**
     * 仅允许通过统一 API 单例创建。
     */
    ChestCavityTypeApi() {
    }

    /**
     * 注册一个新的可生成胸腔类型。
     *
     * @param typeId 类型标识。
     */
    public void register(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetTypeId));
    }

    /**
     * 注销一个已注册的胸腔类型。
     *
     * @param typeId 类型标识。
     */
    public void remove(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> DataLoaders.unregisterType(targetTypeId));
    }

    /**
     * 为指定胸腔类型添加一项基础器官分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    public void addBaseScore(String typeId, String scoreId, float value) {
        final String targetTypeId = typeId;
        final String targetScoreId = scoreId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> addBaseScoreNow(targetTypeId, targetScoreId, targetValue));
    }

    /**
     * 移除指定胸腔类型中的一项基础器官分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     */
    public void removeBaseScore(String typeId, String scoreId) {
        final String targetTypeId = typeId;
        final String targetScoreId = scoreId;
        DataLoaders.applyRuntimeOverride(() -> removeBaseScoreNow(targetTypeId, targetScoreId));
    }

    /**
     * 设置指定胸腔类型中某个默认槽位的物品。
     *
     * @param typeId 类型标识。
     * @param index 槽位索引。
     * @param stack 要放入的物品。
     */
    public void setSlot(String typeId, int index, ItemStack stack) {
        final String targetTypeId = typeId;
        final int targetIndex = index;
        final ItemStack targetStack = stack == null ? ItemStack.EMPTY : stack.copy();
        DataLoaders.applyRuntimeOverride(() -> setSlotNow(targetTypeId, targetIndex, targetStack));
    }

    /**
     * 清空指定胸腔类型的默认器官槽位。
     *
     * @param typeId 类型标识。
     */
    public void clearSlots(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> clearSlotsNow(targetTypeId));
    }

    /**
     * 为指定胸腔类型新增一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    public void addForbiddenSlot(String typeId, int slot) {
        final String targetTypeId = typeId;
        final int targetSlot = slot;
        DataLoaders.applyRuntimeOverride(() -> addForbiddenSlotNow(targetTypeId, targetSlot));
    }

    /**
     * 从指定胸腔类型中移除一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    public void removeForbiddenSlot(String typeId, int slot) {
        final String targetTypeId = typeId;
        final int targetSlot = slot;
        DataLoaders.applyRuntimeOverride(() -> removeForbiddenSlotNow(targetTypeId, targetSlot));
    }

    /**
     * 设置指定胸腔类型的器官掉率倍率。
     *
     * @param typeId 类型标识。
     * @param value 新的掉率倍率。
     */
    public void setDropRateMultiplier(String typeId, float value) {
        final String targetTypeId = typeId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setDropRateMultiplierNow(targetTypeId, targetValue));
    }

    /**
     * 设置指定胸腔类型是否视为 Boss 胸腔。
     *
     * @param typeId 类型标识。
     * @param value 是否为 Boss 胸腔。
     */
    public void setBossChestCavity(String typeId, boolean value) {
        final String targetTypeId = typeId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setBossChestCavityNow(targetTypeId, targetValue));
    }

    /**
     * 设置指定胸腔类型是否视为玩家胸腔。
     *
     * @param typeId 类型标识。
     * @param value 是否为玩家胸腔。
     */
    public void setPlayerChestCavity(String typeId, boolean value) {
        final String targetTypeId = typeId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setPlayerChestCavityNow(targetTypeId, targetValue));
    }

    /**
     * 为指定类型添加一条按具体物品匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param item 要匹配的物品。
     * @param scores 命中后使用的器官分数。
     */
    public void addExceptionalOrgan(String typeId, Item item, Map<String, Float> scores) {
        final String targetTypeId = typeId;
        final Item targetItem = item;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> addExceptionalOrganNow(targetTypeId, targetItem, targetScores));
    }

    /**
     * 为指定类型添加一条按矿辞匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param oreName 要匹配的矿辞名称。
     * @param scores 命中后使用的器官分数。
     */
    public void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        final String targetTypeId = typeId;
        final String targetOreName = oreName;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> addExceptionalOrganByOreNow(targetTypeId, targetOreName, targetScores));
    }

    /**
     * 清空指定类型下的全部特殊器官规则。
     *
     * @param typeId 类型标识。
     */
    public void clearExceptionalOrgans(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> clearExceptionalOrgansNow(targetTypeId));
    }

    /**
     * 判断指定类型是否被标记为 Boss 胸腔。
     *
     * @param typeId 类型标识。
     * @return `true` 表示是 Boss 胸腔。
     */
    public boolean isBossChestCavity(String typeId) {
        return DataLoaders.getType(typeId).isBossChestCavity();
    }

    /**
     * 判断指定类型是否被标记为玩家胸腔。
     *
     * @param typeId 类型标识。
     * @return `true` 表示是玩家胸腔。
     */
    public boolean isPlayerChestCavity(String typeId) {
        return DataLoaders.getType(typeId).isPlayerChestCavity();
    }

    /**
     * 返回指定类型的器官掉率倍率。
     *
     * @param typeId 类型标识。
     * @return 器官掉率倍率。
     */
    public float getDropRateMultiplier(String typeId) {
        return DataLoaders.getType(typeId).getDropRateMultiplier();
    }

    /**
     * 返回指定类型当前对应的胸腔类型对象。
     *
     * @param typeId 类型标识。
     * @return 胸腔类型对象。
     */
    public ChestCavityType get(String typeId) {
        return DataLoaders.getType(typeId);
    }

    /**
     * 在当前数据环境中立即注册一个新的生成式胸腔类型。
     *
     * @param typeId 类型标识。
     */
    private void registerNow(String typeId) {
        if (typeId != null) {
            DataLoaders.registerType(typeId, new GeneratedChestCavityType());
        }
    }

    /**
     * 立即为指定类型添加一项基础器官分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     * @param value 分数值。
     */
    private void addBaseScoreNow(String typeId, String scoreId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addBaseOrganScore(scoreId, value);
        }
    }

    /**
     * 立即移除指定类型中的一项基础器官分数。
     *
     * @param typeId 类型标识。
     * @param scoreId 分数标识。
     */
    private void removeBaseScoreNow(String typeId, String scoreId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeBaseOrganScore(scoreId);
        }
    }

    /**
     * 立即设置指定类型中的默认槽位内容。
     *
     * @param typeId 类型标识。
     * @param index 槽位索引。
     * @param stack 要放入的物品。
     */
    private void setSlotNow(String typeId, int index, ItemStack stack) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setSlot(index, stack);
        }
    }

    /**
     * 立即清空指定类型的默认槽位布局。
     *
     * @param typeId 类型标识。
     */
    private void clearSlotsNow(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearSlots();
        }
    }

    /**
     * 立即为指定类型增加一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    private void addForbiddenSlotNow(String typeId, int slot) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addForbiddenSlot(slot);
        }
    }

    /**
     * 立即为指定类型移除一个禁用槽位。
     *
     * @param typeId 类型标识。
     * @param slot 槽位索引。
     */
    private void removeForbiddenSlotNow(String typeId, int slot) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeForbiddenSlot(slot);
        }
    }

    /**
     * 立即设置指定类型的掉率倍率。
     *
     * @param typeId 类型标识。
     * @param value 新的掉率倍率。
     */
    private void setDropRateMultiplierNow(String typeId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setDropRateMultiplier(value);
        }
    }

    /**
     * 立即设置指定类型的 Boss 标记。
     *
     * @param typeId 类型标识。
     * @param value 是否为 Boss 胸腔。
     */
    private void setBossChestCavityNow(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setBossChestCavity(value);
        }
    }

    /**
     * 立即设置指定类型的玩家标记。
     *
     * @param typeId 类型标识。
     * @param value 是否为玩家胸腔。
     */
    private void setPlayerChestCavityNow(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setPlayerChestCavity(value);
        }
    }

    /**
     * 立即增加一条按具体物品匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param item 要匹配的物品。
     * @param scores 命中后使用的器官分数。
     */
    private void addExceptionalOrganNow(String typeId, Item item, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(item, null, copyScores(scores));
        }
    }

    /**
     * 立即增加一条按矿辞匹配的特殊器官规则。
     *
     * @param typeId 类型标识。
     * @param oreName 要匹配的矿辞名称。
     * @param scores 命中后使用的器官分数。
     */
    private void addExceptionalOrganByOreNow(String typeId, String oreName, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(null, oreName, copyScores(scores));
        }
    }

    /**
     * 立即清空指定类型下的全部特殊器官规则。
     *
     * @param typeId 类型标识。
     */
    private void clearExceptionalOrgansNow(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearExceptionalOrgans();
        }
    }

    /**
     * 返回一个可写的生成式胸腔类型；如果不存在则自动创建。
     *
     * @param typeId 类型标识。
     * @return 生成式胸腔类型；如果类型标识为空则返回 `null`。
     */
    private GeneratedChestCavityType getOrCreateGeneratedType(String typeId) {
        if (typeId == null) {
            return null;
        }
        ChestCavityType existing = DataLoaders.getTypes().get(typeId);
        if (existing instanceof GeneratedChestCavityType) {
            return (GeneratedChestCavityType) existing;
        }
        GeneratedChestCavityType type = new GeneratedChestCavityType();
        DataLoaders.registerType(typeId, type);
        return type;
    }

    /**
     * 返回一个已存在的生成式胸腔类型。
     *
     * @param typeId 类型标识。
     * @return 已存在的生成式胸腔类型；如果不存在或类型不匹配则返回 `null`。
     */
    private GeneratedChestCavityType getGeneratedType(String typeId) {
        ChestCavityType type = DataLoaders.getTypes().get(typeId);
        return type instanceof GeneratedChestCavityType ? (GeneratedChestCavityType) type : null;
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
