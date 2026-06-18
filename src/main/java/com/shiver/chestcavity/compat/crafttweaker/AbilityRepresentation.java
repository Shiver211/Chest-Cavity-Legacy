package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.script.model.ScriptAbilityDefinition;
import com.shiver.chestcavity.script.registry.ScriptAbilityRegistry;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "AbilityRepresentation")
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
    public AbilityCallbacks.OnActivate onActivate;

    public AbilityRepresentation(String id) {
        this.id = CtCompatUtil.requireId(id, "ability");
    }

    @ZenMethod
    public void register() {
        ScriptAbilityDefinition definition = new ScriptAbilityDefinition(
                id,
                translationKey,
                displayName,
                wheelVisible,
                sortOrder,
                onActivate);
        ScriptAbilityRegistry.register(definition);
        CtCompatUtil.logRegistration("Registered Chest Cavity script ability %s", id);
    }

    public ResourceLocation getId() {
        return id;
    }
}
