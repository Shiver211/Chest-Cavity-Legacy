package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrganDefinition {

    private final ResourceLocation itemId;
    private final boolean pseudoOrgan;
    private final Map<ResourceLocation, Float> organScores = new LinkedHashMap<ResourceLocation, Float>();
    private final Object canInsertCallback;
    private final Object canRemoveCallback;
    private final Object insertedCallback;
    private final Object removedCallback;
    private final Object tickCallback;

    public OrganDefinition(ResourceLocation itemId, boolean pseudoOrgan, Map<ResourceLocation, Float> organScores,
                           Object canInsertCallback, Object canRemoveCallback, Object insertedCallback,
                           Object removedCallback, Object tickCallback) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        this.itemId = itemId;
        this.pseudoOrgan = pseudoOrgan;
        if (organScores != null) {
            this.organScores.putAll(organScores);
        }
        this.canInsertCallback = canInsertCallback;
        this.canRemoveCallback = canRemoveCallback;
        this.insertedCallback = insertedCallback;
        this.removedCallback = removedCallback;
        this.tickCallback = tickCallback;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    public Map<ResourceLocation, Float> getOrganScores() {
        return Collections.unmodifiableMap(organScores);
    }

    public Object getCanInsertCallback() {
        return canInsertCallback;
    }

    public Object getCanRemoveCallback() {
        return canRemoveCallback;
    }

    public Object getInsertedCallback() {
        return insertedCallback;
    }

    public Object getRemovedCallback() {
        return removedCallback;
    }

    public Object getTickCallback() {
        return tickCallback;
    }

    public OrganData toOrganData() {
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudoOrgan);
        data.setOrganScores(organScores);
        return data;
    }
}
