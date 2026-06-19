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
        if (typeId != null) {
            DataLoaders.registerType(typeId, new GeneratedChestCavityType());
        }
    }

    public void remove(String typeId) {
        DataLoaders.unregisterType(typeId);
    }

    public void addBaseScore(String typeId, String scoreId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addBaseOrganScore(scoreId, value);
        }
    }

    public void removeBaseScore(String typeId, String scoreId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeBaseOrganScore(scoreId);
        }
    }

    public void setSlot(String typeId, int index, ItemStack stack) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setSlot(index, stack);
        }
    }

    public void clearSlots(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearSlots();
        }
    }

    public void addForbiddenSlot(String typeId, int slot) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addForbiddenSlot(slot);
        }
    }

    public void removeForbiddenSlot(String typeId, int slot) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.removeForbiddenSlot(slot);
        }
    }

    public void setDropRateMultiplier(String typeId, float value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setDropRateMultiplier(value);
        }
    }

    public void setBossChestCavity(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setBossChestCavity(value);
        }
    }

    public void setPlayerChestCavity(String typeId, boolean value) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.setPlayerChestCavity(value);
        }
    }

    public void addExceptionalOrgan(String typeId, Item item, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(item, null, copyScores(scores));
        }
    }

    public void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        GeneratedChestCavityType type = getOrCreateGeneratedType(typeId);
        if (type != null) {
            type.addExceptionalOrgan(null, oreName, copyScores(scores));
        }
    }

    public void clearExceptionalOrgans(String typeId) {
        GeneratedChestCavityType type = getGeneratedType(typeId);
        if (type != null) {
            type.clearExceptionalOrgans();
        }
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
