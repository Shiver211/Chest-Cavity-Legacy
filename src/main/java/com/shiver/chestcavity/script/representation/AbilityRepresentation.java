package com.shiver.chestcavity.script.representation;

import com.shiver.chestcavity.script.model.ScriptAbilityDefinition;
import com.shiver.chestcavity.script.registry.ScriptAbilityRegistry;
import net.minecraft.util.ResourceLocation;

public class AbilityRepresentation {

    private final ResourceLocation id;
    private String translationKey;
    private String displayName;
    private boolean wheelVisible = true;
    private int sortOrder;
    private Object onActivate;

    public AbilityRepresentation(String id) {
        this(new ResourceLocation(id));
    }

    public AbilityRepresentation(ResourceLocation id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isWheelVisible() {
        return wheelVisible;
    }

    public void setWheelVisible(boolean wheelVisible) {
        this.wheelVisible = wheelVisible;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Object getOnActivate() {
        return onActivate;
    }

    public void setOnActivate(Object onActivate) {
        this.onActivate = onActivate;
    }

    public ScriptAbilityDefinition build() {
        return new ScriptAbilityDefinition(id, translationKey, displayName, wheelVisible, sortOrder, onActivate);
    }

    public ScriptAbilityDefinition register() {
        ScriptAbilityDefinition definition = build();
        ScriptAbilityRegistry.register(definition);
        return definition;
    }
}
