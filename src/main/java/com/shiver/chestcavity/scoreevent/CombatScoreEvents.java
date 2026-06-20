package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.util.EntityMovementUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class CombatScoreEvents {

    private static final float DEFENSE_HALF_DAMAGE_STEP = 4.0F;
    private static final int DESTRUCTIVE_COLLISION_MAX_BLOCKS = 16;
    private static final float DESTRUCTIVE_COLLISION_BASE_HARDNESS = 0.75F;

    private static final AttackCancelSpec[] ATTACK_CANCEL_SPECS = new AttackCancelSpec[] {
            CombatScoreEvents::attemptProjectileDodge
    };
    private static final DamageModifierSpec[] DAMAGE_MODIFIERS = new DamageModifierSpec[] {
            CombatScoreEvents::boneDefense,
            CombatScoreEvents::fireResistance,
            CombatScoreEvents::impactResistance,
            CombatScoreEvents::leapingFallReduction
    };
    private static final HitEffectSpec[] HIT_EFFECTS = new HitEffectSpec[] {
            CombatScoreEvents::applyLaunching,
            CombatScoreEvents::applyVenom
    };
    private static final DamageSideEffectSpec[] DAMAGE_SIDE_EFFECTS = new DamageSideEffectSpec[] {
            CombatScoreEvents::applyDestructiveCollisions
    };

    private CombatScoreEvents() {
    }

    @SubscribeEvent
    public static void livingAttack(LivingAttackEvent event) {
        CombatContext context = CombatContext.target(event.getEntityLiving(), event.getSource(), 0.0F);
        if (!context.hasOpenChest()) {
            return;
        }
        for (AttackCancelSpec spec : ATTACK_CANCEL_SPECS) {
            if (spec.shouldCancel(context)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        CombatContext context = CombatContext.target(event.getEntityLiving(), event.getSource(), event.getAmount());
        if (!context.hasOpenChest() || event.getAmount() <= 0.0F) {
            return;
        }
        float amount = event.getAmount();
        for (DamageModifierSpec spec : DAMAGE_MODIFIERS) {
            amount = spec.modify(context, amount);
        }
        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void livingDamage(LivingDamageEvent event) {
        CombatContext targetContext = CombatContext.target(event.getEntityLiving(), event.getSource(), event.getAmount());
        float amount = event.getAmount();
        CombatContext attackerContext = CombatContext.attacker(event.getEntityLiving(), event.getSource(), amount);
        if (attackerContext.hasOpenChest()) {
            for (HitEffectSpec spec : HIT_EFFECTS) {
                spec.apply(attackerContext, event.getEntityLiving(), amount);
            }
        }
        if (targetContext.hasOpenChest()) {
            for (DamageSideEffectSpec spec : DAMAGE_SIDE_EFFECTS) {
                spec.apply(targetContext, amount);
            }
        }
    }

    private static boolean attemptProjectileDodge(CombatContext context) {
        EntityLivingBase entity = context.entity;
        DamageSource source = context.source;
        ChestCavityData chestCavity = context.chestCavity;
        if (source == null || !source.isProjectile()) {
            return false;
        }

        float dodge = chestCavity.getOrganScore(CCOrganScores.ARROW_DODGING);
        if (dodge <= 0.0F || entity.isPotionActive(CCPotions.ARROW_DODGE_COOLDOWN)) {
            return false;
        }

        float range = Math.max(4.0F, CCConfig.ARROW_DODGE_DISTANCE / dodge);
        if (!EntityMovementUtil.attemptRandomTeleport(entity, range)) {
            return false;
        }

        int duration = Math.max(1, Math.round(CCConfig.ARROW_DODGE_COOLDOWN / dodge));
        entity.addPotionEffect(new PotionEffect(CCPotions.ARROW_DODGE_COOLDOWN, duration, 0, false, false));
        return true;
    }

    private static float boneDefense(CombatContext context, float damage) {
        if (context.source != null && context.source.isUnblockable()) {
            return damage;
        }
        float defenseDelta = context.runtime.getDeltaScoreValue(CCOrganScores.DEFENSE);
        return defenseDelta == 0.0F
                ? damage
                : (float) (damage * Math.pow(1.0F - CCConfig.BONE_DEFENSE, defenseDelta / DEFENSE_HALF_DAMAGE_STEP));
    }

    private static float fireResistance(CombatContext context, float damage) {
        return context.source != null && context.source.isFireDamage()
                ? applyDamageResistance(context.chestCavity.getOrganScore(CCOrganScores.FIRE_RESISTANT), CCConfig.FIREPROOF_DEFENSE, damage)
                : damage;
    }

    private static float impactResistance(CombatContext context, float damage) {
        return isImpactDamage(context.source)
                ? applyDamageResistance(context.chestCavity.getOrganScore(CCOrganScores.IMPACT_RESISTANT), CCConfig.IMPACT_DEFENSE, damage)
                : damage;
    }

    private static float leapingFallReduction(CombatContext context, float damage) {
        if (!isImpactDamage(context.source)) {
            return damage;
        }
        float leapingDiff = context.runtime.getDeltaScoreValue(CCOrganScores.LEAPING);
        return leapingDiff > 0.0F ? Math.max(0.0F, damage - leapingDiff * leapingDiff / 4.0F) : damage;
    }

    private static float applyDamageResistance(float score, float defense, float damage) {
        if (score <= 0.0F || damage <= 0.0F) {
            return damage;
        }
        return (float) (damage * Math.pow(1.0F - defense, score / 4.0F));
    }

    private static void applyLaunching(CombatContext context, EntityLivingBase target, float damage) {
        EntityLivingBase attacker = context.entity;
        float launching = context.runtime.getDeltaScoreValue(CCOrganScores.LAUNCHING);
        if (launching == 0.0F || attacker.getDistanceSq(target) > 16.0D) {
            return;
        }

        IAttributeInstance knockbackResistance = target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        double resistance = knockbackResistance == null ? 0.0D : knockbackResistance.getAttributeValue();
        double lift = Math.max(0.0D, CCConfig.LAUNCHING_POWER * launching * (1.0D - resistance));
        if (lift > 0.0D) {
            target.motionY += lift;
            target.velocityChanged = true;
        }
    }

    private static void applyVenom(CombatContext context, EntityLivingBase target, float damage) {
        EntityLivingBase attacker = context.entity;
        ChestCavityData chestCavity = context.chestCavity;
        if (chestCavity.getOrganScore(CCOrganScores.VENOMOUS) <= 0.0F
                || attacker.isPotionActive(CCPotions.VENOM_COOLDOWN)) {
            return;
        }

        List<PotionEffect> effects = getVenomEffects(chestCavity);
        if (effects.isEmpty()) {
            target.addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 0));
        } else {
            for (PotionEffect effect : effects) {
                target.addPotionEffect(new PotionEffect(effect));
            }
        }
        attacker.addPotionEffect(new PotionEffect(CCPotions.VENOM_COOLDOWN, CCConfig.VENOM_COOLDOWN, 0, false, false));
        if (attacker instanceof EntityPlayer) {
            ((EntityPlayer) attacker).addExhaustion(0.1F);
        }
    }

    private static List<PotionEffect> getVenomEffects(ChestCavityData chestCavity) {
        List<PotionEffect> effects = new ArrayList<>();
        for (int slot : chestCavity.getRuntime().getSlotsByScore(CCOrganScores.VENOMOUS)) {
            ItemStack stack = chestCavity.getRuntime().getOrgan(slot);
            OrganData data = chestCavity.getRuntime().getOrganData(slot);
            if (!stack.isEmpty() && data != null && data.getOrganScores().containsKey(CCOrganScores.VENOMOUS)) {
                effects.addAll(PotionUtils.getFullEffectsFromItem(stack));
            }
        }
        return effects;
    }

    private static void applyDestructiveCollisions(CombatContext context, float damage) {
        EntityLivingBase entity = context.entity;
        DamageSource source = context.source;
        if (source == null || damage <= 0.0F || entity.world.isRemote || !isImpactDamage(source)) {
            return;
        }

        float destructive = context.runtime.getDeltaScoreValue(CCOrganScores.DESTRUCTIVE_COLLISIONS);
        if (destructive <= 0.0F) {
            return;
        }
        if (!(entity instanceof EntityPlayer) && !entity.world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }

        int budget = Math.min(DESTRUCTIVE_COLLISION_MAX_BLOCKS,
                Math.max(1, Math.round(destructive * Math.max(1.0F, damage) / 2.0F)));
        float maxHardness = DESTRUCTIVE_COLLISION_BASE_HARDNESS + destructive * 0.75F + damage * 0.25F;
        BlockPos center = source == DamageSource.FALL ? new BlockPos(entity).down() : new BlockPos(entity);
        breakWeakCollisionBlocks(entity, center, budget, maxHardness);
    }

    private static void breakWeakCollisionBlocks(EntityLivingBase entity, BlockPos center, int budget, float maxHardness) {
        int broken = 0;
        for (int y = -1; y <= 1 && broken < budget; y++) {
            for (int x = -1; x <= 1 && broken < budget; x++) {
                for (int z = -1; z <= 1 && broken < budget; z++) {
                    if (tryBreakCollisionBlock(entity, center.add(x, y, z), maxHardness)) {
                        broken++;
                    }
                }
            }
        }
    }

    private static boolean tryBreakCollisionBlock(EntityLivingBase entity, BlockPos pos, float maxHardness) {
        IBlockState state = entity.world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.AIR || material.isLiquid() || entity.world.getTileEntity(pos) != null) {
            return false;
        }

        float hardness = state.getBlockHardness(entity.world, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return false;
        }
        if (entity instanceof EntityPlayer && !entity.world.isBlockModifiable((EntityPlayer) entity, pos)) {
            return false;
        }
        return entity.world.destroyBlock(pos, true);
    }

    private static boolean isImpactDamage(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL;
    }

    private interface AttackCancelSpec {
        boolean shouldCancel(CombatContext context);
    }

    private interface DamageModifierSpec {
        float modify(CombatContext context, float damage);
    }

    private interface HitEffectSpec {
        void apply(CombatContext attackerContext, EntityLivingBase target, float damage);
    }

    private interface DamageSideEffectSpec {
        void apply(CombatContext targetContext, float damage);
    }

    private static final class CombatContext {
        private final EntityLivingBase entity;
        private final DamageSource source;
        private final ChestCavityData chestCavity;
        private final ChestCavityRuntime runtime;

        private CombatContext(EntityLivingBase entity, DamageSource source, ChestCavityData chestCavity) {
            this.entity = entity;
            this.source = source;
            this.chestCavity = chestCavity;
            this.runtime = chestCavity == null ? null : chestCavity.getRuntime();
        }

        private static CombatContext target(EntityLivingBase entity, DamageSource source, float damage) {
            return new CombatContext(entity, source, ChestCavityHelper.getOrNull(entity));
        }

        private static CombatContext attacker(EntityLivingBase target, DamageSource source, float damage) {
            if (target == null || source == null || damage <= 0.0F) {
                return new CombatContext(null, source, null);
            }
            Entity trueSource = source.getTrueSource();
            if (!(trueSource instanceof EntityLivingBase) || trueSource == target || source.getImmediateSource() != trueSource) {
                return new CombatContext(null, source, null);
            }
            EntityLivingBase attacker = (EntityLivingBase) trueSource;
            return new CombatContext(attacker, source, ChestCavityHelper.getOrNull(attacker));
        }

        private boolean hasOpenChest() {
            return entity != null && chestCavity != null && chestCavity.isOpened() && runtime != null;
        }
    }
}
