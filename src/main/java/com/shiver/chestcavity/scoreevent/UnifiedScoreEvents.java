package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.effect.BodyEventContext;
import com.shiver.chestcavity.effect.TickEffect;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.score.ScoreRef;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import com.shiver.chestcavity.util.EntityMovementUtil;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class UnifiedScoreEvents {

    private static final DamageSource HEART_BLEED_DAMAGE = new DamageSource("cc_heartbleed").setDamageBypassesArmor();
    private static final String ENDURANCE_LAST_EXHAUSTION_KEY = "chestcavity:last_exhaustion";
    private static final int HYDROPHOBIA_INTERVAL_TICKS = 20;

    private static final TickEffect[] PASSIVE_TICK_EFFECTS = new TickEffect[] {
            context -> tickGlowing(context.getEntity(), context.getRuntime()),
            context -> tickHydroallergenic(context.getEntity(), context.getRuntime()),
            context -> tickHydrophobia(context.getEntity(), context.getRuntime()),
            context -> tickLightweight(context.getEntity(), context.getRuntime()),
            context -> tickBuoyancy(context.getEntity(), context.getChestCavity(), context.getRuntime()),
            context -> tickPhotosynthesis(context.getEntity(), context.getChestCavity(), context.getRuntime()),
            context -> tickCrystalsynthesis(context.getEntity(), context.getChestCavity(), context.getRuntime()),
            context -> tickOrganRejection(context.getEntity(), context.getRuntime())
    };

    private static final AttributeEffectSpec[] ATTRIBUTE_EFFECTS = new AttributeEffectSpec[] {
            new AttributeEffectSpec(CCOrganScores.HEALTH_REF, UUID.fromString("1187ab41-0e24-42bb-a39d-fb3b5b5492d5"),
                    "Chest Cavity health", entity -> entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH),
                    (entity, chestCavity, runtime, value) -> runtime.getDeltaScoreValue(CCOrganScores.HEALTH_REF) * CCConfig.HEART_HP, 0),
            new AttributeEffectSpec(CCOrganScores.LUCK_REF, UUID.fromString("1dd5473d-d43b-4cf1-8600-f11372c4959a"),
                    "Chest Cavity luck", entity -> entity.getEntityAttribute(SharedMonsterAttributes.LUCK),
                    (entity, chestCavity, runtime, value) -> runtime.getDeltaScoreValue(CCOrganScores.LUCK_REF) * CCConfig.APPENDIX_LUCK, 0),
            new AttributeEffectSpec(CCOrganScores.STRENGTH_REF, UUID.fromString("90d594f2-eaf5-4dc4-b970-fd2e48c83328"),
                    "Chest Cavity strength", entity -> entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE),
                    (entity, chestCavity, runtime, value) -> runtime.getDeltaScoreValue(CCOrganScores.STRENGTH_REF) * CCConfig.MUSCLE_STRENGTH / 8.0F, 1),
            new AttributeEffectSpec(CCOrganScores.SPEED_REF, UUID.fromString("12a770fb-3062-4d2e-b921-a9a139882aa3"),
                    "Chest Cavity speed", entity -> entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED),
                    (entity, chestCavity, runtime, value) -> runtime.getDeltaScoreValue(CCOrganScores.SPEED_REF) * CCConfig.MUSCLE_SPEED / 8.0F, 1),
            new AttributeEffectSpec(CCOrganScores.NERVES_REF, UUID.fromString("709e3e77-0586-4304-80b5-d28bc477e947"),
                    "Chest Cavity attack speed", entity -> entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),
                    (entity, chestCavity, runtime, value) -> runtime.getDeltaScoreValue(CCOrganScores.NERVES_REF) * CCConfig.NERVES_HASTE, 1),
            new AttributeEffectSpec(CCOrganScores.KNOCKBACK_RESISTANT_REF, UUID.fromString("b54ff8c5-fb1d-40eb-9d41-c02580505470"),
                    "Chest Cavity knockback resistance", entity -> entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE),
                    (entity, chestCavity, runtime, value) -> Math.max(0.0F, runtime.getDeltaScoreValue(CCOrganScores.KNOCKBACK_RESISTANT_REF)) * 0.1F, 0),
            new AttributeEffectSpec(CCOrganScores.SWIM_SPEED_REF, UUID.fromString("32d5f52b-796a-4194-a8e3-1acb45f5a365"),
                    "Chest Cavity swim speed", entity -> entity.getEntityAttribute(EntityLivingBase.SWIM_SPEED),
                    (entity, chestCavity, runtime, value) -> Math.max(-0.95F, runtime.getDeltaScoreValue(CCOrganScores.SWIM_SPEED_REF) * CCConfig.SWIMSPEED_FACTOR / 8.0F), 0)
    };

    private UnifiedScoreEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world.isRemote) {
            return;
        }
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null) {
            clearAttributeModifiers(entity);
            return;
        }

        chestCavity.refreshRuntimeIfDirty();
        ChestCavityRuntime runtime = chestCavity.getRuntime();
        ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(chestCavity);
        tickAttributes(entity, chestCavity, runtime);
        tickBasicSurvival(entity, chestCavity, type, runtime);
        tickFiltration(entity, chestCavity, type, runtime);
        tickBreathing(entity, chestCavity, runtime);
        tickMetabolism(entity, chestCavity, type, runtime);
        tickPassiveEffects(new BodyEventContext(entity, chestCavity, runtime));
        tickProjectileQueue(entity, chestCavity);
    }

    @SubscribeEvent
    public static void livingJump(LivingEvent.LivingJumpEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
        if (entity == null || chestCavity == null || !chestCavity.isOpened()) {
            return;
        }
        chestCavity.refreshRuntimeIfDirty();
        ChestCavityRuntime runtime = chestCavity.getRuntime();
        float diff = runtime.getDeltaScoreValue(CCOrganScores.LEAPING);
        if (diff != 0.0F) {
            entity.motionY *= Math.max(0.0D, 1.0D + diff * CCConfig.LEAPING_POWER);
            entity.velocityChanged = true;
        }
    }

    private static void tickAttributes(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        for (AttributeEffectSpec effect : ATTRIBUTE_EFFECTS) {
            effect.apply(entity, chestCavity, runtime);
        }
    }

    private static void clearAttributeModifiers(EntityLivingBase entity) {
        for (AttributeEffectSpec effect : ATTRIBUTE_EFFECTS) {
            effect.clear(entity);
        }
    }

    private static void tickBasicSurvival(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityType type, ChestCavityRuntime runtime) {
        float defaultHealth = runtime.getBaselineScoreValue(CCOrganScores.HEALTH);
        boolean missingRequiredHeart = chestCavity.isOpened()
                && defaultHealth > 0.0F
                && runtime.getScoreValue(CCOrganScores.HEALTH) <= 0.0F;

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
        int cap = type.getHeartBleedCap();
        entity.attackEntityFrom(HEART_BLEED_DAMAGE, cap == Integer.MAX_VALUE ? bleedLevel : Math.min(bleedLevel, cap));
    }

    private static void tickFiltration(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityType type, ChestCavityRuntime runtime) {
        float defaultFiltration = runtime.getBaselineScoreValue(CCOrganScores.FILTRATION);
        if (!chestCavity.isOpened() || defaultFiltration <= 0.0F) {
            chestCavity.setBloodPoisonTimer(0);
            return;
        }

        float ratio = runtime.getScoreValue(CCOrganScores.FILTRATION) / defaultFiltration;
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

    private static void tickBreathing(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        if (!chestCavity.isOpened()) {
            chestCavity.setLungRemainder(0.0F);
            return;
        }

        float capacity = runtime.getScoreValue(CCOrganScores.BREATH_CAPACITY);
        float waterBreath = runtime.getScoreValue(CCOrganScores.WATER_BREATH);

        if (entity.isInsideOfMaterial(Material.WATER)) {
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

    private static void tickMetabolism(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityType type, ChestCavityRuntime runtime) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        if (!chestCavity.isOpened()) {
            rememberFoodExhaustion(player);
            return;
        }

        float metabolismDiff = runtime.getDeltaScoreValue(CCOrganScores.METABOLISM);
        if (metabolismDiff > 0.0F) {
            player.addExhaustion(metabolismDiff * 0.005F);
        }
        applyEnduranceExhaustion(player, type, runtime);
    }

    private static void applyEnduranceExhaustion(EntityPlayer player, ChestCavityType type, ChestCavityRuntime runtime) {
        float enduranceDiff = runtime.getDeltaScoreValue(CCOrganScores.ENDURANCE);
        FoodStats stats = player.getFoodStats();
        float current = stats.foodExhaustionLevel;
        NBTTagCompound entityData = player.getEntityData();

        if (!entityData.hasKey(ENDURANCE_LAST_EXHAUSTION_KEY, Constants.NBT.TAG_FLOAT)) {
            entityData.setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, current);
            return;
        }

        float previous = entityData.getFloat(ENDURANCE_LAST_EXHAUSTION_KEY);
        float delta = current - previous;
        if (delta <= 0.0F || enduranceDiff == 0.0F) {
            entityData.setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, current);
            return;
        }

        float adjustedDelta = enduranceDiff > 0.0F
                ? delta / (1.0F + enduranceDiff / 2.0F)
                : delta * (1.0F - enduranceDiff / 2.0F);
        float adjusted = Math.max(0.0F, Math.min(40.0F, previous + adjustedDelta));
        stats.foodExhaustionLevel = adjusted;
        entityData.setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, adjusted);
    }

    private static void rememberFoodExhaustion(EntityPlayer player) {
        player.getEntityData().setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, player.getFoodStats().foodExhaustionLevel);
    }

    private static void tickOrganRejection(EntityLivingBase entity, ChestCavityRuntime runtime) {
        if (CCConfig.DISABLE_ORGAN_REJECTION) {
            if (entity.isPotionActive(CCPotions.ORGAN_REJECTION)) {
                entity.removePotionEffect(CCPotions.ORGAN_REJECTION);
            }
            return;
        }

        float incompatibility = runtime.getScoreValue(CCOrganScores.INCOMPATIBILITY);
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

    private static void tickPassiveEffects(BodyEventContext context) {
        for (TickEffect effect : PASSIVE_TICK_EFFECTS) {
            effect.apply(context);
        }
    }

    private static void tickGlowing(EntityLivingBase entity, ChestCavityRuntime runtime) {
        if (runtime.getScoreValue(CCOrganScores.GLOWING) > 0.0F && !entity.isPotionActive(MobEffects.GLOWING)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0, false, true));
        }
    }

    private static void tickHydroallergenic(EntityLivingBase entity, ChestCavityRuntime runtime) {
        float value = runtime.getScoreValue(CCOrganScores.HYDROALLERGENIC);
        if (value > 0.0F && entity.isWet()) {
            int amplifier = Math.max(1, Math.round(value * 10));
            PotionEffect active = entity.getActivePotionEffect(CCPotions.WATER_VULNERABILITY);
            if (active == null || active.getAmplifier() != amplifier) {
                entity.addPotionEffect(new PotionEffect(CCPotions.WATER_VULNERABILITY, 32767, amplifier, false, true));
            }
        } else if (entity.isPotionActive(CCPotions.WATER_VULNERABILITY)) {
            entity.removePotionEffect(CCPotions.WATER_VULNERABILITY);
        }
    }

    private static void tickHydrophobia(EntityLivingBase entity, ChestCavityRuntime runtime) {
        float value = runtime.getScoreValue(CCOrganScores.HYDROPHOBIA);
        if (value <= 0.0F
                || runtime.getBaselineScoreValue(CCOrganScores.HYDROPHOBIA) > 0.0F
                || !entity.isWet()
                || entity.ticksExisted % HYDROPHOBIA_INTERVAL_TICKS != 0) {
            return;
        }
        EntityMovementUtil.attemptRandomTeleport(entity, value * CCConfig.ARROW_DODGE_DISTANCE);
    }

    private static void tickLightweight(EntityLivingBase entity, ChestCavityRuntime runtime) {
        if (entity.onGround || entity.hasNoGravity() || entity.isInWater() || entity.isInLava() || entity.motionY >= 0.0D) {
            return;
        }

        float diff = runtime.getDeltaScoreValue(CCOrganScores.LIGHTWEIGHT);
        if (diff == 0.0F) {
            return;
        }

        double factor = diff > 0.0F
                ? 1.0D / (1.0D + diff * CCConfig.LIGHTWIEGHT_FACTOR)
                : 1.0D - diff * CCConfig.LIGHTWIEGHT_FACTOR;
        factor = Math.max(0.1D, Math.min(2.5D, factor));
        entity.motionY *= factor;
        entity.fallDistance *= (float) factor;
        entity.velocityChanged = true;
    }

    private static void tickBuoyancy(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        if (!chestCavity.isOpened()) {
            return;
        }
        float buoyant = runtime.getDeltaScoreValue(CCOrganScores.BUOYANT);
        if (buoyant > 0.0F && !entity.onGround && !entity.hasNoGravity()) {
            entity.motionY += buoyant * CCConfig.BUOYANCY_LIFT * Math.max(0.0F, entity.getAir() / 300.0F);
            entity.velocityChanged = true;
        }
    }

    private static void tickPhotosynthesis(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        float photosynthesis = runtime.getDeltaScoreValue(CCOrganScores.PHOTOSYNTHESIS);
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

    private static void tickCrystalsynthesis(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
        if (!chestCavity.isOpened()) {
            return;
        }

        float crystalsynthesis = runtime.getScoreValue(CCOrganScores.CRYSTALSYNTHESIS);
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
            feedPlayer((EntityPlayer) entity, crystalsynthesis);
            return;
        }

        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(crystalsynthesis / 5.0F);
        }
    }

    private static void feedPlayer(EntityPlayer player, float crystalsynthesis) {
        FoodStats foodStats = player.getFoodStats();
        long time = player.world.getTotalWorldTime();
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
    }

    private static EntityEnderCrystal getConnectedCrystal(EntityLivingBase entity, ChestCavityData chestCavity) {
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

    private static void disconnectCrystal(ChestCavityData chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        EntityEnderCrystal crystal = owner == null ? null : getConnectedCrystal(owner, chestCavity);
        if (crystal != null) {
            crystal.setBeamTarget(null);
        }
        chestCavity.setConnectedCrystalId(-1);
    }

    private static void tickProjectileQueue(EntityLivingBase entity, ChestCavityData chestCavity) {
        if (entity.ticksExisted % 5 != 0) {
            return;
        }
        String abilityId = chestCavity.pollProjectileAbility();
        if (abilityId != null) {
            ActiveOrganAbilities.fireQueuedProjectile(entity, chestCavity, abilityId);
        }
    }

    private interface AttributeProvider {
        IAttributeInstance get(EntityLivingBase entity);
    }

    private interface AmountProvider {
        double get(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime, float value);
    }

    private static final class AttributeEffectSpec {
        private final ScoreRef scoreRef;
        private final UUID modifierId;
        private final String modifierName;
        private final AttributeProvider attributeProvider;
        private final AmountProvider amountProvider;
        private final int operation;
        private final WeakHashMap<EntityLivingBase, Double> lastAmounts = new WeakHashMap<>();

        private AttributeEffectSpec(ScoreRef scoreRef, UUID modifierId, String modifierName,
                                    AttributeProvider attributeProvider, AmountProvider amountProvider, int operation) {
            this.scoreRef = scoreRef;
            this.modifierId = modifierId;
            this.modifierName = modifierName;
            this.attributeProvider = attributeProvider;
            this.amountProvider = amountProvider;
            this.operation = operation;
        }

        private void apply(EntityLivingBase entity, ChestCavityData chestCavity, ChestCavityRuntime runtime) {
            IAttributeInstance attribute = attributeProvider == null ? null : attributeProvider.get(entity);
            float value = runtime.getScoreValue(scoreRef);
            double amount = amountProvider == null ? 0.0D : amountProvider.get(entity, chestCavity, runtime, value);
            applyAttributeModifier(entity, attribute, amount);
        }

        private void clear(EntityLivingBase entity) {
            IAttributeInstance attribute = attributeProvider == null ? null : attributeProvider.get(entity);
            if (attribute == null) {
                return;
            }
            AttributeModifier oldModifier = attribute.getModifier(modifierId);
            if (oldModifier != null) {
                attribute.removeModifier(oldModifier);
            }
            lastAmounts.remove(entity);
        }

        private void applyAttributeModifier(EntityLivingBase entity, IAttributeInstance attribute, double amount) {
            if (attribute == null) {
                return;
            }

            AttributeModifier oldModifier = attribute.getModifier(modifierId);
            Double lastAmount = lastAmounts.get(entity);
            boolean changed = lastAmount == null || Double.compare(lastAmount, amount) != 0;

            if (amount == 0.0D) {
                if (oldModifier != null) {
                    attribute.removeModifier(oldModifier);
                }
                lastAmounts.remove(entity);
                return;
            }

            if (oldModifier != null && changed) {
                attribute.removeModifier(oldModifier);
                oldModifier = null;
            }
            if (oldModifier == null) {
                attribute.applyModifier(new AttributeModifier(modifierId, modifierName, amount, operation));
            }
            lastAmounts.put(entity, amount);
        }
    }
}
