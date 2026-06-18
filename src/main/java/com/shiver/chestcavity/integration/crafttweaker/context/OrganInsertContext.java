package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(CtConstants.CT_NAMESPACE + "OrganInsertContext")
@ZenRegister
public class OrganInsertContext {

    private final EntityLivingBase entity;
    private final int slot;
    private final ItemStack stack;
    private final ScriptDataRuntime scriptData;
    private boolean cancelled;

    public OrganInsertContext(EntityLivingBase entity, int slot, ItemStack stack, ScriptDataRuntime scriptData) {
        this.entity = entity;
        this.slot = slot;
        this.stack = stack;
        this.scriptData = scriptData;
    }

    @ZenGetter("entity")
    public EntityLivingBase getEntity() {
        return entity;
    }

    @ZenGetter("world")
    public World getWorld() {
        return entity == null ? null : entity.world;
    }

    @ZenGetter("slot")
    public int getSlot() {
        return slot;
    }

    @ZenGetter("stack")
    public ItemStack getStack() {
        return stack;
    }

    @ZenGetter("scriptData")
    public ScriptDataRuntime getScriptData() {
        return scriptData;
    }

    @ZenGetter("cancelled")
    public boolean isCancelled() {
        return cancelled;
    }

    @ZenMethod
    public void cancel() {
        this.cancelled = true;
    }
}
