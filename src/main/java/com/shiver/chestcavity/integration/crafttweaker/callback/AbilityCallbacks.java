package com.shiver.chestcavity.integration.crafttweaker.callback;

import com.shiver.chestcavity.integration.crafttweaker.context.SkillActivateContext;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

public final class AbilityCallbacks {

    private AbilityCallbacks() {
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnActivateContext")
    @ZenRegister
    public interface OnActivateContext {
        void handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.CanActivateContext")
    @ZenRegister
    public interface CanActivateContext {
        boolean handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.GetCooldownContext")
    @ZenRegister
    public interface GetCooldownContext {
        int handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.GetCostContext")
    @ZenRegister
    public interface GetCostContext {
        float handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnActivateServerContext")
    @ZenRegister
    public interface OnActivateServerContext {
        void handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnActivateClientContext")
    @ZenRegister
    public interface OnActivateClientContext {
        void handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnActiveTickContext")
    @ZenRegister
    public interface OnActiveTickContext {
        void handle(SkillActivateContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "abilities.OnEndContext")
    @ZenRegister
    public interface OnEndContext {
        void handle(SkillActivateContext context);
    }
}
