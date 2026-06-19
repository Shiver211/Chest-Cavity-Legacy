package com.shiver.chestcavity.crt;

import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.world.MCWorld;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenGetter;

public abstract class CrTOrganChangeEvent {

    private final EntityLivingBase entity;
    private final int slot;
    private final ItemStack organ;
    private final boolean pseudoOrgan;

    CrTOrganChangeEvent(EntityLivingBase entity, int slot, ItemStack organ, boolean pseudoOrgan) {
        this.entity = entity;
        this.slot = slot;
        this.organ = organ == null ? ItemStack.EMPTY : organ.copy();
        this.pseudoOrgan = pseudoOrgan;
    }

    @ZenGetter("entity")
    public IEntityLivingBase getEntity() {
        return CrTUtil.living(entity);
    }

    @ZenGetter("player")
    public IPlayer getPlayer() {
        return CrTUtil.player(entity);
    }

    @ZenGetter("world")
    public IWorld getWorld() {
        return entity == null ? null : new MCWorld(entity.world);
    }

    @ZenGetter("slot")
    public int getSlot() {
        return slot;
    }

    @ZenGetter("organ")
    public IItemStack getOrgan() {
        return CrTUtil.stack(organ);
    }

    @ZenGetter("organId")
    public String getOrganId() {
        if (organ.isEmpty() || organ.getItem() == null) {
            return "";
        }
        ResourceLocation id = organ.getItem().getRegistryName();
        return id == null ? "" : id.toString();
    }

    @ZenGetter("isPseudoOrgan")
    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    @ZenGetter("isServer")
    public boolean isServer() {
        return entity != null && !entity.world.isRemote;
    }
}
