package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class PotionContext {

    private final EntityLivingBase entity;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;
    private final PotionEffect effect;
    private final int originalDuration;
    private final int originalAmplifier;
    private int currentDuration;
    private int currentAmplifier;
    private boolean cancelled;

    public PotionContext(EntityLivingBase entity, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData, PotionEffect effect) {
        this.entity = entity;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
        this.effect = effect;
        this.originalDuration = effect == null ? 0 : effect.getDuration();
        this.originalAmplifier = effect == null ? 0 : effect.getAmplifier();
        this.currentDuration = originalDuration;
        this.currentAmplifier = originalAmplifier;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public World getWorld() {
        return entity == null ? null : entity.world;
    }

    public ResourceLocation getScoreId() {
        return scoreId;
    }

    public float getValue() {
        return value;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public float getExtraValue() {
        return value - baseValue;
    }

    public ScriptDataRuntime getScriptData() {
        return scriptData;
    }

    public PotionEffect getEffect() {
        return effect;
    }

    public Potion getPotion() {
        return effect == null ? null : effect.getPotion();
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public void setDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    public int getOriginalAmplifier() {
        return originalAmplifier;
    }

    public int getCurrentAmplifier() {
        return currentAmplifier;
    }

    public void setAmplifier(int currentAmplifier) {
        this.currentAmplifier = currentAmplifier;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
