package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.script.model.ScriptScoreCallbacks;
import com.shiver.chestcavity.script.model.ScriptScoreDefinition;
import com.shiver.chestcavity.script.model.ScriptScoreEvent;
import com.shiver.chestcavity.script.registry.ScriptScoreRegistry;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "ScoreRepresentation")
@ZenRegister
public class ScoreRepresentation {

    private final ResourceLocation id;

    @ZenProperty
    public String translationKey;

    @ZenProperty
    public String displayName;

    @ZenProperty
    public boolean negative;

    @ZenProperty
    public int sortOrder;

    @ZenProperty
    public ScoreCallbacks.OnScoreChanged onScoreChanged;

    @ZenProperty
    public ScoreCallbacks.OnServerTick onServerTick;

    @ZenProperty
    public ScoreCallbacks.OnClientTick onClientTick;

    @ZenProperty
    public ScoreCallbacks.OnBreakSpeed onBreakSpeed;

    @ZenProperty
    public ScoreCallbacks.OnIncomingDamage onIncomingDamage;

    @ZenProperty
    public ScoreCallbacks.OnAttackTarget onAttackTarget;

    @ZenProperty
    public ScoreCallbacks.OnJump onJump;

    @ZenProperty
    public ScoreCallbacks.OnEat onEat;

    @ZenProperty
    public ScoreCallbacks.OnPotionIncoming onPotionIncoming;

    public ScoreRepresentation(String id) {
        this.id = CtCompatUtil.requireId(id, "score");
    }

    @ZenMethod
    public void register() {
        ScriptScoreCallbacks callbacks = new ScriptScoreCallbacks();
        callbacks.set(ScriptScoreEvent.SCORE_CHANGED, onScoreChanged);
        callbacks.set(ScriptScoreEvent.SERVER_TICK, onServerTick);
        callbacks.set(ScriptScoreEvent.CLIENT_TICK, onClientTick);
        callbacks.set(ScriptScoreEvent.BREAK_SPEED, onBreakSpeed);
        callbacks.set(ScriptScoreEvent.INCOMING_DAMAGE, onIncomingDamage);
        callbacks.set(ScriptScoreEvent.ATTACK_TARGET, onAttackTarget);
        callbacks.set(ScriptScoreEvent.JUMP, onJump);
        callbacks.set(ScriptScoreEvent.EAT, onEat);
        callbacks.set(ScriptScoreEvent.POTION_INCOMING, onPotionIncoming);
        ScriptScoreDefinition definition = new ScriptScoreDefinition(
                id,
                translationKey,
                displayName,
                negative,
                sortOrder,
                callbacks);
        ScriptScoreRegistry.register(definition);
        CtCompatUtil.logRegistration("Registered Chest Cavity script score %s", id);
    }

    public ResourceLocation getId() {
        return id;
    }
}
