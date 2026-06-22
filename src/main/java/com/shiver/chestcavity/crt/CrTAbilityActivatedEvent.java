package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.world.MCWorld;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 暴露给 ZenScript 的“主动能力已触发”事件对象。
 */
@ZenRegister
@ZenClass("mods.chestcavity.event.AbilityActivatedEvent")
public final class CrTAbilityActivatedEvent {

    private final EntityLivingBase entity;
    private final String abilityId;
    private final float score;
    private boolean canceled;

    /**
     * 使用活体实体创建一个能力触发事件。
     *
     * @param entity 触发能力的实体。
     * @param abilityId 能力标识。
     * @param score 触发时的能力分数。
     */
    CrTAbilityActivatedEvent(EntityLivingBase entity, String abilityId, float score) {
        this.entity = entity;
        this.abilityId = abilityId;
        this.score = score;
    }

    /**
     * 使用玩家实体创建一个能力触发事件。
     *
     * @param player 触发能力的玩家。
     * @param abilityId 能力标识。
     * @param score 触发时的能力分数。
     */
    CrTAbilityActivatedEvent(EntityPlayerMP player, String abilityId, float score) {
        this((EntityLivingBase) player, abilityId, score);
    }

    /**
     * 返回触发能力的实体。
     *
     * @return 触发能力的实体。
     */
    @ZenGetter("entity")
    public IEntityLivingBase getEntity() {
        return CrTUtil.living(entity);
    }

    /**
     * 返回触发能力的玩家；若实体不是玩家则返回 `null`。
     *
     * @return 触发能力的玩家。
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
     * 返回被触发的能力标识。
     *
     * @return 能力标识。
     */
    @ZenGetter("abilityId")
    public String getAbilityId() {
        return abilityId;
    }

    /**
     * 返回触发该能力时的分数值。
     *
     * @return 能力分数。
     */
    @ZenGetter("score")
    public float getScore() {
        return score;
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

    /**
     * 判断该事件是否已被脚本取消。
     *
     * @return `true` 表示事件已取消。
     */
    @ZenGetter("canceled")
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * 取消本次能力触发。
     */
    @ZenMethod
    public void cancel() {
        this.canceled = true;
    }
}
