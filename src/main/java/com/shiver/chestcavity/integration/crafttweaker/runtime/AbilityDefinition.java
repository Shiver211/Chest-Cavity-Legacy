package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.util.ResourceLocation;

public class AbilityDefinition {

    private final ResourceLocation id;
    private final String translationKey;
    private final String displayName;
    private final boolean wheelVisible;
    private final int sortOrder;
    private final Object activateContextCallback;
    private final Object canActivateContextCallback;
    private final Object cooldownContextCallback;
    private final Object costContextCallback;
    private final Object activateServerContextCallback;
    private final Object activeTickContextCallback;
    private final Object endContextCallback;
    private final Object activateClientContextCallback;

    public AbilityDefinition(ResourceLocation id, String translationKey, String displayName, boolean wheelVisible, int sortOrder,
                             Object activateContextCallback, Object canActivateContextCallback,
                             Object cooldownContextCallback, Object costContextCallback, Object activateServerContextCallback,
                             Object activeTickContextCallback, Object endContextCallback, Object activateClientContextCallback) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.wheelVisible = wheelVisible;
        this.sortOrder = sortOrder;
        this.activateContextCallback = activateContextCallback;
        this.canActivateContextCallback = canActivateContextCallback;
        this.cooldownContextCallback = cooldownContextCallback;
        this.costContextCallback = costContextCallback;
        this.activateServerContextCallback = activateServerContextCallback;
        this.activeTickContextCallback = activeTickContextCallback;
        this.endContextCallback = endContextCallback;
        this.activateClientContextCallback = activateClientContextCallback;
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

    public Object getActivateContextCallback() {
        return activateContextCallback;
    }

    public Object getCanActivateContextCallback() {
        return canActivateContextCallback;
    }

    public Object getCooldownContextCallback() {
        return cooldownContextCallback;
    }

    public Object getCostContextCallback() {
        return costContextCallback;
    }

    public Object getActivateServerContextCallback() {
        return activateServerContextCallback;
    }

    public Object getActiveTickContextCallback() {
        return activeTickContextCallback;
    }

    public Object getEndContextCallback() {
        return endContextCallback;
    }

    public Object getActivateClientContextCallback() {
        return activateClientContextCallback;
    }
}
