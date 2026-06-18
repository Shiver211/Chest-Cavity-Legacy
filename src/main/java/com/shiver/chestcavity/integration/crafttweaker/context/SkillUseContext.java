package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(CtConstants.CT_NAMESPACE + "SkillUseContext")
@ZenRegister
public class SkillUseContext {

    private final EntityPlayer player;
    private final ResourceLocation abilityId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;
    private int cooldown;
    private float cost;
    private int activeTicks;
    private int tickIndex;
    private boolean cancelled;

    public SkillUseContext(EntityPlayer player, ResourceLocation abilityId, float value, float baseValue, ScriptDataRuntime scriptData) {
        this.player = player;
        this.abilityId = abilityId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
    }

    @ZenGetter("player")
    public EntityPlayer getPlayer() {
        return player;
    }

    @ZenGetter("world")
    public World getWorld() {
        return player == null ? null : player.world;
    }

    @ZenGetter("abilityId")
    public ResourceLocation getAbilityId() {
        return abilityId;
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

    @ZenGetter("lookVec")
    public Vec3d getLookVec() {
        return player == null ? Vec3d.ZERO : player.getLookVec();
    }

    @ZenGetter("cooldown")
    public int getCooldown() {
        return cooldown;
    }

    @ZenMethod
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @ZenGetter("cost")
    public float getCost() {
        return cost;
    }

    @ZenMethod
    public void setCost(float cost) {
        this.cost = cost;
    }

    @ZenGetter("cancelled")
    public boolean isCancelled() {
        return cancelled;
    }

    @ZenMethod
    public void cancel() {
        this.cancelled = true;
    }

    @ZenGetter("activeTicks")
    public int getActiveTicks() {
        return activeTicks;
    }

    @ZenMethod
    public void setActiveTicks(int activeTicks) {
        this.activeTicks = activeTicks;
    }

    @ZenGetter("tickIndex")
    public int getTickIndex() {
        return tickIndex;
    }

    @ZenMethod
    public void setTickIndex(int tickIndex) {
        this.tickIndex = tickIndex;
    }
}
