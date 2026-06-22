package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.organ.OrganCombatController;
import com.shiver.chestcavity.organ.OrganCompatibility;
import com.shiver.chestcavity.organ.OrganDropController;
import com.shiver.chestcavity.organ.OrganFoodController;
import com.shiver.chestcavity.organ.OrganInteractionController;
import com.shiver.chestcavity.organ.OrganLifecycleController;
import com.shiver.chestcavity.organ.OrganScoreCalculator;
import com.shiver.chestcavity.organ.OrganTickController;
import com.shiver.chestcavity.organ.OrganTypeResolver;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 提供胸腔能力相关的统一辅助入口。
 */
public final class ChestCavityHelper {

    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation("chestcavity", "chest_cavity");

    /**
     * 工具类，不允许外部实例化。
     */
    private ChestCavityHelper() {
    }

    /**
     * 从实体身上安全获取胸腔能力实例。
     *
     * @param entity 要查询的实体。
     * @return 胸腔能力实例；如果实体不支持则返回空。
     */
    public static Optional<IChestCavity> get(Entity entity) {
        ChestCavityCapability.ensureRegistered();
        if (!(entity instanceof EntityLivingBase) || ChestCavityCapability.CAPABILITY == null) {
            return Optional.empty();
        }

        IChestCavity chestCavity = entity.getCapability(ChestCavityCapability.CAPABILITY, null);
        if (chestCavity != null) {
            chestCavity.setOwner((EntityLivingBase) entity);
            return Optional.of(chestCavity);
        }
        return Optional.empty();
    }

    /**
     * 从实体身上获取胸腔能力实例，不存在时返回 `null`。
     *
     * @param entity 要查询的实体。
     * @return 胸腔能力实例，或 `null`。
     */
    public static IChestCavity getOrNull(Entity entity) {
        Optional<IChestCavity> chestCavity = get(entity);
        return chestCavity.isPresent() ? chestCavity.get() : null;
    }

    /**
     * 在每个游戏刻刷新胸腔相关逻辑。
     *
     * @param entity 要处理的实体。
     * @param chestCavity 实体对应的胸腔数据。
     */
    public static void tick(EntityLivingBase entity, IChestCavity chestCavity) {
        OrganTickController.tick(entity, chestCavity);
    }

    /**
     * 判断当前器官分数是否相对于旧快照发生了变化。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @return `true` 表示分数已变化。
     */
    public static boolean hasScoreChanges(IChestCavity chestCavity) {
        return !chestCavity.getOldOrganScores().equals(chestCavity.getOrganScores());
    }

    /**
     * 重新计算胸腔中的全部器官分数。
     *
     * @param chestCavity 要重新计算的胸腔数据。
     */
    public static void recalculateOrganScores(IChestCavity chestCavity) {
        OrganScoreCalculator.recalculate(chestCavity);
    }

    /**
     * 设置指定槽位中的器官，并立即触发分数重算。
     *
     * @param chestCavity 要修改的胸腔数据。
     * @param slot 目标槽位。
     * @param stack 要放入的器官物品。
     */
    public static void setOrganAndRecalculate(IChestCavity chestCavity, int slot, ItemStack stack) {
        OrganLifecycleController.setOrganAndRecalculate(chestCavity, slot, stack);
    }

    /**
     * 将胸腔标记为已打开，并执行打开时需要的附带处理。
     *
     * @param chestCavity 要打开的胸腔数据。
     */
    public static void openChestCavity(IChestCavity chestCavity) {
        OrganLifecycleController.openChestCavity(chestCavity);
    }

    /**
     * 复制一个实体的胸腔数据到另一个实体。
     *
     * @param original 源实体。
     * @param replacement 目标实体。
     */
    public static void copy(EntityLivingBase original, EntityLivingBase replacement) {
        copy(original, replacement, false);
    }

    /**
     * 复制一个实体的胸腔数据到另一个实体，并指定是否为死亡复制。
     *
     * @param original 源实体。
     * @param replacement 目标实体。
     * @param wasDeath 是否因为死亡导致复制。
     */
    public static void copy(EntityLivingBase original, EntityLivingBase replacement, boolean wasDeath) {
        OrganLifecycleController.copy(original, replacement, wasDeath);
    }

