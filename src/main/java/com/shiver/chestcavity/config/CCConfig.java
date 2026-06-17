package com.shiver.chestcavity.config;

import com.shiver.chestcavity.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class CCConfig {

    public static String DEFAULT_CHEST_CAVITY = "chestcavity:types/dirt.json";
    public static float ORGAN_BUNDLE_LOOTING_BOOST = 0.04F;
    public static float UNIVERSAL_DONOR_RATE = 0.1F;
    public static int ORGAN_REJECTION_DAMAGE = 2;
    public static int ORGAN_REJECTION_RATE = 600;
    public static int HEARTBLEED_RATE = 20;
    public static int KIDNEY_RATE = 60;
    public static float FILTRATION_DURATION_FACTOR = 1.0F;
    public static float APPENDIX_LUCK = 0.1F;
    public static float HEART_HP = 4.0F;
    public static float MUSCLE_STRENGTH = 1.0F;
    public static float MUSCLE_SPEED = 0.5F;
    public static float NERVES_HASTE = 0.1F;
    public static float BONE_DEFENSE = 0.5F;
    public static float RISK_OF_PRIONS = 0.01F;
    public static int CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD = 20;
    public static float CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD = 0.5F;
    public static boolean CAN_OPEN_OTHER_PLAYERS = false;
    public static boolean KEEP_CHEST_CAVITY = false;
    public static boolean DISABLE_ORGAN_REJECTION = false;

    public static int ARROW_DODGE_DISTANCE = 32;
    public static float BUFF_PURGING_DURATION_FACTOR = 0.5F;
    public static float BUOYANCY_LIFT = 0.015F;
    public static int CRYSTALSYNTHESIS_RANGE = 32;
    public static int CRYSTALSYNTHESIS_FREQUENCY = 10;
    public static float FIREPROOF_DEFENSE = 0.75F;
    public static float IMPACT_DEFENSE = 0.75F;
    public static float IRON_REPAIR_PERCENT = 0.25F;
    public static float LAUNCHING_POWER = 0.1F;
    public static float LEAPING_POWER = 0.25F;
    public static float LIGHTWIEGHT_FACTOR = 0.25F;
    public static int MAX_TELEPORT_ATTEMPTS = 5;
    public static int PHOTOSYNTHESIS_FREQUENCY = 50;
    public static int RUMINATION_TIME = 400;
    public static int RUMINATION_GRASS_PER_SQUARE = 2;
    public static int RUMINATION_SQUARES_PER_STOMACH = 3;
    public static int SHULKER_BULLET_TARGETING_RANGE = 20;
    public static float SWIMSPEED_FACTOR = 1.0F;
    public static float WITHERED_DURATION_FACTOR = 0.5F;

    public static int ARROW_DODGE_COOLDOWN = 200;
    public static int DRAGON_BOMB_COOLDOWN = 200;
    public static int DRAGON_BREATH_COOLDOWN = 200;
    public static int EXPLOSION_COOLDOWN = 200;
    public static int FORCEFUL_SPIT_COOLDOWN = 20;
    public static int GHASTLY_COOLDOWN = 60;
    public static int IRON_REPAIR_COOLDOWN = 1200;
    public static int PYROMANCY_COOLDOWN = 78;
    public static int SHULKER_BULLET_COOLDOWN = 100;
    public static int SILK_COOLDOWN = 20;
    public static int VENOM_COOLDOWN = 40;

    public static boolean BACKROOMS_INTEGRATION = true;
    public static int BACKROOMS_CHEST_ORGAN_LOOT_ATTEMPTS = 2;
    public static float BACKROOMS_CHEST_ORGAN_LOOT_CHANCE = 0.2F;
    public static boolean REQUIEM_INTEGRATION = true;

    private static Configuration configuration;

    private CCConfig() {
    }

    public static void load(File file) {
        configuration = new Configuration(file);
        sync();
    }

    public static void sync() {
        if (configuration == null) {
            return;
        }

        DEFAULT_CHEST_CAVITY = configuration.getString("DEFAULT_CHEST_CAVITY", "core", DEFAULT_CHEST_CAVITY, "");
        ORGAN_BUNDLE_LOOTING_BOOST = getFloat("core", "ORGAN_BUNDLE_LOOTING_BOOST", ORGAN_BUNDLE_LOOTING_BOOST);
        UNIVERSAL_DONOR_RATE = getFloat("core", "UNIVERSAL_DONOR_RATE", UNIVERSAL_DONOR_RATE);
        ORGAN_REJECTION_DAMAGE = getInt("core", "ORGAN_REJECTION_DAMAGE", ORGAN_REJECTION_DAMAGE);
        ORGAN_REJECTION_RATE = getInt("core", "ORGAN_REJECTION_RATE", ORGAN_REJECTION_RATE);
        HEARTBLEED_RATE = getInt("core", "HEARTBLEED_RATE", HEARTBLEED_RATE);
        KIDNEY_RATE = getInt("core", "KIDNEY_RATE", KIDNEY_RATE);
        FILTRATION_DURATION_FACTOR = getFloat("core", "FILTRATION_DURATION_FACTOR", FILTRATION_DURATION_FACTOR);
        APPENDIX_LUCK = getFloat("core", "APPENDIX_LUCK", APPENDIX_LUCK);
        HEART_HP = getFloat("core", "HEART_HP", HEART_HP);
        MUSCLE_STRENGTH = getFloat("core", "MUSCLE_STRENGTH", MUSCLE_STRENGTH);
        MUSCLE_SPEED = getFloat("core", "MUSCLE_SPEED", MUSCLE_SPEED);
        NERVES_HASTE = getFloat("core", "NERVES_HASTE", NERVES_HASTE);
        BONE_DEFENSE = getFloat("core", "BONE_DEFENSE", BONE_DEFENSE);
        RISK_OF_PRIONS = getFloat("core", "RISK_OF_PRIONS", RISK_OF_PRIONS);
        CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD = getInt("core", "CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD", CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD);
        CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD = getFloat("core", "CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD", CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD);
        CAN_OPEN_OTHER_PLAYERS = configuration.getBoolean("CAN_OPEN_OTHER_PLAYERS", "core", CAN_OPEN_OTHER_PLAYERS, "");
        KEEP_CHEST_CAVITY = configuration.getBoolean("KEEP_CHEST_CAVITY", "core", KEEP_CHEST_CAVITY, "");
        DISABLE_ORGAN_REJECTION = configuration.getBoolean("DISABLE_ORGAN_REJECTION", "core", DISABLE_ORGAN_REJECTION, "");

        ARROW_DODGE_DISTANCE = getInt("more", "ARROW_DODGE_DISTANCE", ARROW_DODGE_DISTANCE);
        BUFF_PURGING_DURATION_FACTOR = getFloat("more", "BUFF_PURGING_DURATION_FACTOR", BUFF_PURGING_DURATION_FACTOR);
        BUOYANCY_LIFT = getFloat("more", "BUOYANCY_LIFT", BUOYANCY_LIFT);
        CRYSTALSYNTHESIS_RANGE = getInt("more", "CRYSTALSYNTHESIS_RANGE", CRYSTALSYNTHESIS_RANGE);
        CRYSTALSYNTHESIS_FREQUENCY = getInt("more", "CRYSTALSYNTHESIS_FREQUENCY", CRYSTALSYNTHESIS_FREQUENCY);
        FIREPROOF_DEFENSE = getFloat("more", "FIREPROOF_DEFENSE", FIREPROOF_DEFENSE);
        IMPACT_DEFENSE = getFloat("more", "IMPACT_DEFENSE", IMPACT_DEFENSE);
        IRON_REPAIR_PERCENT = getFloat("more", "IRON_REPAIR_PERCENT", IRON_REPAIR_PERCENT);
        LAUNCHING_POWER = getFloat("more", "LAUNCHING_POWER", LAUNCHING_POWER);
        LEAPING_POWER = getFloat("more", "LEAPING_POWER", LEAPING_POWER);
        LIGHTWIEGHT_FACTOR = getFloat("more", "LIGHTWIEGHT_FACTOR", LIGHTWIEGHT_FACTOR);
        MAX_TELEPORT_ATTEMPTS = getInt("more", "MAX_TELEPORT_ATTEMPTS", MAX_TELEPORT_ATTEMPTS);
        PHOTOSYNTHESIS_FREQUENCY = getInt("more", "PHOTOSYNTHESIS_FREQUENCY", PHOTOSYNTHESIS_FREQUENCY);
        RUMINATION_TIME = getInt("more", "RUMINATION_TIME", RUMINATION_TIME);
        RUMINATION_GRASS_PER_SQUARE = getInt("more", "RUMINATION_GRASS_PER_SQUARE", RUMINATION_GRASS_PER_SQUARE);
        RUMINATION_SQUARES_PER_STOMACH = getInt("more", "RUMINATION_SQUARES_PER_STOMACH", RUMINATION_SQUARES_PER_STOMACH);
        SHULKER_BULLET_TARGETING_RANGE = getInt("more", "SHULKER_BULLET_TARGETING_RANGE", SHULKER_BULLET_TARGETING_RANGE);
        SWIMSPEED_FACTOR = getFloat("more", "SWIMSPEED_FACTOR", SWIMSPEED_FACTOR);
        WITHERED_DURATION_FACTOR = getFloat("more", "WITHERED_DURATION_FACTOR", WITHERED_DURATION_FACTOR);

        ARROW_DODGE_COOLDOWN = getInt("cooldown", "ARROW_DODGE_COOLDOWN", ARROW_DODGE_COOLDOWN);
        DRAGON_BOMB_COOLDOWN = getInt("cooldown", "DRAGON_BOMB_COOLDOWN", DRAGON_BOMB_COOLDOWN);
        DRAGON_BREATH_COOLDOWN = getInt("cooldown", "DRAGON_BREATH_COOLDOWN", DRAGON_BREATH_COOLDOWN);
        EXPLOSION_COOLDOWN = getInt("cooldown", "EXPLOSION_COOLDOWN", EXPLOSION_COOLDOWN);
        FORCEFUL_SPIT_COOLDOWN = getInt("cooldown", "FORCEFUL_SPIT_COOLDOWN", FORCEFUL_SPIT_COOLDOWN);
        GHASTLY_COOLDOWN = getInt("cooldown", "GHASTLY_COOLDOWN", GHASTLY_COOLDOWN);
        IRON_REPAIR_COOLDOWN = getInt("cooldown", "IRON_REPAIR_COOLDOWN", IRON_REPAIR_COOLDOWN);
        PYROMANCY_COOLDOWN = getInt("cooldown", "PYROMANCY_COOLDOWN", PYROMANCY_COOLDOWN);
        SHULKER_BULLET_COOLDOWN = getInt("cooldown", "SHULKER_BULLET_COOLDOWN", SHULKER_BULLET_COOLDOWN);
        SILK_COOLDOWN = getInt("cooldown", "SILK_COOLDOWN", SILK_COOLDOWN);
        VENOM_COOLDOWN = getInt("cooldown", "VENOM_COOLDOWN", VENOM_COOLDOWN);

        BACKROOMS_INTEGRATION = configuration.getBoolean("BACKROOMS_INTEGRATION", "integration", BACKROOMS_INTEGRATION, "");
        BACKROOMS_CHEST_ORGAN_LOOT_ATTEMPTS = getInt("integration", "BACKROOMS_CHEST_ORGAN_LOOT_ATTEMPTS", BACKROOMS_CHEST_ORGAN_LOOT_ATTEMPTS);
        BACKROOMS_CHEST_ORGAN_LOOT_CHANCE = getFloat("integration", "BACKROOMS_CHEST_ORGAN_LOOT_CHANCE", BACKROOMS_CHEST_ORGAN_LOOT_CHANCE);
        REQUIEM_INTEGRATION = configuration.getBoolean("REQUIEM_INTEGRATION", "integration", REQUIEM_INTEGRATION, "");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static ResourceLocation getDefaultChestCavityId() {
        try {
            return new ResourceLocation(DEFAULT_CHEST_CAVITY);
        } catch (RuntimeException ignored) {
            return new ResourceLocation(Tags.MOD_ID, "fallback");
        }
    }

    private static int getInt(String category, String name, int defaultValue) {
        return configuration.getInt(name, category, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, "");
    }

    private static float getFloat(String category, String name, float defaultValue) {
        return configuration.getFloat(name, category, defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE, "");
    }
}
