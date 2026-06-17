package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.OrganRejection;
import com.shiver.chestcavity.recipe.SalvageRecipe;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class ChestCavityHelper {

    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation("chestcavity", "chest_cavity");

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("1187ab41-0e24-42bb-a39d-fb3b5b5492d5");
    private static final UUID STRENGTH_MODIFIER_ID = UUID.fromString("90d594f2-eaf5-4dc4-b970-fd2e48c83328");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("12a770fb-3062-4d2e-b921-a9a139882aa3");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("709e3e77-0586-4304-80b5-d28bc477e947");
    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("1dd5473d-d43b-4cf1-8600-f11372c4959a");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("b54ff8c5-fb1d-40eb-9d41-c02580505470");
    private static final UUID SWIM_SPEED_MODIFIER_ID = UUID.fromString("32d5f52b-796a-4194-a8e3-1acb45f5a365");
    private static final Field POTION_EFFECT_DURATION_FIELD = findPotionEffectDurationField();
    private static final float DEFENSE_HALF_DAMAGE_STEP = 4.0F;
    private static final DamageSource HEART_BLEED_DAMAGE = new DamageSource("cc_heartbleed").setDamageBypassesArmor();
    private static final int NO_BREATH_DAMAGE_RATE_TICKS = 20;
    private static final int HYDROALLERGENIC_BASE_RATE_TICKS = 260;
    private static final int HYDROPHOBIA_INTERVAL_TICKS = 20;
    private static final String ENDURANCE_LAST_EXHAUSTION_KEY = "chestcavity:last_exhaustion";
    private static final String FOOD_EXHAUSTION_KEY = "foodExhaustionLevel";
    private static final int DESTRUCTIVE_COLLISION_MAX_BLOCKS = 16;
    private static final float DESTRUCTIVE_COLLISION_BASE_HARDNESS = 0.75F;
    private static final int PRION_DURATION_TICKS = 24000;
    private static final String BUTCHERING_TOOL_ORE = "chestcavity:butchering_tool";
    private static final String COMPATIBILITY_TAG = "chestcavity:organ_compatibility";
    private static final String COMPATIBILITY_OWNER_KEY = "owner";
    private static final String COMPATIBILITY_NAME_KEY = "name";

    private ChestCavityHelper() {
    }

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

    public static IChestCavity getOrNull(Entity entity) {
        Optional<IChestCavity> chestCavity = get(entity);
        return chestCavity.isPresent() ? chestCavity.get() : null;
    }

    public static void tick(EntityLivingBase entity, IChestCavity chestCavity) {
        recalculateOrganScores(chestCavity);
        boolean scoreChanges = hasScoreChanges(chestCavity);

        if (!entity.world.isRemote) {
            applyBasicAttributeModifiers(entity, chestCavity);
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

    public static boolean hasScoreChanges(IChestCavity chestCavity) {
        return !chestCavity.getOldOrganScores().equals(chestCavity.getOrganScores());
    }

    public static void recalculateOrganScores(IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
        Map<ResourceLocation, Float> scores = new LinkedHashMap<>();

        if (!chestCavity.isOpened()) {
            scores.putAll(type.getDefaultOrganScores());
        } else {
            type.loadBaseOrganScores(scores);
            for (ItemStack stack : chestCavity.getOrgans()) {
                if (!stack.isEmpty()) {
                    OrganData data = type.catchExceptionalOrgan(stack);
                    if (data == null) {
                        data = OrganData.fromStack(stack);
                    }
                    if (data != null) {
                        addOrganScores(scores, data, stack);
                        if (!data.isPseudoOrgan() && getCompatibilityLevel(chestCavity, stack) < 1) {
                            Float old = scores.get(CCOrganScores.INCOMPATIBILITY);
                            scores.put(CCOrganScores.INCOMPATIBILITY, old == null ? 1.0F : old + 1.0F);
                        }
                    }
                }
            }
        }

        chestCavity.replaceOrganScores(scores);
    }

    public static void setOrganAndRecalculate(IChestCavity chestCavity, int slot, ItemStack stack) {
        chestCavity.setOrgan(slot, stack);
        applyAndSyncScoreChanges(chestCavity);
    }

    public static void openChestCavity(IChestCavity chestCavity) {
        if (!chestCavity.isOpened()) {
            ChestCavityType type = getChestCavityType(chestCavity);
            for (int i = 0; i < chestCavity.getSlotCount() && i < type.getDefaultChestCavity().size(); i++) {
                ItemStack stack = type.getDefaultChestCavity().getStack(i);
                chestCavity.setOrgan(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            chestCavity.setOpened(true);
            chestCavity.clearProjectileQueue();
            disconnectCrystal(chestCavity);
            setOrganCompatibility(chestCavity);
            recalculateOrganScores(chestCavity);
            applyAndSyncScoreChanges(chestCavity);
            EntityLivingBase owner = chestCavity.getOwner();
            if (owner != null && !owner.world.isRemote) {
                ChestCavityNetwork.sendChestCavitySync(owner);
            }
        }
    }

    public static void copy(EntityLivingBase original, EntityLivingBase replacement) {
        copy(original, replacement, false);
    }

    public static void copy(EntityLivingBase original, EntityLivingBase replacement, boolean wasDeath) {
        IChestCavity oldCavity = getOrNull(original);
        IChestCavity newCavity = getOrNull(replacement);
        if (oldCavity != null && newCavity != null) {
            newCavity.copyFrom(oldCavity);
            newCavity.setOwner(replacement);
            if (wasDeath && replacement instanceof EntityPlayer) {
                resetPlayerChestCavityAfterDeath(oldCavity, newCavity);
            }
        }
    }

    public static void syncTo(EntityPlayerMP player) {
        ChestCavityNetwork.sendChestCavitySync(player);
    }

    public static float getMiningSpeedMultiplier(IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
        float multiplier = 1.0F + chestCavity.getOrganScore(CCOrganScores.MINING_SPEED);
        float defaultNerves = type.getDefaultOrganScore(CCOrganScores.NERVES);
        if (defaultNerves != 0.0F) {
            multiplier += (chestCavity.getOrganScore(CCOrganScores.NERVES) - defaultNerves) * CCConfig.NERVES_HASTE;
        }
        return Math.max(0.0F, multiplier);
    }

    public static float applyDefense(IChestCavity chestCavity, DamageSource source, float damage) {
        if (chestCavity == null || !chestCavity.isOpened() || damage <= 0.0F) {
            return damage;
        }
        if (source != null && source.isUnblockable()) {
            return damage;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
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

    public static boolean attemptProjectileDodge(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source) {
        if (entity == null || chestCavity == null || !chestCavity.isOpened() || source == null || !source.isProjectile()) {
            return false;
        }

        float dodge = chestCavity.getOrganScore(CCOrganScores.ARROW_DODGING);
        if (dodge <= 0.0F || entity.isPotionActive(CCPotions.ARROW_DODGE_COOLDOWN)) {
            return false;
        }

        float range = Math.max(4.0F, CCConfig.ARROW_DODGE_DISTANCE / dodge);
        if (!attemptRandomTeleport(entity, range)) {
            return false;
        }

        int duration = Math.max(1, Math.round(CCConfig.ARROW_DODGE_COOLDOWN / dodge));
        entity.addPotionEffect(new PotionEffect(CCPotions.ARROW_DODGE_COOLDOWN, duration, 0, false, false));
        return true;
    }

    public static void adjustIncomingPotionEffect(EntityLivingBase entity, PotionEffect effect) {
        if (entity == null || effect == null || effect.getPotion() == null || effect.getDuration() <= 1) {
            return;
        }

        IChestCavity chestCavity = getOrNull(entity);
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
                    - getChestCavityType(chestCavity).getDefaultOrganScore(CCOrganScores.FILTRATION);
            factor *= durationReductionFactor(filtrationDiff, CCConfig.FILTRATION_DURATION_FACTOR);
        }
        if (effect.getPotion().isBadEffect()) {
            factor *= detoxificationDurationFactor(chestCavity);
        }

        if (factor != 1.0F) {
            setPotionDuration(effect, Math.max(1, Math.round(effect.getDuration() * factor)));
        }
    }

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
        IChestCavity attackerCavity = getOrNull(attacker);
        if (attackerCavity == null || !attackerCavity.isOpened()) {
            return damage;
        }

        applyLaunching(attacker, target, attackerCavity);
        applyVenom(attacker, target, attackerCavity);
        return damage;
    }

    public static void applyDestructiveCollisions(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source, float damage) {
        if (entity == null || chestCavity == null || source == null || damage <= 0.0F || entity.world.isRemote) {
            return;
        }
        if (source != DamageSource.FALL && source != DamageSource.FLY_INTO_WALL) {
            return;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
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

    public static void destroyOrgansWithScore(IChestCavity chestCavity, ResourceLocation scoreId) {
        if (chestCavity == null || scoreId == null) {
            return;
        }
        ChestCavityType type = getChestCavityType(chestCavity);
        boolean changed = false;
        for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
            ItemStack stack = chestCavity.getOrgan(slot);
            OrganData data = resolveOrganData(type, stack);
            if (data != null && data.getOrganScores().containsKey(scoreId)) {
                chestCavity.setOrgan(slot, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            applyAndSyncScoreChanges(chestCavity);
        }
    }

    public static void applyWaterSplash(Entity source) {
        if (source == null || source.world == null || source.world.isRemote) {
            return;
        }
        AxisAlignedBB box = source.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityLivingBase> entities = source.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase entity : entities) {
            if (source.getDistanceSq(entity) >= 16.0D) {
                continue;
            }
            IChestCavity chestCavity = getOrNull(entity);
            if (chestCavity == null || !chestCavity.isOpened()) {
                continue;
            }
            float allergy = chestCavity.getOrganScore(CCOrganScores.HYDROALLERGENIC);
            if (allergy > 0.0F) {
                entity.attackEntityFrom(DamageSource.MAGIC, allergy / 26.0F);
            }
            float phobia = chestCavity.getOrganScore(CCOrganScores.HYDROPHOBIA);
            if (phobia > 0.0F) {
                attemptRandomTeleport(entity, phobia * 32.0F);
            }
        }
    }

    public static boolean milkSilk(EntityLivingBase entity) {
        IChestCavity chestCavity = getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()
                || chestCavity.getOrganScore(CCOrganScores.SILK) <= 0.0F
                || entity.isPotionActive(CCPotions.SILK_COOLDOWN)) {
            return false;
        }
        boolean spun = spinWeb(entity, chestCavity.getOrganScore(CCOrganScores.SILK));
        if (spun) {
            entity.addPotionEffect(new PotionEffect(CCPotions.SILK_COOLDOWN,
                    CCConfig.SILK_COOLDOWN, 0, false, false));
        }
        return spun;
    }

    public static boolean shearSilk(EntityLivingBase entity) {
        IChestCavity chestCavity = getOrNull(entity);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return false;
        }
        float silk = chestCavity.getOrganScore(CCOrganScores.SILK);
        if (silk <= 0.0F) {
            return false;
        }

        boolean dropped = false;
        int webs = (int) silk / 2;
        if (webs > 0) {
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ,
                    new ItemStack(Blocks.WEB, webs)));
            dropped = true;
        }
        if (silk % 2.0F >= 1.0F) {
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ,
                    new ItemStack(Items.STRING)));
            dropped = true;
        }
        return dropped;
    }

    public static void applyJump(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity == null || chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
        float leapingDiff = chestCavity.getOrganScore(CCOrganScores.LEAPING)
                - type.getDefaultOrganScore(CCOrganScores.LEAPING);
        if (leapingDiff != 0.0F) {
            entity.motionY *= Math.max(0.0D, 1.0D + leapingDiff * CCConfig.LEAPING_POWER);
            entity.velocityChanged = true;
        }
    }

    public static void applyFoodEffects(EntityPlayer player, ItemStack eaten) {
        if (player == null || player.world.isRemote || eaten == null || eaten.isEmpty() || !(eaten.getItem() instanceof ItemFood)) {
            return;
        }

        applyHumanPrionRisk(player, eaten);

        IChestCavity chestCavity = getOrNull(player);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        ItemFood food = (ItemFood) eaten.getItem();
        int vanillaFood = food.getHealAmount(eaten);
        float vanillaSaturation = food.getSaturationModifier(eaten);
        float digestion = chestCavity.getOrganScore(CCOrganScores.DIGESTION);
        float nutrition = chestCavity.getOrganScore(CCOrganScores.NUTRITION);
        float herbivorousDigestion = chestCavity.getOrganScore(CCOrganScores.HERBIVOROUS_DIGESTION);
        float herbivorousNutrition = chestCavity.getOrganScore(CCOrganScores.HERBIVOROUS_NUTRITION);

        if (isMeatFood(food, eaten)) {
            digestion += chestCavity.getOrganScore(CCOrganScores.CARNIVOROUS_DIGESTION);
            nutrition += chestCavity.getOrganScore(CCOrganScores.CARNIVOROUS_NUTRITION);
        } else {
            digestion += herbivorousDigestion;
            nutrition += herbivorousNutrition;
        }

        if (isFurnacePowerFood(eaten)) {
            digestion -= herbivorousDigestion;
            nutrition -= herbivorousNutrition;
            PotionEffect furnacePower = player.getActivePotionEffect(CCPotions.FURNACE_POWER);
            if (furnacePower != null) {
                nutrition += furnacePower.getAmplifier() + 1;
            }
        }

        if (isRottenFood(eaten)) {
            digestion += chestCavity.getOrganScore(CCOrganScores.ROT_DIGESTION);
            nutrition += chestCavity.getOrganScore(CCOrganScores.ROTGUT);
            if (chestCavity.getOrganScore(CCOrganScores.ROTGUT) + chestCavity.getOrganScore(CCOrganScores.ROT_DIGESTION) > 0.0F) {
                player.removePotionEffect(MobEffects.HUNGER);
            }
        }

        int effectiveFood = applyDigestion(player, digestion, vanillaFood);
        float effectiveSaturation = applyNutrition(player, nutrition, vanillaSaturation);
        adjustFoodStats(player, vanillaFood, vanillaSaturation, effectiveFood, effectiveSaturation);
    }

    public static void consumeFurnacePowerFood(EntityPlayer player) {
        if (player == null || player.world.isRemote || !(CCItems.FURNACE_POWER instanceof ItemFood)) {
            return;
        }

        ItemStack stack = new ItemStack(CCItems.FURNACE_POWER);
        player.getFoodStats().addStats((ItemFood) stack.getItem(), stack);
        applyFoodEffects(player, stack);
    }

    public static boolean isSlotForbidden(IChestCavity chestCavity, int slot) {
        return chestCavity == null || getChestCavityType(chestCavity).isSlotForbidden(slot);
    }

    public static boolean isOpenable(IChestCavity chestCavity) {
        if (chestCavity == null) {
            return false;
        }

        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null || !owner.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST).isEmpty()) {
            return false;
        }

        recalculateOrganScores(chestCavity);
        boolean weakEnough = owner.getHealth() <= CCConfig.CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD
                || owner.getHealth() <= owner.getMaxHealth() * CCConfig.CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD;
        boolean easyAccess = chestCavity.getOrganScore(CCOrganScores.EASE_OF_ACCESS) > 0.0F;
        return weakEnough || easyAccess;
    }

    public static ChestCavityType getChestCavityType(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner != null) {
            ResourceLocation entityId = owner instanceof EntityPlayer
                    ? new ResourceLocation("minecraft", "player")
                    : EntityList.getKey(owner);
            ResourceLocation typeId = DataLoaders.getAssignedTypeId(entityId);
            if (typeId != null) {
                return DataLoaders.getType(typeId);
            }
        }
        return DataLoaders.getType(CCConfig.getDefaultChestCavityId());
    }

    public static List<ItemStack> generateUnopenedOrganDrops(IChestCavity chestCavity, Random random, int baseLooting, EntityLivingBase killer) {
        List<ItemStack> loot = new ArrayList<ItemStack>();
        if (chestCavity == null || random == null) {
            return loot;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
        if (type == DataLoaders.getFallbackType() || type.isPlayerChestCavity()) {
            return loot;
        }

        int looting = Math.max(0, baseLooting);
        boolean butcher = false;
        boolean malpractice = false;
        if (killer != null) {
            if (EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.TOMOPHOBIA, killer) > 0) {
                return loot;
            }
            looting += 2 * EnchantmentHelper.getMaxEnchantmentLevel(CCEnchantments.SURGICAL, killer);

            ItemStack held = killer.getHeldItemMainhand();
            butcher = hasOreName(held, BUTCHERING_TOOL_ORE);
            if (butcher) {
                looting *= 10;
            }
            malpractice = EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, held) > 0;
        }

        if (type.isBossChestCavity()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 3 + random.nextInt(2 + looting) + random.nextInt(2 + looting), random, loot);
        } else if (random.nextFloat() < (CCConfig.UNIVERSAL_DONOR_RATE + CCConfig.ORGAN_BUNDLE_LOOTING_BOOST * looting) * type.getDropRateMultiplier()) {
            drawOrgansFromPile(type.getDroppableOrgans(), 1 + random.nextInt(3) + random.nextInt(3), random, loot);
        }

        if (butcher) {
            processButchering(loot);
        }
        if (malpractice) {
            processMalpractice(loot);
        }
        return loot;
    }

    public static List<ItemStack> removeUnboundOrgansForDeath(IChestCavity chestCavity) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        if (chestCavity == null || !chestCavity.isOpened()) {
            return drops;
        }

        for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
            ItemStack stack = chestCavity.getOrgan(slot);
            if (!stack.isEmpty() && getCompatibilityLevel(chestCavity, stack) < 2) {
                drops.add(stack.copy());
                chestCavity.setOrgan(slot, ItemStack.EMPTY);
            }
        }
        applyAndSyncScoreChanges(chestCavity);
        return drops;
    }

    public static int getCompatibilityLevel(IChestCavity chestCavity, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        if (EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, stack) > 0) {
            return 0;
        }

        int oNegative = EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack);
        int ownership = 0;
        NBTTagCompound tag = getCompatibilityTag(stack);
        if (tag == null) {
            ownership = 1;
        } else if (hasCompatibilityOwner(tag) && chestCavity != null && tag.getUniqueId(COMPATIBILITY_OWNER_KEY).equals(chestCavity.getCompatibilityId())) {
            ownership = 2;
        }
        return Math.max(oNegative, ownership);
    }

    public static boolean hasCompatibilityTag(ItemStack stack) {
        return getCompatibilityTag(stack) != null;
    }

    public static String getCompatibilityName(ItemStack stack) {
        NBTTagCompound tag = getCompatibilityTag(stack);
        return tag == null ? "" : tag.getString(COMPATIBILITY_NAME_KEY);
    }

    private static void addOrganScores(Map<ResourceLocation, Float> scores, OrganData data, ItemStack stack) {
        float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
        for (Map.Entry<ResourceLocation, Float> entry : data.getOrganScores().entrySet()) {
            Float old = scores.get(entry.getKey());
            float value = entry.getValue() * stackRatio;
            scores.put(entry.getKey(), old == null ? value : old + value);
        }
    }

    private static void setOrganCompatibility(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
        for (int i = 0; i < chestCavity.getSlotCount(); i++) {
            ItemStack stack = chestCavity.getOrgan(i);
            OrganData data = resolveOrganData(type, stack);
            if (data != null && !data.isPseudoOrgan()) {
                setCompatibilityTag(chestCavity, stack, owner);
            }
        }

        if (type.isPlayerChestCavity()) {
            return;
        }

        Random random = owner.getRNG();
        int universalOrgans = 0;
        if (type.isBossChestCavity()) {
            universalOrgans = 3 + random.nextInt(2) + random.nextInt(2);
        } else if (random.nextFloat() < CCConfig.UNIVERSAL_DONOR_RATE) {
            universalOrgans = 1 + random.nextInt(3) + random.nextInt(3);
        }

        while (universalOrgans > 0) {
            ItemStack stack = chestCavity.getOrgan(random.nextInt(chestCavity.getSlotCount()));
            OrganData data = resolveOrganData(type, stack);
            if (data != null && !data.isPseudoOrgan()) {
                removeCompatibilityTag(stack);
            }
            universalOrgans--;
        }
    }

    private static void resetPlayerChestCavityAfterDeath(IChestCavity oldCavity, IChestCavity newCavity) {
        if (CCConfig.KEEP_CHEST_CAVITY) {
            recalculateOrganScores(newCavity);
            applyAndSyncScoreChanges(newCavity);
            return;
        }

        Map<Integer, ItemStack> preserved = new LinkedHashMap<Integer, ItemStack>();
        if (oldCavity.isOpened()) {
            for (int slot = 0; slot < oldCavity.getSlotCount(); slot++) {
                ItemStack stack = oldCavity.getOrgan(slot);
                if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack) >= 2) {
                    preserved.put(slot, stack.copy());
                }
            }
        }

        newCavity.setCompatibilityId(UUID.randomUUID());
        if (newCavity.isOpened()) {
            ChestCavityType type = getChestCavityType(newCavity);
            for (int slot = 0; slot < newCavity.getSlotCount(); slot++) {
                ItemStack stack = slot < type.getDefaultChestCavity().size()
                        ? type.getDefaultChestCavity().getStack(slot)
                        : ItemStack.EMPTY;
                newCavity.setOrgan(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            setOrganCompatibility(newCavity);
            for (Map.Entry<Integer, ItemStack> entry : preserved.entrySet()) {
                if (entry.getKey() >= 0 && entry.getKey() < newCavity.getSlotCount()) {
                    newCavity.setOrgan(entry.getKey(), entry.getValue());
                }
            }
        }
        recalculateOrganScores(newCavity);
        applyAndSyncScoreChanges(newCavity);
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
        ChestCavityType type = getChestCavityType(chestCavity);
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

        ChestCavityType type = getChestCavityType(chestCavity);
        float defaultRecovery = type.getDefaultOrganScore(CCOrganScores.BREATH_RECOVERY);
        float recovery = chestCavity.getOrganScore(CCOrganScores.BREATH_RECOVERY);
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

        if (defaultRecovery > 0.0F && recovery <= 0.0F && !entity.isPotionActive(MobEffects.WATER_BREATHING)) {
            if (entity.ticksExisted % NO_BREATH_DAMAGE_RATE_TICKS == 0) {
                entity.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
            return;
        }

        chestCavity.setLungRemainder(0.0F);
    }

    private static void tickMetabolism(EntityLivingBase entity, IChestCavity chestCavity) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        if (!chestCavity.isOpened()) {
            rememberFoodExhaustion(player);
            return;
        }

        ChestCavityType type = getChestCavityType(chestCavity);
        float metabolismDiff = chestCavity.getOrganScore(CCOrganScores.METABOLISM)
                - type.getDefaultOrganScore(CCOrganScores.METABOLISM);
        if (metabolismDiff > 0.0F) {
            player.addExhaustion(metabolismDiff * 0.005F);
        }
        applyEnduranceExhaustion(player, chestCavity, type);
    }

    private static void tickPassiveEffects(EntityLivingBase entity, IChestCavity chestCavity) {
        float glowing = chestCavity.getOrganScore(CCOrganScores.GLOWING);
        if (glowing > 0.0F && !entity.isPotionActive(MobEffects.GLOWING)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0, false, true));
        }

        ChestCavityType type = getChestCavityType(chestCavity);
        applyLightweight(entity, chestCavity, type);

        float buoyant = chestCavity.getOrganScore(CCOrganScores.BUOYANT)
                - type.getDefaultOrganScore(CCOrganScores.BUOYANT);
        if (buoyant > 0.0F && !entity.onGround && !entity.hasNoGravity()) {
            entity.motionY += buoyant * CCConfig.BUOYANCY_LIFT * Math.max(0.0F, entity.getAir() / 300.0F);
            entity.velocityChanged = true;
        }

        float hydroallergenic = chestCavity.getOrganScore(CCOrganScores.HYDROALLERGENIC);
        if (hydroallergenic > 0.0F && entity.isWet()) {
            int rate = Math.max(20, (int) (HYDROALLERGENIC_BASE_RATE_TICKS / hydroallergenic));
            if (entity.ticksExisted % rate == 0) {
                entity.attackEntityFrom(DamageSource.MAGIC, entity.isInsideOfMaterial(Material.WATER) ? 10.0F : 1.0F);
            }
        }

        float hydrophobia = chestCavity.getOrganScore(CCOrganScores.HYDROPHOBIA);
        if (hydrophobia > 0.0F
                && type.getDefaultOrganScore(CCOrganScores.HYDROPHOBIA) <= 0.0F
                && entity.isWet()
                && entity.ticksExisted % HYDROPHOBIA_INTERVAL_TICKS == 0) {
            attemptRandomTeleport(entity, hydrophobia * CCConfig.ARROW_DODGE_DISTANCE);
        }

        tickPhotosynthesis(entity, chestCavity, type);
        tickCrystalsynthesis(entity, chestCavity);
    }

    private static void tickProjectileQueue(EntityLivingBase entity, IChestCavity chestCavity) {
        if (entity.ticksExisted % 5 != 0) {
            return;
        }
        ResourceLocation abilityId = chestCavity.pollProjectileAbility();
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

    private static void applyAndSyncScoreChanges(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        if (owner == null) {
            return;
        }

        if (!owner.world.isRemote) {
            applyBasicAttributeModifiers(owner, chestCavity);
        }

        if (hasScoreChanges(chestCavity)) {
            chestCavity.copyCurrentScoresToOld();
            if (!owner.world.isRemote) {
                ChestCavityNetwork.sendChestCavitySync(owner);
            }
        }
    }

    private static void drawOrgansFromPile(List<ItemStack> organPile, int rolls, Random random, List<ItemStack> loot) {
        LinkedList<ItemStack> remaining = new LinkedList<ItemStack>();
        for (ItemStack stack : organPile) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }

        for (int i = 0; i < rolls && !remaining.isEmpty(); i++) {
            ItemStack rolledItem = remaining.remove(random.nextInt(remaining.size())).copy();
            int count = 1;
            if (rolledItem.getCount() > 1) {
                count += random.nextInt(rolledItem.getMaxStackSize());
            }
            rolledItem.setCount(count);
            loot.add(rolledItem);
        }
    }

    private static OrganData resolveOrganData(ChestCavityType type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        OrganData data = type == null ? null : type.catchExceptionalOrgan(stack);
        if (data == null) {
            data = OrganData.fromStack(stack);
        }
        return data;
    }

    private static void setCompatibilityTag(IChestCavity chestCavity, ItemStack stack, EntityLivingBase owner) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }

        NBTTagCompound compatibility = new NBTTagCompound();
        compatibility.setUniqueId(COMPATIBILITY_OWNER_KEY, chestCavity.getCompatibilityId());
        compatibility.setString(COMPATIBILITY_NAME_KEY, owner.getDisplayName().getUnformattedText());
        root.setTag(COMPATIBILITY_TAG, compatibility);
    }

    private static void removeCompatibilityTag(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTagCompound()) {
            stack.getTagCompound().removeTag(COMPATIBILITY_TAG);
        }
    }

    private static NBTTagCompound getCompatibilityTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(COMPATIBILITY_TAG, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        return root.getCompoundTag(COMPATIBILITY_TAG);
    }

    private static boolean hasCompatibilityOwner(NBTTagCompound tag) {
        return tag.hasKey(COMPATIBILITY_OWNER_KEY + "Most", Constants.NBT.TAG_LONG)
                && tag.hasKey(COMPATIBILITY_OWNER_KEY + "Least", Constants.NBT.TAG_LONG);
    }

    private static void processButchering(List<ItemStack> loot) {
        Map<SalvageRecipe, Integer> salvageResults = new LinkedHashMap<SalvageRecipe, Integer>();
        Iterator<ItemStack> iterator = loot.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            SalvageRecipe recipe = findSalvageRecipe(stack);
            if (recipe != null) {
                Integer old = salvageResults.get(recipe);
                salvageResults.put(recipe, (old == null ? 0 : old) + stack.getCount());
                iterator.remove();
            }
        }

        for (Map.Entry<SalvageRecipe, Integer> entry : salvageResults.entrySet()) {
            ItemStack result = entry.getKey().getResultForInputCount(entry.getValue());
            if (!result.isEmpty()) {
                loot.add(result);
            }
        }
    }

    private static SalvageRecipe findSalvageRecipe(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe instanceof SalvageRecipe && ((SalvageRecipe) recipe).getInput().apply(stack)) {
                return (SalvageRecipe) recipe;
            }
        }
        return null;
    }

    private static void processMalpractice(List<ItemStack> loot) {
        for (ItemStack stack : loot) {
            OrganData data = OrganManager.get(stack);
            if (data != null && !data.isPseudoOrgan()) {
                stack.addEnchantment(CCEnchantments.MALPRACTICE, 1);
            }
        }
    }

    private static boolean hasOreName(ItemStack stack, String name) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (name.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    private static void applyBasicAttributeModifiers(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
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

    private static void tickBasicSurvival(EntityLivingBase entity, IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
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
        int cap = getChestCavityType(chestCavity).getHeartBleedCap();
        entity.attackEntityFrom(HEART_BLEED_DAMAGE, cap == Integer.MAX_VALUE ? bleedLevel : Math.min(bleedLevel, cap));
    }

    private static float applyDamageResistance(float score, float defense, float damage) {
        if (score <= 0.0F || damage <= 0.0F) {
            return damage;
        }
        return (float) (damage * Math.pow(1.0F - defense, score / 4.0F));
    }

    private static void applyLaunching(EntityLivingBase attacker, EntityLivingBase target, IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
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

    private static boolean spinWeb(EntityLivingBase entity, float silkScore) {
        int exhaustionCost = 0;
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getFoodStats().getFoodLevel() < 6) {
            return false;
        }

        if (silkScore >= 2.0F) {
            BlockPos pos = new BlockPos(entity).offset(entity.getHorizontalFacing().getOpposite());
            if (entity.world.isAirBlock(pos)) {
                if (silkScore >= 3.0F && entity.world.setBlockState(pos, Blocks.WOOL.getDefaultState(), 2)) {
                    exhaustionCost = 16;
                    silkScore -= 3.0F;
                } else if (entity.world.setBlockState(pos, Blocks.WEB.getDefaultState(), 2)) {
                    exhaustionCost = 8;
                    silkScore -= 2.0F;
                }
            }
        }

        while (silkScore >= 1.0F) {
            silkScore -= 1.0F;
            exhaustionCost += 4;
            entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY + 0.5D, entity.posZ,
                    new ItemStack(Items.STRING)));
        }
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).addExhaustion(exhaustionCost);
        }
        return exhaustionCost > 0;
    }

    private static int applyDigestion(EntityPlayer player, float digestion, int food) {
        if (digestion < 0.0F) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (int) (-food * digestion * 400.0F)));
            return 1;
        }
        return Math.max((int) (food * digestion), 1);
    }

    private static float applyNutrition(EntityPlayer player, float nutrition, float saturation) {
        if (nutrition < 0.0F) {
            player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, (int) (-saturation * nutrition * 800.0F)));
            return 0.0F;
        }
        return saturation * nutrition / 4.0F;
    }

    private static void adjustFoodStats(EntityPlayer player, int vanillaFood, float vanillaSaturation, int effectiveFood, float effectiveSaturation) {
        FoodStats stats = player.getFoodStats();
        NBTTagCompound foodTag = new NBTTagCompound();
        stats.writeNBT(foodTag);

        int foodDelta = effectiveFood - vanillaFood;
        int foodLevel = Math.max(0, Math.min(20, stats.getFoodLevel() + foodDelta));
        float saturationDelta = effectiveFood * effectiveSaturation * 2.0F - vanillaFood * vanillaSaturation * 2.0F;
        float saturation = Math.max(0.0F, Math.min(foodLevel, stats.getSaturationLevel() + saturationDelta));

        foodTag.setInteger("foodLevel", foodLevel);
        foodTag.setFloat("foodSaturationLevel", saturation);
        stats.readNBT(foodTag);
    }

    private static void applyEnduranceExhaustion(EntityPlayer player, IChestCavity chestCavity, ChestCavityType type) {
        float enduranceDiff = chestCavity.getOrganScore(CCOrganScores.ENDURANCE)
                - type.getDefaultOrganScore(CCOrganScores.ENDURANCE);
        FoodStats stats = player.getFoodStats();
        float current = getFoodExhaustion(stats);
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
        setFoodExhaustion(stats, adjusted);
        entityData.setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, adjusted);
    }

    private static void rememberFoodExhaustion(EntityPlayer player) {
        player.getEntityData().setFloat(ENDURANCE_LAST_EXHAUSTION_KEY, getFoodExhaustion(player.getFoodStats()));
    }

    private static float getFoodExhaustion(FoodStats stats) {
        NBTTagCompound foodTag = new NBTTagCompound();
        stats.writeNBT(foodTag);
        return foodTag.getFloat(FOOD_EXHAUSTION_KEY);
    }

    private static void setFoodExhaustion(FoodStats stats, float exhaustion) {
        NBTTagCompound foodTag = new NBTTagCompound();
        stats.writeNBT(foodTag);
        foodTag.setFloat(FOOD_EXHAUSTION_KEY, exhaustion);
        stats.readNBT(foodTag);
    }

    private static boolean isMeatFood(ItemFood food, ItemStack stack) {
        return food.isWolfsFavoriteMeat() || stack.getItem() == Items.ROTTEN_FLESH;
    }

    private static void applyHumanPrionRisk(EntityPlayer player, ItemStack eaten) {
        int amplifier = getPrionAmplifier(eaten);
        if (amplifier < 0) {
            return;
        }

        Random random = player.getRNG();
        if (random.nextFloat() < CCConfig.RISK_OF_PRIONS) {
            player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, PRION_DURATION_TICKS, amplifier));
        }
        if (random.nextFloat() < CCConfig.RISK_OF_PRIONS) {
            player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, PRION_DURATION_TICKS, amplifier));
        }
        if (random.nextFloat() < CCConfig.RISK_OF_PRIONS) {
            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, PRION_DURATION_TICKS, amplifier));
        }
    }

    private static int getPrionAmplifier(ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !"chestcavity".equals(id.getNamespace())) {
            return -1;
        }

        String path = id.getPath();
        if ("cooked_human_organ_meat".equals(path)
                || "cooked_man_meat".equals(path)
                || "human_sausage".equals(path)
                || "rich_human_sausage".equals(path)) {
            return 0;
        }
        if ("appendix".equals(path)
                || "heart".equals(path)
                || "intestine".equals(path)
                || "kidney".equals(path)
                || "liver".equals(path)
                || "lung".equals(path)
                || "muscle".equals(path)
                || "spleen".equals(path)
                || "stomach".equals(path)
                || "raw_human_organ_meat".equals(path)
                || "raw_man_meat".equals(path)
                || "raw_human_sausage".equals(path)
                || "raw_rich_human_sausage".equals(path)) {
            return 1;
        }
        return -1;
    }

    private static boolean isRottenFood(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation id = item.getRegistryName();
        return item == Items.ROTTEN_FLESH || id != null && id.getPath().contains("rotten");
    }

    private static boolean isFurnacePowerFood(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation id = item.getRegistryName();
        return id != null && "chestcavity".equals(id.getNamespace()) && "furnace_power".equals(id.getPath());
    }

    private static float durationReductionFactor(float score, float scalar) {
        return score <= 0.0F ? 1.0F : 1.0F / (1.0F + scalar * score);
    }

    private static float detoxificationDurationFactor(IChestCavity chestCavity) {
        ChestCavityType type = getChestCavityType(chestCavity);
        float defaultDetoxification = type.getDefaultOrganScore(CCOrganScores.DETOXIFICATION);
        float detoxification = chestCavity.getOrganScore(CCOrganScores.DETOXIFICATION);
        if (defaultDetoxification <= 0.0F || detoxification == defaultDetoxification) {
            return 1.0F;
        }

        float ratio = detoxification / defaultDetoxification;
        return ratio > -1.0F ? Math.max(0.05F, 2.0F / (1.0F + ratio)) : 9999.0F;
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

    private static boolean attemptRandomTeleport(EntityLivingBase entity, float range) {
        if (entity.world.isRemote || !entity.isEntityAlive()) {
            return false;
        }

        for (int i = 0; i < Math.max(1, CCConfig.MAX_TELEPORT_ATTEMPTS); i++) {
            double x = entity.posX + (entity.getRNG().nextDouble() - 0.5D) * range;
            double y = Math.max(1.0D, entity.posY + (entity.getRNG().nextDouble() - 0.5D) * range);
            double z = entity.posZ + (entity.getRNG().nextDouble() - 0.5D) * range;
            if (entity.attemptTeleport(x, y, z)) {
                entity.world.playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
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

    private static void disconnectCrystal(IChestCavity chestCavity) {
        EntityLivingBase owner = chestCavity.getOwner();
        EntityEnderCrystal crystal = owner == null ? null : getConnectedCrystal(owner, chestCavity);
        if (crystal != null) {
            crystal.setBeamTarget(null);
        }
        chestCavity.setConnectedCrystalId(-1);
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
