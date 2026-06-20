package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.content.BodyTypeDef;
import com.shiver.chestcavity.content.ContentRegistry;
import com.shiver.chestcavity.content.ExceptionalOrganDef;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChestCavityTypeApi {

    ChestCavityTypeApi() {
    }

    public void register(String typeId) {
        publish(type(typeId));
    }

    public void remove(String typeId) {
        if (!DataLoaders.FALLBACK_ID.equals(typeId)) {
            ContentRegistry.removeScriptBodyType(typeId);
        }
    }

    public void addBaseScore(String typeId, String scoreId, float value) {
        mutate(typeId, type -> type.addBaseOrganScore(scoreId, value));
    }

    public void removeBaseScore(String typeId, String scoreId) {
        mutateExisting(typeId, type -> type.removeBaseOrganScore(scoreId));
    }

    public void setSlot(String typeId, int index, ItemStack stack) {
        mutate(typeId, type -> type.setSlot(index, stack));
    }

    public void clearSlots(String typeId) {
        mutateExisting(typeId, BodyTypeDef::clearSlots);
    }

    public void addForbiddenSlot(String typeId, int slot) {
        mutate(typeId, type -> type.addForbiddenSlot(slot));
    }

    public void removeForbiddenSlot(String typeId, int slot) {
        mutateExisting(typeId, type -> type.removeForbiddenSlot(slot));
    }

    public void setDropRateMultiplier(String typeId, float value) {
        mutate(typeId, type -> type.setDropRateMultiplier(value));
    }

    public void setBossChestCavity(String typeId, boolean value) {
        mutate(typeId, type -> type.setBossChestCavity(value));
    }

    public void setPlayerChestCavity(String typeId, boolean value) {
        mutate(typeId, type -> type.setPlayerChestCavity(value));
    }

    public void setLayout(String typeId, ResourceLocation layoutId) {
        mutate(typeId, type -> type.setLayoutId(layoutId));
    }

    public void addExceptionalOrgan(String typeId, Item item, Map<String, Float> scores) {
        mutate(typeId, type -> type.addExceptionalOrgan(new ExceptionalOrganDef(item == null ? null : item.getRegistryName(), null, copyScores(scores))));
    }

    public void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        mutate(typeId, type -> type.addExceptionalOrgan(new ExceptionalOrganDef(null, oreName, copyScores(scores))));
    }

    public void clearExceptionalOrgans(String typeId) {
        mutateExisting(typeId, BodyTypeDef::clearExceptionalOrgans);
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

    public ResourceLocation getLayout(String typeId) {
        return DataLoaders.getType(typeId).getLayoutId();
    }

    private BodyTypeDef type(String typeId) {
        BodyTypeDef type = existingType(typeId);
        return type == null ? new BodyTypeDef(typeId) : type;
    }

    private BodyTypeDef existingType(String typeId) {
        return typeId == null ? null : ContentRegistry.getManifest().getBodyType(typeId);
    }

    private void publish(BodyTypeDef type) {
        if (type != null) {
            ContentRegistry.publishScriptBodyType(type);
        }
    }

    private void mutate(String typeId, TypeMutation mutation) {
        if (typeId == null || mutation == null) {
            return;
        }
        ContentRegistry.applyScriptOperation(manifest -> {
            BodyTypeDef type = manifest.getBodyType(typeId);
            if (type == null) {
                type = new BodyTypeDef(typeId);
            }
            mutation.apply(type);
            manifest.registerBodyType(type);
        });
    }

    private void mutateExisting(String typeId, TypeMutation mutation) {
        if (typeId == null || mutation == null) {
            return;
        }
        ContentRegistry.applyScriptOperation(manifest -> {
            BodyTypeDef type = manifest.getBodyType(typeId);
            if (type != null) {
                mutation.apply(type);
                manifest.registerBodyType(type);
            }
        });
    }

    private interface TypeMutation {
        void apply(BodyTypeDef type);
    }

    private Map<String, Float> copyScores(Map<String, Float> scores) {
        Map<String, Float> copy = new LinkedHashMap<>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
