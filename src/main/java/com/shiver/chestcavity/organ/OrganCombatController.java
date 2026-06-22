package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责处理由器官分数衍生出的战斗相关逻辑。
 */
public final class OrganCombatController {

    private static final Field POTION_EFFECT_DURATION_FIELD = findPotionEffectDurationField();
    private static final float DEFENSE_HALF_DAMAGE_STEP = 4.0F;
    private static final int DESTRUCTIVE_COLLISION_MAX_BLOCKS = 16;
    private static final float DESTRUCTIVE_COLLISION_BASE_HARDNESS = 0.75F;

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganCombatController() {
    }

    /**
     * 按骨骼、防火、抗冲击等分数修正受到的伤害值。
     *
     * @param chestCavity 受击者胸腔数据。
     * @param source 伤害来源。
     * @param damage 原始伤害值。
     * @return 修正后的伤害值。
     */
    public static float applyDefense(IChestCavity chestCavity, DamageSource source, float damage) {
        if (chestCavity == null || !chestCavity.isOpened() || damage <= 0.0F) {
            return damage;
        }
        if (source != null && source.isUnblockable()) {
            return damage;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float defenseDelta = chestCavity.getOrganScore(CCOrganScores.DEFENSE)
                - type.getDefaultOrganScore(CCOrganScores.DEFENSE);
        if (defenseDelta != 0.0F) {
            damage = (float) (damage * Math.pow(1.0F - CCConfig.BONE_DEFENSE, defenseDelta / DEFENSE_HALF_DAMAGE_STEP));
        }

        if (source != null && source.isFireDamage()) {
            damage = applyDamageResistance(chestCavity.getOrganScore(CCOrganScores.FIRE_RESISTANT), CCConfig.FIREPROOF_DEFENSE, damage);
        }
        if (source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL) {
            damage = applyDamageResistance(chestCavity.getOrganScore(CCOrganScores.IMPACT_RESISTANT), CCConfig.IMPACT_DEFENSE, damage);
            float leapingDiff = chestCavity.getOrganScore(CCOrganScores.LEAPING)
                    - type.getDefaultOrganScore(CCOrganScores.LEAPING);
            if (leapingDiff > 0.0F) {
                damage = Math.max(0.0F, damage - leapingDiff * leapingDiff / 4.0F);
            }
        }
        return damage;
    }

    /**
     * 尝试通过传送来闪避一次投射物攻击。
     *
     * @param entity 被攻击的实体。
     * @param chestCavity 实体胸腔数据。
     * @param source 伤害来源。
     * @return `true` 表示成功闪避。
     */
    public static boolean attemptProjectileDodge(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source) {
        if (entity == null || chestCavity == null || !chestCavity.isOpened() || source == null || !source.isProjectile()) {
            return false;
        }

        float dodge = chestCavity.getOrganScore(CCOrganScores.ARROW_DODGING);
        if (dodge <= 0.0F || entity.isPotionActive(CCPotions.ARROW_DODGE_COOLDOWN)) {
            return false;
        }

        float range = Math.max(4.0F, CCConfig.ARROW_DODGE_DISTANCE / dodge);
        if (!OrganMovementController.attemptRandomTeleport(entity, range)) {
            return false;
        }

        int duration = Math.max(1, Math.round(CCConfig.ARROW_DODGE_COOLDOWN / dodge));
        entity.addPotionEffect(new PotionEffect(CCPotions.ARROW_DODGE_COOLDOWN, duration, 0, false, false));
        return true;
    }

    /**
     * 在药水效果生效前根据净化、解毒等分数调整持续时间。
     *
     * @param entity 目标实体。
     * @param effect 即将应用的药水效果。
     */
    public static void adjustIncomingPotionEffect(EntityLivingBase entity, PotionEffect effect) {
        if (entity == null || effect == null || effect.getPotion() == null || effect.getDuration() <= 1) {
            return;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        float factor = 1.0F;
        if (effect.getPotion().isBeneficial()) {
            factor *= durationReductionFactor(chestCavity.getOrganScore(CCOrganScores.BUFF_PURGING), CCConfig.BUFF_PURGING_DURATION_FACTOR);
        }
        if (effect.getPotion() == MobEffects.WITHER) {
            factor *= durationReductionFactor(chestCavity.getOrganScore(CCOrganScores.WITHERED), CCConfig.WITHERED_DURATION_FACTOR);
        }
        if (effect.getPotion() == MobEffects.POISON) {
            float filtrationDiff = chestCavity.getOrganScore(CCOrganScores.FILTRATION)
                    - ChestCavityHelper.getChestCavityType(chestCavity).getDefaultOrganScore(CCOrganScores.FILTRATION);
            factor *= durationReductionFactor(filtrationDiff, CCConfig.FILTRATION_DURATION_FACTOR);
        }
        if (effect.getPotion().isBadEffect()) {
            factor *= detoxificationDurationFactor(chestCavity);
        }

        if (factor != 1.0F) {
            setPotionDuration(effect, Math.max(1, Math.round(effect.getDuration() * factor)));
        }
    }

    /**
     * 处理近战命中后触发的发射、毒液等附加效果。
     *
     * @param target 受击目标。
     * @param source 伤害来源。
     * @param damage 当前伤害值。
     * @return 原样返回伤害值，便于链式调用。
     */
    public static float applyFinalDamageEffects(EntityLivingBase target, DamageSource source, float damage) {
        if (target == null || source == null || damage <= 0.0F) {
            return damage;
        }

        Entity trueSource = source.getTrueSource();
        if (!(trueSource instanceof EntityLivingBase) || trueSource == target) {
            return damage;
        }
        if (source.getImmediateSource() != trueSource) {
            return damage;
        }

        EntityLivingBase attacker = (EntityLivingBase) trueSource;
        IChestCavity attackerCavity = ChestCavityHelper.getOrNull(attacker);
        if (attackerCavity == null || !attackerCavity.isOpened()) {
            return damage;
        }

        applyLaunching(attacker, target, attackerCavity);
        applyVenom(attacker, target, attackerCavity);
        return damage;
    }

    /**
     * 在摔落或撞墙后按分数尝试破坏周围脆弱方块。
     *
     * @param entity 目标实体。
     * @param chestCavity 实体胸腔数据。
     * @param source 伤害来源。
     * @param damage 伤害值。
     */
    public static void applyDestructiveCollisions(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source, float damage) {
        if (entity == null || chestCavity == null || source == null || damage <= 0.0F || entity.world.isRemote) {
            return;
        }
        if (source != DamageSource.FALL && source != DamageSource.FLY_INTO_WALL) {
            return;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float destructive = chestCavity.getOrganScore(CCOrganScores.DESTRUCTIVE_COLLISIONS)
                - type.getDefaultOrganScore(CCOrganScores.DESTRUCTIVE_COLLISIONS);
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

    /**
     * 在实体跳跃时按跳跃分数修正纵向速度。
     *
     * @param entity 跳跃实体。
     * @param chestCavity 实体胸腔数据。
     */
    public static void applyJump(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity == null || chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float leapingDiff = chestCavity.getOrganScore(CCOrganScores.LEAPING)
                - type.getDefaultOrganScore(CCOrganScores.LEAPING);
        if (leapingDiff != 0.0F) {
            entity.motionY *= Math.max(0.0D, 1.0D + leapingDiff * CCConfig.LEAPING_POWER);
            entity.velocityChanged = true;
        }
    }

    /**
     * 按抗性分数与配置比例缩放伤害值。
     *
     * @param score 抗性分数。
     * @param defense 配置中的减伤强度。
     * @param damage 原始伤害值。
     * @return 修正后的伤害值。
     */
    private static float applyDamageResistance(float score, float defense, float damage) {
        if (score <= 0.0F || damage <= 0.0F) {
            return damage;
        }
        return (float) (damage * Math.pow(1.0F - defense, score / 4.0F));
    }

    /**
     * 根据发射分数把目标向上击飞。
     *
     * @param attacker 攻击者。
     * @param target 被击中的目标。
     * @param chestCavity 攻击者胸腔数据。
     */
    private static void applyLaunching(EntityLivingBase attacker, EntityLivingBase target, IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float launching = chestCavity.getOrganScore(CCOrganScores.LAUNCHING)
                - type.getDefaultOrganScore(CCOrganScores.LAUNCHING);
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

    /**
     * 根据毒液器官为目标施加毒性效果，并给攻击者附加冷却。
     *
     * @param attacker 攻击者。
     * @param target 被击中的目标。
     * @param chestCavity 攻击者胸腔数据。
     */
    private static void applyVenom(EntityLivingBase attacker, EntityLivingBase target, IChestCavity chestCavity) {
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

    /**
     * 汇总胸腔内所有带毒液分数器官自带的药水效果。
     *
     * @param chestCavity 目标胸腔数据。
     * @return 可用于攻击的药水效果列表。
     */
    private static List<PotionEffect> getVenomEffects(IChestCavity chestCavity) {
        List<PotionEffect> effects = new ArrayList<PotionEffect>();
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (!stack.isEmpty()) {
                OrganData data = OrganData.fromStack(stack);
                if (data != null && data.getOrganScores().containsKey(CCOrganScores.VENOMOUS)) {
                    effects.addAll(PotionUtils.getFullEffectsFromItem(stack));
                }
            }
        }
        return effects;
    }

    /**
     * 在给定预算内尝试破坏目标附近的脆弱方块。
     *
     * @param entity 造成碰撞的实体。
     * @param center 搜索中心。
     * @param budget 最多可破坏的方块数量。
     * @param maxHardness 允许破坏的最大硬度。
     */
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

    /**
     * 尝试破坏单个碰撞方块。
     *
     * @param entity 造成碰撞的实体。
     * @param pos 目标方块位置。
     * @param maxHardness 允许破坏的最大硬度。
     * @return `true` 表示方块被成功破坏。
     */
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

    /**
     * 把正向分数换算为药水持续时间缩短倍率。
     *
     * @param score 相关分数。
     * @param scalar 缩放系数。
     * @return 持续时间倍率。
     */
    private static float durationReductionFactor(float score, float scalar) {
        return score <= 0.0F ? 1.0F : 1.0F / (1.0F + scalar * score);
    }

    /**
     * 根据解毒分数计算负面效果持续时间倍率。
     *
     * @param chestCavity 目标胸腔数据。
     * @return 持续时间倍率。
     */
    private static float detoxificationDurationFactor(IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float defaultDetoxification = type.getDefaultOrganScore(CCOrganScores.DETOXIFICATION);
        float detoxification = chestCavity.getOrganScore(CCOrganScores.DETOXIFICATION);
        if (defaultDetoxification <= 0.0F || detoxification == defaultDetoxification) {
            return 1.0F;
        }

        float ratio = detoxification / defaultDetoxification;
        return ratio > -1.0F ? Math.max(0.05F, 2.0F / (1.0F + ratio)) : 9999.0F;
    }

    /**
     * 通过反射强制修改药水效果持续时间。
     *
     * @param effect 要修改的药水效果。
     * @param duration 新的持续时间。
     */
    private static void setPotionDuration(PotionEffect effect, int duration) {
        if (POTION_EFFECT_DURATION_FIELD == null) {
            return;
        }
        try {
            POTION_EFFECT_DURATION_FIELD.setInt(effect, duration);
        } catch (IllegalAccessException ignored) {
        }
    }

    /**
     * 通过反射查找药水效果内部的持续时间字段。
     *
     * @return 持续时间字段；查找失败时返回 `null`。
     */
    private static Field findPotionEffectDurationField() {
        try {
            Field field = ReflectionHelper.findField(PotionEffect.class, "duration", "field_76460_b");
            field.setAccessible(true);
            return field;
        } catch (ReflectionHelper.UnableToFindFieldException ignored) {
            return null;
        }
    }
}
