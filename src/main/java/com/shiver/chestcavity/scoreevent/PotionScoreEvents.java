package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PotionScoreEvents {

    private static final Field POTION_EFFECT_DURATION_FIELD = findPotionEffectDurationField();
    private static final PotionAdjustmentSpec[] POTION_ADJUSTMENTS = new PotionAdjustmentSpec[] {
            PotionScoreEvents::buffPurging,
            PotionScoreEvents::withered,
            PotionScoreEvents::poisonFiltration,
            PotionScoreEvents::detoxification
    };

    private PotionScoreEvents() {
    }

    @SubscribeEvent
    public static void potionApplicable(PotionEvent.PotionApplicableEvent event) {
        adjustIncomingPotionEffect(event.getEntityLiving(), event.getPotionEffect());
    }

    private static void adjustIncomingPotionEffect(EntityLivingBase entity, PotionEffect effect) {
        if (entity == null || effect == null || effect.getPotion() == null || effect.getDuration() <= 1) {
            return;
        }

        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        PotionContext context = new PotionContext(entity, chestCavity, effect);
        float factor = 1.0F;
        for (PotionAdjustmentSpec spec : POTION_ADJUSTMENTS) {
            factor *= spec.factor(context);
        }

        if (factor != 1.0F) {
            setPotionDuration(effect, Math.max(1, Math.round(effect.getDuration() * factor)));
        }
    }

    private static float buffPurging(PotionContext context) {
        return context.effect.getPotion().isBeneficial()
                ? durationReductionFactor(context.chestCavity.getOrganScore(CCOrganScores.BUFF_PURGING), CCConfig.BUFF_PURGING_DURATION_FACTOR)
                : 1.0F;
    }

    private static float withered(PotionContext context) {
        return context.effect.getPotion() == MobEffects.WITHER
                ? durationReductionFactor(context.chestCavity.getOrganScore(CCOrganScores.WITHERED), CCConfig.WITHERED_DURATION_FACTOR)
                : 1.0F;
    }

    private static float poisonFiltration(PotionContext context) {
        return context.effect.getPotion() == MobEffects.POISON
                ? durationReductionFactor(context.runtime.getDeltaScoreValue(CCOrganScores.FILTRATION), CCConfig.FILTRATION_DURATION_FACTOR)
                : 1.0F;
    }

    private static float detoxification(PotionContext context) {
        if (!context.effect.getPotion().isBadEffect()) {
            return 1.0F;
        }
        float defaultDetoxification = context.runtime.getBaselineScoreValue(CCOrganScores.DETOXIFICATION);
        float detoxification = context.runtime.getScoreValue(CCOrganScores.DETOXIFICATION);
        if (defaultDetoxification <= 0.0F || detoxification == defaultDetoxification) {
            return 1.0F;
        }

        float ratio = detoxification / defaultDetoxification;
        return ratio > -1.0F ? Math.max(0.05F, 2.0F / (1.0F + ratio)) : 9999.0F;
    }

    private static float durationReductionFactor(float score, float scalar) {
        return score <= 0.0F ? 1.0F : 1.0F / (1.0F + scalar * score);
    }

    private static void setPotionDuration(PotionEffect effect, int duration) {
        if (POTION_EFFECT_DURATION_FIELD == null) {
            return;
        }
        try {
            POTION_EFFECT_DURATION_FIELD.setInt(effect, duration);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static Field findPotionEffectDurationField() {
        try {
            Field field = ReflectionHelper.findField(PotionEffect.class, "duration", "field_76460_b");
            field.setAccessible(true);
            return field;
        } catch (ReflectionHelper.UnableToFindFieldException ignored) {
            return null;
        }
    }

    private interface PotionAdjustmentSpec {
        float factor(PotionContext context);
    }

    private static final class PotionContext {
        private final EntityLivingBase entity;
        private final ChestCavityData chestCavity;
        private final ChestCavityRuntime runtime;
        private final PotionEffect effect;

        private PotionContext(EntityLivingBase entity, ChestCavityData chestCavity, PotionEffect effect) {
            this.entity = entity;
            this.chestCavity = chestCavity;
            this.runtime = chestCavity.getRuntime();
            this.effect = effect;
        }
    }
}
