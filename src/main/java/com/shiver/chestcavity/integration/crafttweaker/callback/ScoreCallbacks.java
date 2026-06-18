package com.shiver.chestcavity.integration.crafttweaker.callback;

import com.shiver.chestcavity.integration.crafttweaker.context.AttackContext;
import com.shiver.chestcavity.integration.crafttweaker.context.BreakSpeedContext;
import com.shiver.chestcavity.integration.crafttweaker.context.DamageContext;
import com.shiver.chestcavity.integration.crafttweaker.context.EatContext;
import com.shiver.chestcavity.integration.crafttweaker.context.JumpContext;
import com.shiver.chestcavity.integration.crafttweaker.context.PotionContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreChangeContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreTickContext;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

public final class ScoreCallbacks {

    private ScoreCallbacks() {
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnBecameActiveContext")
    @ZenRegister
    public interface OnBecameActiveContext {
        void handle(ScoreChangeContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnBecameInactiveContext")
    @ZenRegister
    public interface OnBecameInactiveContext {
        void handle(ScoreChangeContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnValueChangedContext")
    @ZenRegister
    public interface OnValueChangedContext {
        void handle(ScoreChangeContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnServerTickContext")
    @ZenRegister
    public interface OnServerTickContext {
        void handle(ScoreTickContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnClientTickContext")
    @ZenRegister
    public interface OnClientTickContext {
        void handle(ScoreTickContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnBreakSpeedContext")
    @ZenRegister
    public interface OnBreakSpeedContext {
        void handle(BreakSpeedContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnIncomingDamageContext")
    @ZenRegister
    public interface OnIncomingDamageContext {
        void handle(DamageContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnAttackTargetContext")
    @ZenRegister
    public interface OnAttackTargetContext {
        void handle(AttackContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnJumpContext")
    @ZenRegister
    public interface OnJumpContext {
        void handle(JumpContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnEatContext")
    @ZenRegister
    public interface OnEatContext {
        void handle(EatContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnPotionIncomingContext")
    @ZenRegister
    public interface OnPotionIncomingContext {
        void handle(PotionContext context);
    }
}