    /**
     * 将指定玩家自身的胸腔数据同步到客户端。
     *
     * @param player 需要接收同步的玩家。
     */
    public static void syncTo(EntityPlayerMP player) {
        ChestCavityNetwork.sendChestCavitySync(player);
    }

    /**
     * 计算采矿速度修正后的最终倍率。
     *
     * @param chestCavity 要读取分数的胸腔数据。
     * @return 采矿速度倍率。
     */
    public static float getMiningSpeedMultiplier(IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
        float multiplier = 1.0F + chestCavity.getOrganScore(CCOrganScores.MINING_SPEED);
        float defaultNerves = type.getDefaultOrganScore(CCOrganScores.NERVES);
        if (defaultNerves != 0.0F) {
            multiplier += (chestCavity.getOrganScore(CCOrganScores.NERVES) - defaultNerves) * CCConfig.NERVES_HASTE;
        }
        return Math.max(0.0F, multiplier);
    }

    /**
     * 按器官防御属性修正即将受到的伤害值。
     *
     * @param chestCavity 被攻击者的胸腔数据。
     * @param source 伤害来源。
     * @param damage 原始伤害值。
     * @return 修正后的伤害值。
     */
    public static float applyDefense(IChestCavity chestCavity, DamageSource source, float damage) {
        return OrganCombatController.applyDefense(chestCavity, source, damage);
    }

    /**
     * 尝试触发投射物闪避。
     *
     * @param entity 被攻击的实体。
     * @param chestCavity 实体胸腔数据。
     * @param source 伤害来源。
     * @return `true` 表示成功闪避。
     */
    public static boolean attemptProjectileDodge(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source) {
        return OrganCombatController.attemptProjectileDodge(entity, chestCavity, source);
    }

    /**
     * 在药水效果施加前按器官属性调整其参数。
     *
     * @param entity 目标实体。
     * @param effect 即将施加的药水效果。
     */
    public static void adjustIncomingPotionEffect(EntityLivingBase entity, PotionEffect effect) {
        OrganCombatController.adjustIncomingPotionEffect(entity, effect);
    }

    /**
     * 在最终结算前处理受伤后的附加效果，并返回修正后的伤害值。
     *
     * @param target 受击目标。
     * @param source 伤害来源。
     * @param damage 当前伤害值。
     * @return 结算后的最终伤害值。
     */
    public static float applyFinalDamageEffects(EntityLivingBase target, DamageSource source, float damage) {
        return OrganCombatController.applyFinalDamageEffects(target, source, damage);
    }

    /**
     * 根据器官属性处理摔落、碰撞等破坏性连带效果。
     *
     * @param entity 目标实体。
     * @param chestCavity 实体胸腔数据。
     * @param source 伤害来源。
     * @param damage 当前伤害值。
     */
    public static void applyDestructiveCollisions(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source, float damage) {
        OrganCombatController.applyDestructiveCollisions(entity, chestCavity, source, damage);
    }

    /**
     * 销毁所有带有指定分数标签的器官。
     *
     * @param chestCavity 要处理的胸腔数据。
     * @param scoreId 要匹配的分数标识。
     */
    public static void destroyOrgansWithScore(IChestCavity chestCavity, String scoreId) {
        OrganLifecycleController.destroyOrgansWithScore(chestCavity, scoreId);
    }

    /**
     * 对指定实体周围应用泼水交互效果。
     *
     * @param source 泼水来源实体。
     */
    public static void applyWaterSplash(Entity source) {
        OrganInteractionController.applyWaterSplash(source);
    }

    /**
     * 尝试从实体身上收集蜘蛛丝。
     *
     * @param entity 目标实体。
     * @return `true` 表示成功收集。
     */
    public static boolean milkSilk(EntityLivingBase entity) {
        return OrganInteractionController.milkSilk(entity);
    }

    /**
     * 尝试通过剪切方式从实体身上获取蜘蛛丝。
     *
     * @param entity 目标实体。
     * @return `true` 表示成功剪取。
     */
    public static boolean shearSilk(EntityLivingBase entity) {
        return OrganInteractionController.shearSilk(entity);
    }

