package com.shiver.chestcavity.script.model;

import net.minecraft.util.ResourceLocation;

public class ScriptAbilityDefinition {

    private final ResourceLocation id;
    private final String translationKey;
    private final String displayName;
    private final boolean wheelVisible;
    private final int sortOrder;
    private final Object activateCallback;

    public ScriptAbilityDefinition(ResourceLocation id, String translationKey, String displayName, boolean wheelVisible, int sortOrder, Object activateCallback) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.wheelVisible = wheelVisible;
        this.sortOrder = sortOrder;
        this.activateCallback = activateCallback;
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

    public boolean isWheelVisible() {
        return wheelVisible;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Object getActivateCallback() {
        return activateCallback;
    }
}
