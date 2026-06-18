package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass(CtConstants.CT_NAMESPACE + "AttackContext")
@ZenRegister
public class AttackContext {

    private final EntityLivingBase attacker;
    private final EntityLivingBase target;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;

    public AttackContext(EntityLivingBase attacker, EntityLivingBase target, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData) {
        this.attacker = attacker;
        this.target = target;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
    }

    @ZenGetter("attacker")
    public EntityLivingBase getAttacker() {
        return attacker;
    }

    @ZenGetter("target")
    public EntityLivingBase getTarget() {
        return target;
    }

    @ZenGetter("world")
    public World getWorld() {
        return attacker == null ? null : attacker.world;
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
