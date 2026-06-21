package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

final class OrganAttributeController {

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("1187ab41-0e24-42bb-a39d-fb3b5b5492d5");
    private static final UUID STRENGTH_MODIFIER_ID = UUID.fromString("90d594f2-eaf5-4dc4-b970-fd2e48c83328");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("12a770fb-3062-4d2e-b921-a9a139882aa3");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("709e3e77-0586-4304-80b5-d28bc477e947");
    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("1dd5473d-d43b-4cf1-8600-f11372c4959a");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("b54ff8c5-fb1d-40eb-9d41-c02580505470");
    private static final UUID SWIM_SPEED_MODIFIER_ID = UUID.fromString("32d5f52b-796a-4194-a8e3-1acb45f5a365");
    private static final int REFRESH_INTERVAL_TICKS = 20;

    private OrganAttributeController() {
    }

    static boolean shouldRefresh(EntityLivingBase entity, IChestCavity chestCavity, boolean scoreChanges) {
        if (chestCavity instanceof ChestCavityData) {
            ChestCavityData data = (ChestCavityData) chestCavity;
            if (scoreChanges) {
                data.markAttributeModifiersDirty();
            }
            return data.shouldRefreshAttributeModifiers(entity.ticksExisted, REFRESH_INTERVAL_TICKS);
        }
        return true;
    }

    static void apply(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH),
                HEALTH_MODIFIER_ID,
                "Chest Cavity health",
                (chestCavity.getOrganScore(CCOrganScores.HEALTH) - type.getDefaultOrganScore(CCOrganScores.HEALTH)) * CCConfig.HEART_HP);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE),
                STRENGTH_MODIFIER_ID,
                "Chest Cavity strength",
                (chestCavity.getOrganScore(CCOrganScores.STRENGTH) - type.getDefaultOrganScore(CCOrganScores.STRENGTH)) * CCConfig.MUSCLE_STRENGTH / 8.0F,
                1);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED),
                SPEED_MODIFIER_ID,
                "Chest Cavity speed",
                (chestCavity.getOrganScore(CCOrganScores.SPEED) - type.getDefaultOrganScore(CCOrganScores.SPEED)) * CCConfig.MUSCLE_SPEED / 8.0F,
                1);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),
                ATTACK_SPEED_MODIFIER_ID,
                "Chest Cavity attack speed",
                (chestCavity.getOrganScore(CCOrganScores.NERVES) - type.getDefaultOrganScore(CCOrganScores.NERVES)) * CCConfig.NERVES_HASTE,
                1);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.LUCK),
                LUCK_MODIFIER_ID,
                "Chest Cavity luck",
                (chestCavity.getOrganScore(CCOrganScores.LUCK) - type.getDefaultOrganScore(CCOrganScores.LUCK)) * CCConfig.APPENDIX_LUCK);
        applyScoreModifier(entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE),
                KNOCKBACK_RESISTANCE_MODIFIER_ID,
                "Chest Cavity knockback resistance",
                Math.max(0.0F, chestCavity.getOrganScore(CCOrganScores.KNOCKBACK_RESISTANT) - type.getDefaultOrganScore(CCOrganScores.KNOCKBACK_RESISTANT)) * 0.1F);
        applyScoreModifier(entity.getEntityAttribute(EntityLivingBase.SWIM_SPEED),
                SWIM_SPEED_MODIFIER_ID,
                "Chest Cavity swim speed",
                Math.max(-0.95F, (chestCavity.getOrganScore(CCOrganScores.SWIM_SPEED)
                        - type.getDefaultOrganScore(CCOrganScores.SWIM_SPEED)) * CCConfig.SWIMSPEED_FACTOR / 8.0F));

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static void applyScoreModifier(IAttributeInstance attribute, UUID id, String name, float amount) {
        applyScoreModifier(attribute, id, name, amount, 0);
    }

    private static void applyScoreModifier(IAttributeInstance attribute, UUID id, String name, float amount, int operation) {
        if (attribute == null) {
            return;
        }

        AttributeModifier oldModifier = attribute.getModifier(id);
        if (oldModifier != null && oldModifier.getAmount() == amount && oldModifier.getOperation() == operation) {
            return;
        }
        if (oldModifier != null) {
            attribute.removeModifier(oldModifier);
        }
        if (amount != 0.0F) {
            attribute.applyModifier(new AttributeModifier(id, name, amount, operation));
        }
    }
}
