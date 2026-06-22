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

/**
 * 抽象的器官装备变更事件基类。
 */
public abstract class CrTOrganChangeEvent {

    private final EntityLivingBase entity;
    private final int slot;
    private final ItemStack organ;
    private final boolean pseudoOrgan;

    /**
     * 创建一条器官变更事件。
     *
     * @param entity 发生变更的实体。
     * @param slot 变更槽位。
     * @param organ 变更的器官物品。
     * @param pseudoOrgan 是否为伪器官。
     */
    CrTOrganChangeEvent(EntityLivingBase entity, int slot, ItemStack organ, boolean pseudoOrgan) {
        this.entity = entity;
        this.slot = slot;
        this.organ = organ == null ? ItemStack.EMPTY : organ.copy();
        this.pseudoOrgan = pseudoOrgan;
    }

    /**
     * 返回发生变更的实体。
     *
     * @return 目标实体。
     */
    @ZenGetter("entity")
    public IEntityLivingBase getEntity() {
        return CrTUtil.living(entity);
    }

    /**
     * 返回发生变更的玩家；若实体不是玩家则返回 `null`。
     *
     * @return 目标玩家。
     */
    @ZenGetter("player")
    public IPlayer getPlayer() {
        return CrTUtil.player(entity);
    }

    /**
     * 返回事件发生时所在世界。
     *
     * @return 事件所在世界。
     */
    @ZenGetter("world")
    public IWorld getWorld() {
        return entity == null ? null : new MCWorld(entity.world);
    }

    /**
     * 返回发生变更的槽位索引。
     *
     * @return 槽位索引。
     */
    @ZenGetter("slot")
    public int getSlot() {
        return slot;
    }

    /**
     * 返回参与变更的器官物品。
     *
     * @return 器官物品。
     */
    @ZenGetter("organ")
    public IItemStack getOrgan() {
        return CrTUtil.stack(organ);
    }

    /**
     * 返回参与变更的器官物品注册名。
     *
     * @return 器官物品注册名。
     */
    @ZenGetter("organId")
    public String getOrganId() {
        if (organ.isEmpty() || organ.getItem() == null) {
            return "";
        }
        ResourceLocation id = organ.getItem().getRegistryName();
        return id == null ? "" : id.toString();
    }

    /**
     * 判断该器官是否为伪器官。
     *
     * @return `true` 表示为伪器官。
     */
    @ZenGetter("isPseudoOrgan")
    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    /**
     * 判断事件是否发生在服务端。
     *
     * @return `true` 表示当前在服务端。
     */
    @ZenGetter("isServer")
    public boolean isServer() {
        return entity != null && !entity.world.isRemote;
    }
}
