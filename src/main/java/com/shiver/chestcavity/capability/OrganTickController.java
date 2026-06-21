package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

final class OrganTickController {

    private static final DamageSource HEART_BLEED_DAMAGE = new DamageSource("cc_heartbleed").setDamageBypassesArmor();
    private static final int HYDROPHOBIA_INTERVAL_TICKS = 20;

    private OrganTickController() {
    }

    static void tick(EntityLivingBase entity, IChestCavity chestCavity) {
        ensureOrganScoresUpToDate(chestCavity);
        boolean scoreChanges = ChestCavityHelper.hasScoreChanges(chestCavity);

        if (!entity.world.isRemote) {
            if (OrganAttributeController.shouldRefresh(entity, chestCavity, scoreChanges)) {
                OrganAttributeController.apply(entity, chestCavity);
            }
            if (scoreChanges && chestCavity.getOldOrganScore(CCOrganScores.INCOMPATIBILITY) != chestCavity.getOrganScore(CCOrganScores.INCOMPATIBILITY)) {
                entity.removePotionEffect(CCPotions.ORGAN_REJECTION);
            }
            tickBasicSurvival(entity, chestCavity);
            tickFiltration(entity, chestCavity);
            tickBreathing(entity, chestCavity);
            tickMetabolism(entity, chestCavity);
            tickProjectileQueue(entity, chestCavity);
            tickPassiveEffects(entity, chestCavity);
            tickOrganRejection(entity, chestCavity);
        }

        if (scoreChanges) {
            onScoreChanged(chestCavity);
            chestCavity.copyCurrentScoresToOld();
            if (!entity.world.isRemote) {
                ChestCavityNetwork.sendChestCavitySync(entity);
            }
        }
    }

    static void disconnectCrystal(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        EntityEnderCrystal crystal = owner == null ? null : getConnectedCrystal(owner, chestCavity);
        if (crystal != null) {
            crystal.setBeamTarget(null);
        }
        chestCavity.setConnectedCrystalId(-1);
    }

    private static void ensureOrganScoresUpToDate(IChestCavity chestCavity) {
        if (chestCavity instanceof ChestCavityData) {
            ChestCavityData data = (ChestCavityData) chestCavity;
            if (!data.needsScoreRecalculation(DataLoaders.getDataVersion())) {
                return;
            }
        }
        ChestCavityHelper.recalculateOrganScores(chestCavity);
    }

    private static void tickOrganRejection(EntityLivingBase entity, IChestCavity chestCavity) {
        if (CCConfig.DISABLE_ORGAN_REJECTION) {
            if (entity.isPotionActive(CCPotions.ORGAN_REJECTION)) {
                entity.removePotionEffect(CCPotions.ORGAN_REJECTION);
            }
            return;
        }

        float incompatibility = chestCavity.getOrganScore(CCOrganScores.INCOMPATIBILITY);
        if (incompatibility <= 0.0F) {
            if (entity.isPotionActive(CCPotions.ORGAN_REJECTION)) {
                entity.removePotionEffect(CCPotions.ORGAN_REJECTION);
            }
            return;
        }

        if (!entity.isPotionActive(CCPotions.ORGAN_REJECTION)) {
            int duration = Math.max(1, (int) (CCConfig.ORGAN_REJECTION_RATE / incompatibility));
            entity.addPotionEffect(new PotionEffect(CCPotions.ORGAN_REJECTION, duration, 0, false, true));
        }
    }

