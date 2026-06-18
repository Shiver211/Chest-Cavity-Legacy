package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DamageContext {

    private final EntityLivingBase entity;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;
    private final DamageSource source;
    private final float originalDamage;
    private float currentDamage;
    private boolean cancelled;

    public DamageContext(EntityLivingBase entity, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData, DamageSource source, float originalDamage) {
        this.entity = entity;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
        this.source = source;
        this.originalDamage = originalDamage;
        this.currentDamage = originalDamage;
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

    public DamageSource getSource() {
        return source;
    }

    public Entity getAttacker() {
        return source == null ? null : source.getTrueSource();
    }

    public Entity getImmediateSource() {
        return source == null ? null : source.getImmediateSource();
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public float getCurrentDamage() {
        return currentDamage;
    }

    public void setDamage(float damage) {
        this.currentDamage = damage;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
        this.currentDamage = 0.0F;
    }
}
