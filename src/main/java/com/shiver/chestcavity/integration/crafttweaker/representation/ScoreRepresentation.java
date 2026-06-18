package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.integration.crafttweaker.callback.ScoreCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScoreCallbackSet;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScoreDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScoreEvent;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScoreRegistry;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass(CtConstants.CT_NAMESPACE + "ScoreRepresentation")
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
    public ScoreCallbacks.OnValueChangedContext onValueChangedContext;

    @ZenProperty
    public ScoreCallbacks.OnBecameActiveContext onBecameActiveContext;

    @ZenProperty
    public ScoreCallbacks.OnBecameInactiveContext onBecameInactiveContext;

    @ZenProperty
    public ScoreCallbacks.OnServerTickContext onServerTickContext;

    @ZenProperty
    public ScoreCallbacks.OnClientTickContext onClientTickContext;

    @ZenProperty
    public ScoreCallbacks.OnBreakSpeedContext onBreakSpeedContext;

    @ZenProperty
    public ScoreCallbacks.OnIncomingDamageContext onIncomingDamageContext;

    @ZenProperty
    public ScoreCallbacks.OnAttackTargetContext onAttackTargetContext;

    @ZenProperty
    public ScoreCallbacks.OnJumpContext onJumpContext;

    @ZenProperty
    public ScoreCallbacks.OnEatContext onEatContext;

    @ZenProperty
    public ScoreCallbacks.OnPotionIncomingContext onPotionIncomingContext;

    public ScoreRepresentation(String id) {
        this.id = CtUtil.requireId(id, "score");
    }

    @ZenMethod
    public void register() {
        ScoreCallbackSet callbacks = new ScoreCallbackSet();
        callbacks.set(ScoreEvent.BECAME_ACTIVE, onBecameActiveContext);
        callbacks.set(ScoreEvent.BECAME_INACTIVE, onBecameInactiveContext);
        callbacks.set(ScoreEvent.SCORE_CHANGED, onValueChangedContext);
        callbacks.set(ScoreEvent.SERVER_TICK, onServerTickContext);
        callbacks.set(ScoreEvent.CLIENT_TICK, onClientTickContext);
        callbacks.set(ScoreEvent.BREAK_SPEED, onBreakSpeedContext);
        callbacks.set(ScoreEvent.INCOMING_DAMAGE, onIncomingDamageContext);
        callbacks.set(ScoreEvent.ATTACK_TARGET, onAttackTargetContext);
        callbacks.set(ScoreEvent.JUMP, onJumpContext);
        callbacks.set(ScoreEvent.EAT, onEatContext);
        callbacks.set(ScoreEvent.POTION_INCOMING, onPotionIncomingContext);
        ScoreRegistry.register(new ScoreDefinition(id, translationKey, displayName, negative, sortOrder, callbacks));
        CtUtil.logRegistration("Registered Chest Cavity script score %s", id);
    }
}
