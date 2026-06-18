package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

public class ScoreDefinition {

    private final ResourceLocation id;
    private final String translationKey;
    private final String displayName;
    private final boolean negative;
    private final int sortOrder;
    private final ScoreCallbackSet callbacks;

    public ScoreDefinition(ResourceLocation id, String translationKey, String displayName, boolean negative, int sortOrder, ScoreCallbackSet callbacks) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.negative = negative;
        this.sortOrder = sortOrder;
        this.callbacks = new ScoreCallbackSet(callbacks);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isNegative() {
        return negative;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Object getCallback(ScoreEvent event) {
        return callbacks.get(event);
    }

    public ScoreCallbackSet getCallbacks() {
        return new ScoreCallbackSet(callbacks);
    }
}
