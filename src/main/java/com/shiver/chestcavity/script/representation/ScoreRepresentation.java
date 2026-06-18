package com.shiver.chestcavity.script.representation;

import com.shiver.chestcavity.script.model.ScriptScoreCallbacks;
import com.shiver.chestcavity.script.model.ScriptScoreDefinition;
import com.shiver.chestcavity.script.model.ScriptScoreEvent;
import com.shiver.chestcavity.script.registry.ScriptScoreRegistry;
import net.minecraft.util.ResourceLocation;

public class ScoreRepresentation {

    private final ResourceLocation id;
    private String translationKey;
    private String displayName;
    private boolean negative;
    private int sortOrder;
    private final ScriptScoreCallbacks callbacks = new ScriptScoreCallbacks();

    public ScoreRepresentation(String id) {
        this(new ResourceLocation(id));
    }

    public ScoreRepresentation(ResourceLocation id) {
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

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setCallback(ScriptScoreEvent event, Object callback) {
        callbacks.set(event, callback);
    }

    public Object getCallback(ScriptScoreEvent event) {
        return callbacks.get(event);
    }

    public void setOnScoreChanged(Object callback) {
        setCallback(ScriptScoreEvent.SCORE_CHANGED, callback);
    }

    public void setOnServerTick(Object callback) {
        setCallback(ScriptScoreEvent.SERVER_TICK, callback);
    }

    public void setOnClientTick(Object callback) {
        setCallback(ScriptScoreEvent.CLIENT_TICK, callback);
    }

    public void setOnBreakSpeed(Object callback) {
        setCallback(ScriptScoreEvent.BREAK_SPEED, callback);
    }

    public void setOnIncomingDamage(Object callback) {
        setCallback(ScriptScoreEvent.INCOMING_DAMAGE, callback);
    }

    public void setOnAttackTarget(Object callback) {
        setCallback(ScriptScoreEvent.ATTACK_TARGET, callback);
    }

    public void setOnJump(Object callback) {
        setCallback(ScriptScoreEvent.JUMP, callback);
    }

    public void setOnEat(Object callback) {
        setCallback(ScriptScoreEvent.EAT, callback);
    }

    public void setOnPotionIncoming(Object callback) {
        setCallback(ScriptScoreEvent.POTION_INCOMING, callback);
    }

    public ScriptScoreDefinition build() {
        return new ScriptScoreDefinition(id, translationKey, displayName, negative, sortOrder, callbacks);
    }

    public ScriptScoreDefinition register() {
        ScriptScoreDefinition definition = build();
        ScriptScoreRegistry.register(definition);
        return definition;
    }
}
