package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.network.ChestCavityNetwork;
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

public final class ChestCavityHelper {

    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation("chestcavity", "chest_cavity");

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
        OrganTickController.tick(entity, chestCavity);
    }

    public static boolean hasScoreChanges(IChestCavity chestCavity) {
        return !chestCavity.getOldOrganScores().equals(chestCavity.getOrganScores());
    }

    public static void recalculateOrganScores(IChestCavity chestCavity) {
        OrganScoreCalculator.recalculate(chestCavity);
    }

    public static void setOrganAndRecalculate(IChestCavity chestCavity, int slot, ItemStack stack) {
        OrganLifecycleController.setOrganAndRecalculate(chestCavity, slot, stack);
    }

    public static void openChestCavity(IChestCavity chestCavity) {
        OrganLifecycleController.openChestCavity(chestCavity);
    }

    public static void copy(EntityLivingBase original, EntityLivingBase replacement) {
        copy(original, replacement, false);
    }

    public static void copy(EntityLivingBase original, EntityLivingBase replacement, boolean wasDeath) {
        OrganLifecycleController.copy(original, replacement, wasDeath);
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
        return OrganCombatController.applyDefense(chestCavity, source, damage);
    }

    public static boolean attemptProjectileDodge(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source) {
        return OrganCombatController.attemptProjectileDodge(entity, chestCavity, source);
    }

    public static void adjustIncomingPotionEffect(EntityLivingBase entity, PotionEffect effect) {
        OrganCombatController.adjustIncomingPotionEffect(entity, effect);
    }

    public static float applyFinalDamageEffects(EntityLivingBase target, DamageSource source, float damage) {
        return OrganCombatController.applyFinalDamageEffects(target, source, damage);
    }

    public static void applyDestructiveCollisions(EntityLivingBase entity, IChestCavity chestCavity, DamageSource source, float damage) {
        OrganCombatController.applyDestructiveCollisions(entity, chestCavity, source, damage);
    }

    public static void destroyOrgansWithScore(IChestCavity chestCavity, String scoreId) {
        OrganLifecycleController.destroyOrgansWithScore(chestCavity, scoreId);
    }

    public static void applyWaterSplash(Entity source) {
        OrganInteractionController.applyWaterSplash(source);
    }

    public static boolean milkSilk(EntityLivingBase entity) {
        return OrganInteractionController.milkSilk(entity);
    }

    public static boolean shearSilk(EntityLivingBase entity) {
        return OrganInteractionController.shearSilk(entity);
    }

    public static void applyJump(EntityLivingBase entity, IChestCavity chestCavity) {
        OrganCombatController.applyJump(entity, chestCavity);
    }

    public static void applyFoodEffects(EntityPlayer player, ItemStack eaten) {
        OrganFoodController.applyFoodEffects(player, eaten);
    }

    public static void consumeFurnacePowerFood(EntityPlayer player) {
        OrganFoodController.consumeFurnacePowerFood(player);
    }

    public static boolean isSlotForbidden(IChestCavity chestCavity, int slot) {
        return chestCavity == null || getChestCavityType(chestCavity).isSlotForbidden(slot);
    }

    public static boolean isOpenable(IChestCavity chestCavity) {
        return OrganLifecycleController.isOpenable(chestCavity);
    }

    public static boolean hasAssignedChestCavityType(IChestCavity chestCavity) {
        return OrganTypeResolver.hasAssignedType(chestCavity);
    }

    public static ChestCavityType getChestCavityType(IChestCavity chestCavity) {
        return OrganTypeResolver.getType(chestCavity);
    }

    public static List<ItemStack> generateUnopenedOrganDrops(IChestCavity chestCavity, Random random, int baseLooting, EntityLivingBase killer) {
        return OrganDropController.generateUnopenedOrganDrops(chestCavity, random, baseLooting, killer);
    }

    public static List<ItemStack> removeUnboundOrgansForDeath(IChestCavity chestCavity) {
        return OrganDropController.removeUnboundOrgansForDeath(chestCavity);
    }

    public static int getCompatibilityLevel(IChestCavity chestCavity, ItemStack stack) {
        return OrganCompatibility.getLevel(chestCavity, stack);
    }

    public static boolean hasCompatibilityTag(ItemStack stack) {
        return OrganCompatibility.hasTag(stack);
    }

    public static String getCompatibilityName(ItemStack stack) {
        return OrganCompatibility.getName(stack);
    }

    public static void applyAndSyncScoreChanges(IChestCavity chestCavity) {
        OrganLifecycleController.applyAndSyncScoreChanges(chestCavity);
    }
}
