package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FoodScoreEvents {

    private static final int PRION_DURATION_TICKS = 24000;
    private static final FoodEffectSpec[] FOOD_EFFECTS = new FoodEffectSpec[] {
            FoodScoreEvents::applyNutritionProfile
    };

    private FoodScoreEvents() {
    }

    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            applyFoodEffects((EntityPlayer) event.getEntityLiving(), event.getItem());
        }
    }

    public static void consumeFurnacePowerFood(EntityPlayer player) {
        if (player == null || player.world.isRemote || !(CCItems.FURNACE_POWER instanceof ItemFood)) {
            return;
        }

        ItemStack stack = new ItemStack(CCItems.FURNACE_POWER);
        player.getFoodStats().addStats((ItemFood) stack.getItem(), stack);
        applyFoodEffects(player, stack);
    }

    private static void applyFoodEffects(EntityPlayer player, ItemStack eaten) {
        if (player == null || player.world.isRemote || eaten == null || eaten.isEmpty() || !(eaten.getItem() instanceof ItemFood)) {
            return;
        }

        applyHumanPrionRisk(player, eaten);

        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity == null || !chestCavity.isOpened()) {
            return;
        }

        FoodContext context = new FoodContext(player, chestCavity, eaten, (ItemFood) eaten.getItem());
        for (FoodEffectSpec spec : FOOD_EFFECTS) {
            spec.apply(context);
        }
    }

    private static void applyNutritionProfile(FoodContext context) {
        int vanillaFood = context.food.getHealAmount(context.stack);
        float vanillaSaturation = context.food.getSaturationModifier(context.stack);
        float digestion = context.chestCavity.getOrganScore(CCOrganScores.DIGESTION);
        float nutrition = context.chestCavity.getOrganScore(CCOrganScores.NUTRITION);

        int effectiveFood = applyDigestion(context.player, digestion, vanillaFood);
        float effectiveSaturation = applyNutrition(context.player, nutrition, vanillaSaturation);
        adjustFoodStats(context.player, vanillaFood, vanillaSaturation, effectiveFood, effectiveSaturation);
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

        int foodDelta = effectiveFood - vanillaFood;
        int foodLevel = Math.max(0, Math.min(20, stats.getFoodLevel() + foodDelta));
        float saturationDelta = effectiveFood * effectiveSaturation * 2.0F - vanillaFood * vanillaSaturation * 2.0F;
        float saturation = Math.max(0.0F, Math.min(foodLevel, stats.getSaturationLevel() + saturationDelta));

        stats.foodLevel = foodLevel;
        stats.foodSaturationLevel = saturation;
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

    private interface FoodEffectSpec {
        void apply(FoodContext context);
    }

    private static final class FoodContext {
        private final EntityPlayer player;
        private final ChestCavityData chestCavity;
        private final ItemStack stack;
        private final ItemFood food;

        private FoodContext(EntityPlayer player, ChestCavityData chestCavity, ItemStack stack, ItemFood food) {
            this.player = player;
            this.chestCavity = chestCavity;
            this.stack = stack;
            this.food = food;
        }
    }
}
