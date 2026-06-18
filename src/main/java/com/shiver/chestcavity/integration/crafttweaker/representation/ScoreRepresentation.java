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
        this.id = CtUtil.requireId(id, "score");
    }

    @ZenMethod
    public void register() {
        ScoreCallbackSet callbacks = new ScoreCallbackSet();
        callbacks.set(ScoreEvent.SCORE_CHANGED, onScoreChanged);
        callbacks.set(ScoreEvent.SERVER_TICK, onServerTick);
        callbacks.set(ScoreEvent.CLIENT_TICK, onClientTick);
        callbacks.set(ScoreEvent.BREAK_SPEED, onBreakSpeed);
        callbacks.set(ScoreEvent.INCOMING_DAMAGE, onIncomingDamage);
        callbacks.set(ScoreEvent.ATTACK_TARGET, onAttackTarget);
        callbacks.set(ScoreEvent.JUMP, onJump);
        callbacks.set(ScoreEvent.EAT, onEat);
        callbacks.set(ScoreEvent.POTION_INCOMING, onPotionIncoming);
        ScoreRegistry.register(new ScoreDefinition(id, translationKey, displayName, negative, sortOrder, callbacks));
        CtUtil.logRegistration("Registered Chest Cavity script score %s", id);
    }
}
