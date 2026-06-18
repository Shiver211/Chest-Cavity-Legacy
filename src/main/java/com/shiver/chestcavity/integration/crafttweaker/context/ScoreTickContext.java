package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ScoreTickContext {

    private final EntityLivingBase entity;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;
    private final boolean client;

    public ScoreTickContext(EntityLivingBase entity, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData, boolean client) {
        this.entity = entity;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
        this.client = client;
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

    public boolean isClient() {
        return client;
    }

    public boolean isServer() {
        return !client;
    }
}
