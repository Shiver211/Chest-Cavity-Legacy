package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass(CtConstants.CT_NAMESPACE + "JumpContext")
@ZenRegister
public class JumpContext {

    private final EntityLivingBase entity;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;

    public JumpContext(EntityLivingBase entity, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData) {
        this.entity = entity;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
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
}
