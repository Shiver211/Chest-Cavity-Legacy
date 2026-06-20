package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChestCavityTypeApi {

    ChestCavityTypeApi() {
    }

    public void register(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetTypeId));
    }

    public void remove(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> DataLoaders.unregisterType(targetTypeId));
    }

    public void addBaseScore(String typeId, String scoreId, float value) {
        final String targetTypeId = typeId;
        final String targetScoreId = scoreId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> addBaseScoreNow(targetTypeId, targetScoreId, targetValue));
    }

    public void removeBaseScore(String typeId, String scoreId) {
        final String targetTypeId = typeId;
        final String targetScoreId = scoreId;
        DataLoaders.applyRuntimeOverride(() -> removeBaseScoreNow(targetTypeId, targetScoreId));
    }

    public void setSlot(String typeId, int index, ItemStack stack) {
        final String targetTypeId = typeId;
        final int targetIndex = index;
        final ItemStack targetStack = stack == null ? ItemStack.EMPTY : stack.copy();
        DataLoaders.applyRuntimeOverride(() -> setSlotNow(targetTypeId, targetIndex, targetStack));
    }

    public void clearSlots(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> clearSlotsNow(targetTypeId));
    }

    public void addForbiddenSlot(String typeId, int slot) {
        final String targetTypeId = typeId;
        final int targetSlot = slot;
        DataLoaders.applyRuntimeOverride(() -> addForbiddenSlotNow(targetTypeId, targetSlot));
    }

    public void removeForbiddenSlot(String typeId, int slot) {
        final String targetTypeId = typeId;
        final int targetSlot = slot;
        DataLoaders.applyRuntimeOverride(() -> removeForbiddenSlotNow(targetTypeId, targetSlot));
    }

    public void setDropRateMultiplier(String typeId, float value) {
        final String targetTypeId = typeId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setDropRateMultiplierNow(targetTypeId, targetValue));
    }

    public void setBossChestCavity(String typeId, boolean value) {
        final String targetTypeId = typeId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setBossChestCavityNow(targetTypeId, targetValue));
    }

    public void setPlayerChestCavity(String typeId, boolean value) {
        final String targetTypeId = typeId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setPlayerChestCavityNow(targetTypeId, targetValue));
    }

    public void addExceptionalOrgan(String typeId, Item item, Map<String, Float> scores) {
        final String targetTypeId = typeId;
        final Item targetItem = item;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> addExceptionalOrganNow(targetTypeId, targetItem, targetScores));
    }

    public void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        final String targetTypeId = typeId;
        final String targetOreName = oreName;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> addExceptionalOrganByOreNow(targetTypeId, targetOreName, targetScores));
    }

    public void clearExceptionalOrgans(String typeId) {
        final String targetTypeId = typeId;
        DataLoaders.applyRuntimeOverride(() -> clearExceptionalOrgansNow(targetTypeId));
    }

    public boolean isBossChestCavity(String typeId) {
        return DataLoaders.getType(typeId).isBossChestCavity();
    }

    public boolean isPlayerChestCavity(String typeId) {
        return DataLoaders.getType(typeId).isPlayerChestCavity();
    }

    public float getDropRateMultiplier(String typeId) {
        return DataLoaders.getType(typeId).getDropRateMultiplier();
    }

    public ChestCavityType get(String typeId) {
        return DataLoaders.getType(typeId);
    }

    private void registerNow(String typeId) {
        if (typeId != null) {
            DataLoaders.registerType(typeId, new GeneratedChestCavityType());
        }
    }

    private void addBaseScoreNow(String typeId, String scoreId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addBaseOrganScore(scoreId, value);
        }
    }

    private void removeBaseScoreNow(String typeId, String scoreId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeBaseOrganScore(scoreId);
        }
    }

    private void setSlotNow(String typeId, int index, ItemStack stack) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setSlot(index, stack);
        }
    }

    private void clearSlotsNow(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearSlots();
        }
    }

    private void addForbiddenSlotNow(String typeId, int slot) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addForbiddenSlot(slot);
        }
    }

    private void removeForbiddenSlotNow(String typeId, int slot) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeForbiddenSlot(slot);
        }
    }

    private void setDropRateMultiplierNow(String typeId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setDropRateMultiplier(value);
        }
    }

    private void setBossChestCavityNow(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setBossChestCavity(value);
        }
    }

    private void setPlayerChestCavityNow(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setPlayerChestCavity(value);
        }
    }

    private void addExceptionalOrganNow(String typeId, Item item, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(item, null, copyScores(scores));
        }
    }

    private void addExceptionalOrganByOreNow(String typeId, String oreName, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(null, oreName, copyScores(scores));
        }
    }

    private void clearExceptionalOrgansNow(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearExceptionalOrgans();
        }
    }

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

    private GeneratedChestCavityType getGeneratedType(String typeId) {
        ChestCavityType type = DataLoaders.getTypes().get(typeId);
        return type instanceof GeneratedChestCavityType ? (GeneratedChestCavityType) type : null;
    }

    private Map<String, Float> copyScores(Map<String, Float> scores) {
        Map<String, Float> copy = new LinkedHashMap<String, Float>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
