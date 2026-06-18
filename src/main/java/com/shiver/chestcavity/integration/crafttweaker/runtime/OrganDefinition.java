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

    public OrganDefinition(ResourceLocation itemId, boolean pseudoOrgan, Map<ResourceLocation, Float> organScores) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        this.itemId = itemId;
        this.pseudoOrgan = pseudoOrgan;
        if (organScores != null) {
            this.organScores.putAll(organScores);
        }
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

    public OrganData toOrganData() {
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudoOrgan);
        data.setOrganScores(organScores);
        return data;
    }
}
