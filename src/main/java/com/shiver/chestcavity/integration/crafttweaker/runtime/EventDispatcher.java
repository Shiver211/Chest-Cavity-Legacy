package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.integration.crafttweaker.callback.ScoreCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.context.AttackContext;
import com.shiver.chestcavity.integration.crafttweaker.context.BreakSpeedContext;
import com.shiver.chestcavity.integration.crafttweaker.context.DamageContext;
import com.shiver.chestcavity.integration.crafttweaker.context.EatContext;
import com.shiver.chestcavity.integration.crafttweaker.context.JumpContext;
import com.shiver.chestcavity.integration.crafttweaker.context.PotionContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreChangeContext;
import com.shiver.chestcavity.integration.crafttweaker.context.ScoreTickContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public final class EventDispatcher {

    private EventDispatcher() {
    }

    public static void fireScoreChanged(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> previousScores) {
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            float current = getScore(currentScores, definition.getId());
            float previous = getScore(previousScores, definition.getId());
            if (previous <= 0.0F && current > 0.0F) {
                Object becameActive = definition.getCallback(ScoreEvent.BECAME_ACTIVE);
                if (becameActive instanceof ScoreCallbacks.OnBecameActiveContext) {
                    ((ScoreCallbacks.OnBecameActiveContext) becameActive).handle(
                            new ScoreChangeContext(entity, definition.getId(), previous, current, getScoreBase(baseScoresOrFallback(entity), definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId())));
                }
            }
            if (previous > 0.0F && current <= 0.0F) {
                Object becameInactive = definition.getCallback(ScoreEvent.BECAME_INACTIVE);
                if (becameInactive instanceof ScoreCallbacks.OnBecameInactiveContext) {
                    ((ScoreCallbacks.OnBecameInactiveContext) becameInactive).handle(
                            new ScoreChangeContext(entity, definition.getId(), previous, current, getScoreBase(baseScoresOrFallback(entity), definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId())));
                }
            }

            if (current != previous) {
                Object callback = definition.getCallback(ScoreEvent.SCORE_CHANGED);
                if (callback instanceof ScoreCallbacks.OnValueChangedContext) {
                    ((ScoreCallbacks.OnValueChangedContext) callback).handle(
                            new ScoreChangeContext(entity, definition.getId(), previous, current, getScoreBase(baseScoresOrFallback(entity), definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId())));
                }
            }
        }
    }

    public static void fireTick(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, boolean client) {
        ScoreEvent event = client ? ScoreEvent.CLIENT_TICK : ScoreEvent.SERVER_TICK;
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(event);
            if (client && callback instanceof ScoreCallbacks.OnClientTickContext) {
                ((ScoreCallbacks.OnClientTickContext) callback).handle(
                        new ScoreTickContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId()), true));
            } else if (!client && callback instanceof ScoreCallbacks.OnServerTickContext) {
                ((ScoreCallbacks.OnServerTickContext) callback).handle(
                        new ScoreTickContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId()), false));
            }
        }
    }

    public static float modifyBreakSpeed(EntityPlayer entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, float currentSpeed, IBlockState blockState, BlockPos pos, ItemStack tool) {
        float result = currentSpeed;
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.BREAK_SPEED);
            if (callback instanceof ScoreCallbacks.OnBreakSpeedContext) {
                BreakSpeedContext context = new BreakSpeedContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId()), blockState, pos, tool, result);
                ((ScoreCallbacks.OnBreakSpeedContext) callback).handle(context);
                result = context.getCurrentSpeed();
            }
        }
        return result;
    }

    public static float modifyIncomingDamage(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores, DamageSource source, float damage) {
        float result = damage;
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.INCOMING_DAMAGE);
            if (callback instanceof ScoreCallbacks.OnIncomingDamageContext) {
                DamageContext context = new DamageContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId()), source, result);
                ((ScoreCallbacks.OnIncomingDamageContext) callback).handle(context);
                result = context.isCancelled() ? 0.0F : context.getCurrentDamage();
            }
        }
        return result;
    }

    public static void fireAttackTarget(EntityLivingBase attacker, EntityLivingBase target, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.ATTACK_TARGET);
            if (callback instanceof ScoreCallbacks.OnAttackTargetContext) {
                ((ScoreCallbacks.OnAttackTargetContext) callback).handle(
                        new AttackContext(attacker, target, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(attacker, definition.getId())));
            }
        }
    }

    public static void fireJump(EntityLivingBase entity, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.JUMP);
            if (callback instanceof ScoreCallbacks.OnJumpContext) {
                ((ScoreCallbacks.OnJumpContext) callback).handle(
                        new JumpContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId())));
            }
        }
    }

    public static void fireEat(EntityLivingBase entity, ItemStack food, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        if (food == null || food.isEmpty()) {
            return;
        }
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.EAT);
            if (callback instanceof ScoreCallbacks.OnEatContext) {
                ((ScoreCallbacks.OnEatContext) callback).handle(
                        new EatContext(entity, food, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId())));
            }
        }
    }

    public static int modifyPotionIncoming(EntityLivingBase entity, PotionEffect effect, Map<ResourceLocation, Float> currentScores, Map<ResourceLocation, Float> baseScores) {
        if (effect == null) {
            return 0;
        }
        int duration = effect.getDuration();
        for (ScoreDefinition definition : ScoreRegistry.getDefinitions().values()) {
            Object callback = definition.getCallback(ScoreEvent.POTION_INCOMING);
            if (callback instanceof ScoreCallbacks.OnPotionIncomingContext) {
                PotionContext context = new PotionContext(entity, definition.getId(), getScore(currentScores, definition.getId()), getScore(baseScores, definition.getId()), RuntimeStateRegistry.getScoreData(entity, definition.getId()), effect);
                ((ScoreCallbacks.OnPotionIncomingContext) callback).handle(context);
                if (context.isCancelled()) {
                    return -1;
                }
                if (effect != null && context.getCurrentAmplifier() != effect.getAmplifier()) {
                    ChestCavityHelper.setPotionAmplifier(effect, Math.max(0, context.getCurrentAmplifier()));
                }
                duration = context.getCurrentDuration();
            }
        }
        return duration;
    }

    private static java.util.Map<ResourceLocation, Float> baseScoresOrFallback(EntityLivingBase entity) {
        if (entity == null) {
            return java.util.Collections.emptyMap();
        }
        com.shiver.chestcavity.capability.IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null) {
            return java.util.Collections.emptyMap();
        }
        return ChestCavityHelper.getChestCavityType(chestCavity).getDefaultOrganScores();
    }

    private static float getScoreBase(Map<ResourceLocation, Float> baseScores, ResourceLocation id) {
        return getScore(baseScores, id);
    }

    private static float getScore(Map<ResourceLocation, Float> scores, ResourceLocation id) {
        if (scores == null || id == null) {
            return 0.0F;
        }
        Float value = scores.get(id);
        return value == null ? 0.0F : value;
    }
}
