package com.shiver.chestcavity.script.model;

import net.minecraft.util.ResourceLocation;

public class ScriptScoreDefinition {

    private final ResourceLocation id;
    private final String translationKey;
    private final String displayName;
    private final boolean negative;
    private final int sortOrder;
    private final ScriptScoreCallbacks callbacks;

    public ScriptScoreDefinition(ResourceLocation id, String translationKey, String displayName, boolean negative, int sortOrder, ScriptScoreCallbacks callbacks) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.negative = negative;
        this.sortOrder = sortOrder;
        this.callbacks = new ScriptScoreCallbacks(callbacks);
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

    public Object getCallback(ScriptScoreEvent event) {
        return callbacks.get(event);
    }

    public ScriptScoreCallbacks getCallbacks() {
        return new ScriptScoreCallbacks(callbacks);
    }
}
