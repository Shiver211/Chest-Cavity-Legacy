package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
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

final class OrganFoodController {

    private static final String ENDURANCE_LAST_EXHAUSTION_KEY = "chestcavity:last_exhaustion";
    private static final String FOOD_EXHAUSTION_KEY = "foodExhaustionLevel";
    private static final int PRION_DURATION_TICKS = 24000;

    private OrganFoodController() {
    }

    static void applyFoodEffects(EntityPlayer player, ItemStack eaten) {
        if (player == null || player.world.isRemote || eaten == null || eaten.isEmpty() || !(eaten.getItem() instanceof ItemFood)) {
            return;
        }

        applyHumanPrionRisk(player, eaten);

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
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

    static void consumeFurnacePowerFood(EntityPlayer player) {
        if (player == null || player.world.isRemote || !(CCItems.FURNACE_POWER instanceof ItemFood)) {
            return;
        }

        ItemStack stack = new ItemStack(CCItems.FURNACE_POWER);
        player.getFoodStats().addStats((ItemFood) stack.getItem(), stack);
        applyFoodEffects(player, stack);
    }

    static void tickMetabolism(EntityPlayer player, IChestCavity chestCavity) {
        if (!chestCavity.isOpened()) {
            rememberFoodExhaustion(player);
            return;
        }

        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        float metabolismDiff = chestCavity.getOrganScore(CCOrganScores.METABOLISM)
                - type.getDefaultOrganScore(CCOrganScores.METABOLISM);
        if (metabolismDiff > 0.0F) {
            player.addExhaustion(metabolismDiff * 0.005F);
        }
        applyEnduranceExhaustion(player, chestCavity, type);
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
}
