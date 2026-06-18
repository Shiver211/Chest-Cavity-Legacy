package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(CtConstants.CT_NAMESPACE + "PotionContext")
@ZenRegister
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

    @ZenGetter("effect")
    public PotionEffect getEffect() {
        return effect;
    }

    @ZenGetter("potion")
    public Potion getPotion() {
        return effect == null ? null : effect.getPotion();
    }

    @ZenGetter("originalDuration")
    public int getOriginalDuration() {
        return originalDuration;
    }

    @ZenGetter("currentDuration")
    public int getCurrentDuration() {
        return currentDuration;
    }

    @ZenMethod
    public void setDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    @ZenGetter("originalAmplifier")
    public int getOriginalAmplifier() {
        return originalAmplifier;
    }

    @ZenGetter("currentAmplifier")
    public int getCurrentAmplifier() {
        return currentAmplifier;
    }

    @ZenMethod
    public void setAmplifier(int currentAmplifier) {
        this.currentAmplifier = currentAmplifier;
    }

    @ZenGetter("cancelled")
    public boolean isCancelled() {
        return cancelled;
    }

    @ZenMethod
    public void cancel() {
        this.cancelled = true;
    }
}
