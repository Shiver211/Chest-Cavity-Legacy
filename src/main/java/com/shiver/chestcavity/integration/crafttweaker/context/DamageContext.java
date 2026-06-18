package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(CtConstants.CT_NAMESPACE + "DamageContext")
@ZenRegister
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

    @ZenGetter("entity")
    public EntityLivingBase getEntity() {
        return entity;
    }

    @ZenGetter("world")
    public World getWorld() {
        return entity == null ? null : entity.world;
    }

    @ZenGetter("scoreId")
    public ResourceLocation getScoreId() {
        return scoreId;
    }

    @ZenGetter("value")
    public float getValue() {
        return value;
    }

    @ZenGetter("baseValue")
    public float getBaseValue() {
        return baseValue;
    }

    @ZenGetter("extraValue")
    public float getExtraValue() {
        return value - baseValue;
    }

    @ZenGetter("scriptData")
    public ScriptDataRuntime getScriptData() {
        return scriptData;
    }

    @ZenGetter("source")
    public DamageSource getSource() {
        return source;
    }

    @ZenGetter("attacker")
    public Entity getAttacker() {
        return source == null ? null : source.getTrueSource();
    }

    @ZenGetter("immediateSource")
    public Entity getImmediateSource() {
        return source == null ? null : source.getImmediateSource();
    }

    @ZenGetter("originalDamage")
    public float getOriginalDamage() {
        return originalDamage;
    }

    @ZenGetter("currentDamage")
    public float getCurrentDamage() {
        return currentDamage;
    }

    @ZenMethod
    public void setDamage(float damage) {
        this.currentDamage = damage;
    }

    @ZenGetter("cancelled")
    public boolean isCancelled() {
        return cancelled;
    }

    @ZenMethod
    public void cancel() {
        this.cancelled = true;
        this.currentDamage = 0.0F;
    }
}
