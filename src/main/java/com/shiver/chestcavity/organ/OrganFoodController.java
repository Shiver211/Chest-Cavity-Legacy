package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.mixin.FoodStatsAccessor;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

/**
 * Food intake, spleen metabolism, and endurance.
 */
public final class OrganFoodController {

    private static final String ENDURANCE_LAST_EXHAUSTION_KEY = Tags.MOD_ID + ":last_exhaustion";
    private static final String FOOD_EXHAUSTION_KEY = "foodExhaustionLevel";
    private static final int PRION_DURATION_TICKS = 24000;

    private OrganFoodController() {
    }

    /**
     * Applies organ food modifiers before vanilla hunger is added.
     *
     * @return true if vanilla addStats should be skipped
     */
    public static boolean handleEatenFood(FoodStats stats, EntityPlayer player, ItemFood food, ItemStack eaten) {
        if (player == null || player.world.isRemote || food == null || eaten == null || eaten.isEmpty()) {
            return false;
        }

        applyHumanPrionRisk(player, eaten);

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return false;
        }

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
        }

        int nausea = OrganFormulas.nauseaTicks(digestion, vanillaFood);
        if (nausea > 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, nausea));
        }
        int hunger = OrganFormulas.hungerTicks(nutrition, vanillaSaturation);
        if (hunger > 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, hunger));
        }

        stats.addStats(OrganFormulas.digestedHunger(digestion, vanillaFood),
                OrganFormulas.digestedSaturation(nutrition, vanillaSaturation));
        return true;
    }

    public static void finishEatingFood(EntityPlayer player, ItemStack eaten) {
        if (player.world.isRemote || eaten.isEmpty() || !(eaten.getItem() instanceof ItemFood) || !isRottenFood(eaten)) {
            return;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity != null && chestCavity.isOpened()
                && chestCavity.getOrganScore(CCOrganScores.ROTGUT) + chestCavity.getOrganScore(CCOrganScores.ROT_DIGESTION) > 0.0F) {
            // ItemFood applies its potion effects after FoodStats.addStats.
            player.removePotionEffect(MobEffects.HUNGER);
        }
    }

    public static void consumeFurnacePowerFood(EntityPlayer player) {
        if (player == null || player.world.isRemote || !(CCItems.FURNACE_POWER instanceof ItemFood)) {
            return;
        }

        ItemStack stack = new ItemStack(CCItems.FURNACE_POWER);
        player.getFoodStats().addStats((ItemFood) stack.getItem(), stack);
    }

    public static int applySpleenMetabolism(EntityPlayer player, int foodTimer) {
        if (player == null || player.world.isRemote) {
            return foodTimer;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return foodTimer;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float metabolismDiff = chestCavity.getOrganScore(CCOrganScores.METABOLISM)
                - type.getDefaultOrganScore(CCOrganScores.METABOLISM);
        OrganFormulas.MetabolismTick tick = OrganFormulas.applySpleenMetabolism(
                foodTimer, metabolismDiff, chestCavity.getMetabolismRemainder());
        chestCavity.setMetabolismRemainder(tick.remainder);
        return tick.foodTimer;
    }

    static void tickMetabolism(EntityPlayer player, IChestCavity chestCavity) {
        if (!chestCavity.isOpened()) {
            rememberFoodExhaustion(player);
            return;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        applyEnduranceExhaustion(player, chestCavity, type);
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
        if (stats instanceof FoodStatsAccessor) {
            return ((FoodStatsAccessor) stats).chestcavity$getFoodExhaustionLevel();
        }
        return 0.0F;
    }

    private static void setFoodExhaustion(FoodStats stats, float exhaustion) {
        if (stats instanceof FoodStatsAccessor) {
            ((FoodStatsAccessor) stats).chestcavity$setFoodExhaustionLevel(exhaustion);
        }
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
        if (id == null || !Tags.MOD_ID.equals(id.getNamespace())) {
            return -1;
        }

        String path = id.getPath();
        if ("appendix".equals(path)
                || "heart".equals(path)
                || "intestine".equals(path)
                || "kidney".equals(path)
                || "liver".equals(path)
                || "lung".equals(path)
                || "muscle".equals(path)
                || "spleen".equals(path)
                || "stomach".equals(path)) {
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
        return id != null && Tags.MOD_ID.equals(id.getNamespace()) && "furnace_power".equals(id.getPath());
    }
}