    private static void tickFiltration(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float defaultFiltration = type.getDefaultOrganScore(CCOrganScores.FILTRATION);
        if (!chestCavity.isOpened() || defaultFiltration <= 0.0F) {
            chestCavity.setBloodPoisonTimer(0);
            return;
        }

        float ratio = chestCavity.getOrganScore(CCOrganScores.FILTRATION) / defaultFiltration;
        if (ratio >= 1.0F) {
            chestCavity.setBloodPoisonTimer(0);
            return;
        }

        int timer = chestCavity.getBloodPoisonTimer() + 1;
        if (timer >= CCConfig.KIDNEY_RATE) {
            int duration = Math.max(1, (int) (48.0F * (1.0F - ratio)));
            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, duration, 0, false, true));
            timer = 0;
        }
        chestCavity.setBloodPoisonTimer(timer);
    }

    private static void tickBreathing(EntityLivingBase entity, IChestCavity chestCavity) {
        if (!chestCavity.isOpened()) {
            chestCavity.setLungRemainder(0.0F);
            return;
        }

        float capacity = chestCavity.getOrganScore(CCOrganScores.BREATH_CAPACITY);
        float waterBreath = chestCavity.getOrganScore(CCOrganScores.WATER_BREATH);

        if (entity.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)) {
            float airLoss = capacity <= 0.0F ? 20.0F : Math.min(2.0F / capacity, 20.0F);
            airLoss -= waterBreath * 2.0F;
            float delta = airLoss - 1.0F + chestCavity.getLungRemainder();
            int whole = (int) delta;
            chestCavity.setLungRemainder(delta - whole);
            if (whole != 0) {
                entity.setAir(Math.min(300, Math.max(-20, entity.getAir() - whole)));
            }
            return;
        }

        chestCavity.setLungRemainder(0.0F);
    }

    private static void tickMetabolism(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity instanceof EntityPlayer) {
            OrganFoodController.tickMetabolism((EntityPlayer) entity, chestCavity);
        }
    }

    private static void tickPassiveEffects(EntityLivingBase entity, IChestCavity chestCavity) {
        float glowing = chestCavity.getOrganScore(CCOrganScores.GLOWING);
        if (glowing > 0.0F && !entity.isPotionActive(MobEffects.GLOWING)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0, false, true));
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        applyLightweight(entity, chestCavity, type);

        float buoyant = chestCavity.getOrganScore(CCOrganScores.BUOYANT)
                - type.getDefaultOrganScore(CCOrganScores.BUOYANT);
        if (buoyant > 0.0F && !entity.onGround && !entity.hasNoGravity()) {
            entity.motionY += buoyant * CCConfig.BUOYANCY_LIFT * Math.max(0.0F, entity.getAir() / 300.0F);
            entity.velocityChanged = true;
        }

        float hydroallergenic = chestCavity.getOrganScore(CCOrganScores.HYDROALLERGENIC);
        if (hydroallergenic > 0.0F && entity.isWet()) {
            int amplifier = Math.max(1, Math.round(hydroallergenic * 10));
            PotionEffect active = entity.getActivePotionEffect(CCPotions.WATER_VULNERABILITY);
            if (active == null || active.getAmplifier() != amplifier) {
                entity.addPotionEffect(new PotionEffect(CCPotions.WATER_VULNERABILITY, 32767, amplifier, false, true));
            }
        } else if (entity.isPotionActive(CCPotions.WATER_VULNERABILITY)) {
            entity.removePotionEffect(CCPotions.WATER_VULNERABILITY);
        }

        float hydrophobia = chestCavity.getOrganScore(CCOrganScores.HYDROPHOBIA);
        if (hydrophobia > 0.0F
                && type.getDefaultOrganScore(CCOrganScores.HYDROPHOBIA) <= 0.0F
                && entity.isWet()
                && entity.ticksExisted % HYDROPHOBIA_INTERVAL_TICKS == 0) {
            OrganMovementController.attemptRandomTeleport(entity, hydrophobia * CCConfig.ARROW_DODGE_DISTANCE);
        }

        tickPhotosynthesis(entity, chestCavity, type);
        tickCrystalsynthesis(entity, chestCavity);
    }

    private static void tickProjectileQueue(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity.ticksExisted % 5 != 0) {
            return;
        }
        String abilityId = chestCavity.pollProjectileAbility();
        if (abilityId != null) {
            ActiveOrganAbilities.fireQueuedProjectile(entity, chestCavity, abilityId);
        }
    }

    private static void onScoreChanged(IChestCavity chestCavity) {
        if (chestCavity.getOrganScore(CCOrganScores.FILTRATION) >= chestCavity.getOldOrganScore(CCOrganScores.FILTRATION)) {
            chestCavity.setBloodPoisonTimer(0);
        }
        if (chestCavity.getOrganScore(CCOrganScores.HEALTH) > 0.0F) {
            chestCavity.setHeartBleedTimer(0);
        }
    }

    private static void tickBasicSurvival(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float defaultHealth = type.getDefaultOrganScore(CCOrganScores.HEALTH);
        boolean missingRequiredHeart = chestCavity.isOpened()
                && defaultHealth > 0.0F
                && chestCavity.getOrganScore(CCOrganScores.HEALTH) <= 0.0F;

        if (!missingRequiredHeart || entity.getHealth() <= 0.0F) {
            if (chestCavity.getHeartBleedTimer() != 0) {
                chestCavity.setHeartBleedTimer(0);
            }
            return;
        }

        if (entity.ticksExisted % CCConfig.HEARTBLEED_RATE != 0) {
            return;
        }

        int bleedLevel = chestCavity.getHeartBleedTimer() + 1;
        chestCavity.setHeartBleedTimer(bleedLevel);
        int cap = ChestCavityHelper.getChestCavityType(chestCavity).getHeartBleedCap();
        entity.attackEntityFrom(HEART_BLEED_DAMAGE, cap == Integer.MAX_VALUE ? bleedLevel : Math.min(bleedLevel, cap));
    }

    private static void applyLightweight(EntityLivingBase entity, IChestCavity chestCavity, ChestCavityType type) {
        if (entity.onGround || entity.hasNoGravity() || entity.isInWater() || entity.isInLava() || entity.motionY >= 0.0D) {
            return;
        }

        float diff = chestCavity.getOrganScore(CCOrganScores.LIGHTWEIGHT)
                - type.getDefaultOrganScore(CCOrganScores.LIGHTWEIGHT);
        if (diff == 0.0F) {
            return;
        }

        double factor = diff > 0.0F
                ? 1.0D / (1.0D + diff * CCConfig.LIGHTWIEGHT_FACTOR)
                : 1.0D - diff * CCConfig.LIGHTWIEGHT_FACTOR;
        factor = Math.max(0.1D, Math.min(2.5D, factor));
        entity.motionY *= factor;
        entity.fallDistance *= factor;
        entity.velocityChanged = true;
    }

    private static void tickPhotosynthesis(EntityLivingBase entity, IChestCavity chestCavity, ChestCavityType type) {
        float photosynthesis = chestCavity.getOrganScore(CCOrganScores.PHOTOSYNTHESIS)
                - type.getDefaultOrganScore(CCOrganScores.PHOTOSYNTHESIS);
        if (photosynthesis <= 0.0F) {
            chestCavity.setPhotosynthesisProgress(0);
            return;
        }

        int light = entity.world.getLight(new BlockPos(entity));
        if (light <= 0) {
            return;
        }

        int progress = chestCavity.getPhotosynthesisProgress() + Math.max(1, Math.round(photosynthesis * light));
        int threshold = Math.max(1, CCConfig.PHOTOSYNTHESIS_FREQUENCY * 8 * 15);
        if (progress < threshold) {
            chestCavity.setPhotosynthesisProgress(progress);
            return;
        }

        chestCavity.setPhotosynthesisProgress(0);
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            FoodStats foodStats = player.getFoodStats();
            if (foodStats.needFood()) {
                foodStats.addStats(1, 0.0F);
            } else if (foodStats.getSaturationLevel() < 20.0F) {
                foodStats.addStats(1, 0.5F);
            } else if (player.shouldHeal()) {
                player.heal(1.0F);
            }
        } else if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    private static void tickCrystalsynthesis(EntityLivingBase entity, IChestCavity chestCavity) {
        float crystalsynthesis = chestCavity.getOrganScore(CCOrganScores.CRYSTALSYNTHESIS);
        EntityEnderCrystal connectedCrystal = getConnectedCrystal(entity, chestCavity);
        if (connectedCrystal != null) {
            if (crystalsynthesis > 0.0F) {
                connectedCrystal.setBeamTarget(new BlockPos(entity).down(2));
            } else {
                disconnectCrystal(chestCavity);
            }
        } else if (chestCavity.getConnectedCrystalId() >= 0) {
            entity.attackEntityFrom(DamageSource.STARVE, crystalsynthesis * 2.0F);
            chestCavity.setConnectedCrystalId(-1);
        }

        if (crystalsynthesis <= 0.0F || entity instanceof EntityDragon
                || entity.ticksExisted % Math.max(1, CCConfig.CRYSTALSYNTHESIS_FREQUENCY) != 0) {
            return;
        }

        connectedCrystal = findNearestCrystal(entity);
        if (connectedCrystal == null) {
            disconnectCrystal(chestCavity);
            return;
        }
        EntityEnderCrystal oldCrystal = getConnectedCrystal(entity, chestCavity);
        if (oldCrystal != null && oldCrystal != connectedCrystal) {
            oldCrystal.setBeamTarget(null);
        }
        chestCavity.setConnectedCrystalId(connectedCrystal.getEntityId());
        connectedCrystal.setBeamTarget(new BlockPos(entity).down(2));

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            FoodStats foodStats = player.getFoodStats();
            long time = entity.world.getTotalWorldTime();
            if (foodStats.needFood()) {
                if (crystalsynthesis >= 5.0F
                        || time % (Math.max(1, CCConfig.CRYSTALSYNTHESIS_FREQUENCY) * 5L) < Math.max(1, CCConfig.CRYSTALSYNTHESIS_FREQUENCY) * crystalsynthesis) {
                    foodStats.addStats(1, 0.0F);
                }
            } else if (foodStats.getSaturationLevel() < foodStats.getFoodLevel()) {
                foodStats.addStats(1, crystalsynthesis / 10.0F);
            } else if (player.shouldHeal()) {
                player.heal(crystalsynthesis / 5.0F);
            }
            return;
        }

        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(crystalsynthesis / 5.0F);
        }
    }

    private static EntityEnderCrystal getConnectedCrystal(EntityLivingBase entity, IChestCavity chestCavity) {
        int crystalId = chestCavity.getConnectedCrystalId();
        if (crystalId < 0 || entity.world == null) {
            return null;
        }
        Entity entityById = entity.world.getEntityByID(crystalId);
        return entityById instanceof EntityEnderCrystal && entityById.isEntityAlive()
                ? (EntityEnderCrystal) entityById
                : null;
    }

    private static EntityEnderCrystal findNearestCrystal(EntityLivingBase entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox().grow(CCConfig.CRYSTALSYNTHESIS_RANGE);
        List<EntityEnderCrystal> crystals = entity.world.getEntitiesWithinAABB(EntityEnderCrystal.class, box);
        EntityEnderCrystal nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityEnderCrystal crystal : crystals) {
            double distance = crystal.getDistanceSq(entity);
            if (distance < nearestDistance) {
                nearest = crystal;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
