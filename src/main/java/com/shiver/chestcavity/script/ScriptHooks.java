package com.shiver.chestcavity.script;

import com.shiver.chestcavity.compat.crafttweaker.ScoreCallbacks;
import com.shiver.chestcavity.script.model.ScriptScoreDefinition;
import com.shiver.chestcavity.script.model.ScriptScoreEvent;
import com.shiver.chestcavity.script.registry.ScriptScoreRegistry;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.potions.IPotionEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public final class ScriptHooks {

    private ScriptHooks() {
    }

    public static void fireScoreChanged(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> previousScores) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return;
        }
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.SCORE_CHANGED);
            if (!(callback instanceof ScoreCallbacks.OnScoreChanged)) {
                continue;
            }
            float current = getScore(currentScores, definition.getId());
            float previous = getScore(previousScores, definition.getId());
            if (current != previous) {
                ((ScoreCallbacks.OnScoreChanged) callback).handle(ctEntity, definition.getId().toString(), current, previous);
            }
        }
    }

    public static void fireTick(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, boolean client) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return;
        }
        ScriptScoreEvent event = client ? ScriptScoreEvent.CLIENT_TICK : ScriptScoreEvent.SERVER_TICK;
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(event);
            if (client && callback instanceof ScoreCallbacks.OnClientTick) {
                ((ScoreCallbacks.OnClientTick) callback).handle(ctEntity, definition.getId().toString(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()));
            } else if (!client && callback instanceof ScoreCallbacks.OnServerTick) {
                ((ScoreCallbacks.OnServerTick) callback).handle(ctEntity, definition.getId().toString(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()));
            }
        }
    }

    public static float modifyBreakSpeed(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, float currentSpeed) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return currentSpeed;
        }
        float result = currentSpeed;
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.BREAK_SPEED);
            if (callback instanceof ScoreCallbacks.OnBreakSpeed) {
                result = ((ScoreCallbacks.OnBreakSpeed) callback).handle(
                        ctEntity,
                        definition.getId().toString(),
                        getScore(currentScores, definition.getId()),
                        getScore(baseScores, definition.getId()),
                        result);
            }
        }
        return result;
    }

    public static float modifyIncomingDamage(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, float damage) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return damage;
        }
        float result = damage;
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.INCOMING_DAMAGE);
            if (callback instanceof ScoreCallbacks.OnIncomingDamage) {
                result = ((ScoreCallbacks.OnIncomingDamage) callback).handle(
                        ctEntity,
                        definition.getId().toString(),
                        getScore(currentScores, definition.getId()),
                        getScore(baseScores, definition.getId()),
                        result);
            }
        }
        return result;
    }

    public static void fireAttackTarget(EntityLivingBase attacker, EntityLivingBase target, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        IEntityLivingBase ctAttacker = toCtEntity(attacker);
        IEntityLivingBase ctTarget = toCtEntity(target);
        if (ctAttacker == null || ctTarget == null) {
            return;
        }
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.ATTACK_TARGET);
            if (callback instanceof ScoreCallbacks.OnAttackTarget) {
                ((ScoreCallbacks.OnAttackTarget) callback).handle(ctAttacker, ctTarget, definition.getId().toString(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()));
            }
        }
    }

    public static void fireJump(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return;
        }
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.JUMP);
            if (callback instanceof ScoreCallbacks.OnJump) {
                ((ScoreCallbacks.OnJump) callback).handle(ctEntity, definition.getId().toString(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()));
            }
        }
    }

    public static void fireEat(EntityLivingBase entity, ItemStack food, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        IEntityLivingBase ctEntity = toCtEntity(entity);
        IItemStack ctFood = food == null || food.isEmpty() ? null : CraftTweakerMC.getIItemStack(food);
        if (ctEntity == null || ctFood == null) {
            return;
        }
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.EAT);
            if (callback instanceof ScoreCallbacks.OnEat) {
                ((ScoreCallbacks.OnEat) callback).handle(ctEntity, ctFood, definition.getId().toString(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()));
            }
        }
    }

    public static int modifyPotionIncoming(EntityLivingBase entity, PotionEffect effect, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        if (effect == null) {
            return 0;
        }
        IEntityLivingBase ctEntity = toCtEntity(entity);
        if (ctEntity == null) {
            return effect.getDuration();
        }
        IPotionEffect ctEffect = CraftTweakerMC.getIPotionEffect(effect);
        int duration = effect.getDuration();
        for (ScriptScoreDefinition definition : ScriptScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScriptScoreEvent.POTION_INCOMING);
            if (callback instanceof ScoreCallbacks.OnPotionIncoming) {
                duration = ((ScoreCallbacks.OnPotionIncoming) callback).handle(
                        ctEntity,
                        ctEffect,
                        definition.getId().toString(),
                        getScore(currentScores, definition.getId()),
                        getScore(baseScores, definition.getId()),
                        duration);
            }
        }
        return duration;
    }

    private static float getScore(Map<ResourceLocation, Float> scores, ResourceLocation id) {
        if (scores == null || id == null) {
            return 0.0F;
        }
        Float value = scores.get(id);
        return value == null ? 0.0F : value;
    }

    private static IEntityLivingBase toCtEntity(EntityLivingBase entity) {
        return entity == null ? null : CraftTweakerMC.getIEntityLivingBase(entity);
    }
}
