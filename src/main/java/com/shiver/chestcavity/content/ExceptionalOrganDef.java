package com.shiver.chestcavity.content;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ExceptionalOrganDef {

    private final ResourceLocation itemId;
    private final String oreName;
    private final Map<String, Float> scores;

    public ExceptionalOrganDef(ResourceLocation itemId, String oreName, Map<String, Float> scores) {
        this.itemId = itemId;
        this.oreName = oreName;
        this.scores = new LinkedHashMap<>();
        if (scores != null) {
            this.scores.putAll(scores);
        }
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public String getOreName() {
        return oreName;
    }

    public Map<String, Float> getScores() {
        return Collections.unmodifiableMap(scores);
    }
}
