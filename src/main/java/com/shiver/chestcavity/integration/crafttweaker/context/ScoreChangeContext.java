package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ScoreChangeContext {

    private final EntityLivingBase entity;
    private final ResourceLocation scoreId;
    private final float oldValue;
    private final float newValue;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;

    public ScoreChangeContext(EntityLivingBase entity, ResourceLocation scoreId, float oldValue, float newValue, float baseValue, ScriptDataRuntime scriptData) {
        this.entity = entity;
        this.scoreId = scoreId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
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

    public float getOldValue() {
        return oldValue;
    }

    public float getNewValue() {
        return newValue;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public float getExtraValue() {
        return newValue - baseValue;
    }

    public float getDeltaValue() {
        return newValue - oldValue;
    }

    public ScriptDataRuntime getScriptData() {
        return scriptData;
    }
}
