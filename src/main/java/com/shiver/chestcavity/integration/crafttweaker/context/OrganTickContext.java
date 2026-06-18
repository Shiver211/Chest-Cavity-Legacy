package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass(CtConstants.CT_NAMESPACE + "OrganTickContext")
@ZenRegister
public class OrganTickContext {

    private final EntityLivingBase entity;
    private final int slot;
    private final ItemStack stack;
    private final ScriptDataRuntime scriptData;
    private final boolean client;

    public OrganTickContext(EntityLivingBase entity, int slot, ItemStack stack, ScriptDataRuntime scriptData, boolean client) {
        this.entity = entity;
        this.slot = slot;
        this.stack = stack;
        this.scriptData = scriptData;
        this.client = client;
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

    @ZenGetter("client")
    public boolean isClient() {
        return client;
    }

    @ZenGetter("server")
    public boolean isServer() {
        return !client;
    }
}
