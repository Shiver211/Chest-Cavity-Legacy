package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.integration.crafttweaker.callback.AbilityCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityRegistry;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass(CtConstants.CT_NAMESPACE + "AbilityRepresentation")
@ZenRegister
public class AbilityRepresentation {

    private final ResourceLocation id;

    @ZenProperty
    public String translationKey;

    @ZenProperty
    public String displayName;

    @ZenProperty
    public boolean wheelVisible = true;

    @ZenProperty
    public int sortOrder;

    @ZenProperty
    public AbilityCallbacks.OnActivateContext onActivateContext;

    @ZenProperty
    public AbilityCallbacks.CanActivateContext canActivateContext;

    @ZenProperty
    public AbilityCallbacks.GetCooldownContext getCooldownContext;

    @ZenProperty
    public AbilityCallbacks.GetCostContext getCostContext;

    @ZenProperty
    public AbilityCallbacks.OnActivateServerContext onActivateServerContext;

    @ZenProperty
    public AbilityCallbacks.OnActivateClientContext onActivateClientContext;

    @ZenProperty
    public AbilityCallbacks.OnActiveTickContext onActiveTickContext;

    @ZenProperty
    public AbilityCallbacks.OnEndContext onEndContext;

    public AbilityRepresentation(String id) {
        this.id = CtUtil.requireId(id, "ability");
    }

    @ZenMethod
    public void register() {
        AbilityRegistry.register(new AbilityDefinition(
                id,
                translationKey,
                displayName,
                wheelVisible,
                sortOrder,
                onActivateContext,
                canActivateContext,
                getCooldownContext,
                getCostContext,
                onActivateServerContext,
                onActiveTickContext,
                onEndContext,
                onActivateClientContext));
        CtUtil.logRegistration("Registered Chest Cavity script ability %s", id);
    }
}
