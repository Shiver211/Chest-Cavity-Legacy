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

@ZenRegister
@ZenClass("mods.chestcavity.event.AbilityActivatedEvent")
public final class CrTAbilityActivatedEvent {

    private final EntityLivingBase entity;
    private final String abilityId;
    private final float score;

    CrTAbilityActivatedEvent(EntityLivingBase entity, String abilityId, float score) {
        this.entity = entity;
        this.abilityId = abilityId;
        this.score = score;
    }

    CrTAbilityActivatedEvent(EntityPlayerMP player, String abilityId, float score) {
        this((EntityLivingBase) player, abilityId, score);
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

    @ZenGetter("abilityId")
    public String getAbilityId() {
        return abilityId;
    }

    @ZenGetter("score")
    public float getScore() {
        return score;
    }

    @ZenGetter("isServer")
    public boolean isServer() {
        return entity != null && !entity.world.isRemote;
    }
}