    /**
     * 在实体跳跃时应用器官相关的跳跃修正。
     *
     * @param entity 触发跳跃的实体。
     * @param chestCavity 实体胸腔数据。
     */
    public static void applyJump(EntityLivingBase entity, IChestCavity chestCavity) {
        OrganCombatController.applyJump(entity, chestCavity);
    }

    /**
     * 在玩家进食后应用器官带来的额外效果。
     *
     * @param player 进食的玩家。
     * @param eaten 本次吃下的物品。
     */
    public static void applyFoodEffects(EntityPlayer player, ItemStack eaten) {
        OrganFoodController.applyFoodEffects(player, eaten);
    }

    /**
     * 消耗玩家当前存储的熔炉动力食物值。
     *
     * @param player 目标玩家。
     */
    public static void consumeFurnacePowerFood(EntityPlayer player) {
        OrganFoodController.consumeFurnacePowerFood(player);
    }

    /**
     * 判断指定槽位是否被当前胸腔类型禁止使用。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @param slot 槽位索引。
     * @return `true` 表示该槽位不可放置器官。
     */
    public static boolean isSlotForbidden(IChestCavity chestCavity, int slot) {
        return chestCavity == null || getChestCavityType(chestCavity).isSlotForbidden(slot);
    }

    /**
     * 判断当前胸腔是否允许被打开。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @return `true` 表示可以打开。
     */
    public static boolean isOpenable(IChestCavity chestCavity) {
        return OrganLifecycleController.isOpenable(chestCavity);
    }

    /**
     * 判断当前胸腔是否已经分配了具体类型。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @return `true` 表示已分配类型。
     */
    public static boolean hasAssignedChestCavityType(IChestCavity chestCavity) {
        return OrganTypeResolver.hasAssignedType(chestCavity);
    }

    /**
     * 返回当前胸腔应使用的胸腔类型定义。
     *
     * @param chestCavity 要查询的胸腔数据。
     * @return 胸腔类型定义。
     */
    public static ChestCavityType getChestCavityType(IChestCavity chestCavity) {
        return OrganTypeResolver.getType(chestCavity);
    }

    /**
     * 为未打开状态的胸腔生成死亡掉落物列表。
     *
     * @param chestCavity 掉落来源胸腔。
     * @param random 随机源。
     * @param baseLooting 基础抢夺等级。
     * @param killer 击杀者实体。
     * @return 生成出的掉落物列表。
     */
    public static List<ItemStack> generateUnopenedOrganDrops(IChestCavity chestCavity, Random random, int baseLooting, EntityLivingBase killer) {
        return OrganDropController.generateUnopenedOrganDrops(chestCavity, random, baseLooting, killer);
    }

    /**
     * 在实体死亡时移除并返回所有未绑定器官。
     *
     * @param chestCavity 要处理的胸腔数据。
     * @return 被移除的器官列表。
     */
    public static List<ItemStack> removeUnboundOrgansForDeath(IChestCavity chestCavity) {
        return OrganDropController.removeUnboundOrgansForDeath(chestCavity);
    }

    /**
     * 计算器官与当前胸腔的兼容等级。
     *
     * @param chestCavity 目标胸腔数据。
     * @param stack 要检查的器官物品。
     * @return 兼容等级。
     */
    public static int getCompatibilityLevel(IChestCavity chestCavity, ItemStack stack) {
        return OrganCompatibility.getLevel(chestCavity, stack);
    }

    /**
     * 判断物品上是否携带兼容性标签。
     *
     * @param stack 要检查的物品。
     * @return `true` 表示具有兼容性标签。
     */
    public static boolean hasCompatibilityTag(ItemStack stack) {
        return OrganCompatibility.hasTag(stack);
    }

    /**
     * 返回物品上记录的兼容性名称。
     *
     * @param stack 要读取的物品。
     * @return 兼容性名称。
     */
    public static String getCompatibilityName(ItemStack stack) {
        return OrganCompatibility.getName(stack);
    }

    /**
     * 应用器官分数变更并在需要时向客户端同步。
     *
     * @param chestCavity 要处理的胸腔数据。
     */
    public static void applyAndSyncScoreChanges(IChestCavity chestCavity) {
        OrganLifecycleController.applyAndSyncScoreChanges(chestCavity);
    }
}
