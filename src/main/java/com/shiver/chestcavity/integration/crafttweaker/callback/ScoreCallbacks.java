package com.shiver.chestcavity.integration.crafttweaker.callback;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.potions.IPotionEffect;
import stanhebben.zenscript.annotations.ZenClass;

public final class ScoreCallbacks {

    private ScoreCallbacks() {
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnScoreChanged")
    @ZenRegister
    public interface OnScoreChanged {
        void handle(IEntityLivingBase entity, String scoreId, float value, float previousValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnServerTick")
    @ZenRegister
    public interface OnServerTick {
        void handle(IEntityLivingBase entity, String scoreId, float value, float baseValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnClientTick")
    @ZenRegister
    public interface OnClientTick {
        void handle(IEntityLivingBase entity, String scoreId, float value, float baseValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnBreakSpeed")
    @ZenRegister
    public interface OnBreakSpeed {
        float handle(IEntityLivingBase entity, String scoreId, float value, float baseValue, float currentSpeed);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnIncomingDamage")
    @ZenRegister
    public interface OnIncomingDamage {
        float handle(IEntityLivingBase entity, String scoreId, float value, float baseValue, float damage);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnAttackTarget")
    @ZenRegister
    public interface OnAttackTarget {
        void handle(IEntityLivingBase attacker, IEntityLivingBase target, String scoreId, float value, float baseValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnJump")
    @ZenRegister
    public interface OnJump {
        void handle(IEntityLivingBase entity, String scoreId, float value, float baseValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnEat")
    @ZenRegister
    public interface OnEat {
        void handle(IEntityLivingBase entity, IItemStack food, String scoreId, float value, float baseValue);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "scores.OnPotionIncoming")
    @ZenRegister
    public interface OnPotionIncoming {
        int handle(IEntityLivingBase entity, IPotionEffect effect, String scoreId, float value, float baseValue, int currentDuration);
    }
}
