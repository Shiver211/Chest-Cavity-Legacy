package com.shiver.chestcavity.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Runtime configuration loaded from config/chestcavity.cfg.
 */
public final class CCConfig {

    public static String DEFAULT_CHEST_CAVITY = "dirt";
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

        DEFAULT_CHEST_CAVITY = configuration.getString("DEFAULT_CHEST_CAVITY", "core", DEFAULT_CHEST_CAVITY,
                "Fallback chest cavity type id used when an entity has no assignment.");
        ORGAN_BUNDLE_LOOTING_BOOST = getFloat("core", "ORGAN_BUNDLE_LOOTING_BOOST", ORGAN_BUNDLE_LOOTING_BOOST,
                "Extra unopened organ-drop chance per looting level.");
        UNIVERSAL_DONOR_RATE = getFloat("core", "UNIVERSAL_DONOR_RATE", UNIVERSAL_DONOR_RATE,
                "Chance that a freshly opened cavity contains unmarked compatible organs.");
        ORGAN_REJECTION_DAMAGE = getInt("core", "ORGAN_REJECTION_DAMAGE", ORGAN_REJECTION_DAMAGE,
                "Damage dealt by each organ-rejection tick.");
        ORGAN_REJECTION_RATE = getInt("core", "ORGAN_REJECTION_RATE", ORGAN_REJECTION_RATE,
                "Base rejection interval in ticks. Higher incompatibility shortens this.");
        HEARTBLEED_RATE = getInt("core", "HEARTBLEED_RATE", HEARTBLEED_RATE,
                "How often missing a required heart deals damage, in ticks.");
        KIDNEY_RATE = getInt("core", "KIDNEY_RATE", KIDNEY_RATE,
                "How often insufficient filtration applies poison, in ticks.");
        FILTRATION_DURATION_FACTOR = getFloat("core", "FILTRATION_DURATION_FACTOR", FILTRATION_DURATION_FACTOR,
                "How strongly extra filtration shortens poison duration.");
        APPENDIX_LUCK = getFloat("core", "APPENDIX_LUCK", APPENDIX_LUCK,
                "Luck attribute added per luck score.");
        HEART_HP = getFloat("core", "HEART_HP", HEART_HP,
                "Max health added per extra heart score.");
        MUSCLE_STRENGTH = getFloat("core", "MUSCLE_STRENGTH", MUSCLE_STRENGTH,
                "Attack damage multiplier contributed by 8 strength score.");
        MUSCLE_SPEED = getFloat("core", "MUSCLE_SPEED", MUSCLE_SPEED,
                "Movement speed multiplier contributed by 8 speed score.");
        NERVES_HASTE = getFloat("core", "NERVES_HASTE", NERVES_HASTE,
                "Attack and mining speed contributed by each nerve score.");
        BONE_DEFENSE = getFloat("core", "BONE_DEFENSE", BONE_DEFENSE,
                "Damage reduction from 4 extra defense score.");
        RISK_OF_PRIONS = getFloat("core", "RISK_OF_PRIONS", RISK_OF_PRIONS,
                "Chance that eating human organs inflicts a long debuff.");
        CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD = getInt("core", "CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD", CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD,
                "Absolute health at or below which another entity can be opened.");
        CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD = getFloat("core", "CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD", CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD,
                "Health fraction at or below which another entity can be opened.");
        CAN_OPEN_OTHER_PLAYERS = configuration.getBoolean("CAN_OPEN_OTHER_PLAYERS", "core", CAN_OPEN_OTHER_PLAYERS,
                "Allow opening another player's chest cavity.");
        KEEP_CHEST_CAVITY = configuration.getBoolean("KEEP_CHEST_CAVITY", "core", KEEP_CHEST_CAVITY,
                "Keep the player's organs after death.");
        DISABLE_ORGAN_REJECTION = configuration.getBoolean("DISABLE_ORGAN_REJECTION", "core", DISABLE_ORGAN_REJECTION,
                "Disable organ rejection damage entirely.");

        ARROW_DODGE_DISTANCE = getInt("more", "ARROW_DODGE_DISTANCE", ARROW_DODGE_DISTANCE,
                "Teleport range used when dodging projectiles.");
        BUFF_PURGING_DURATION_FACTOR = getFloat("more", "BUFF_PURGING_DURATION_FACTOR", BUFF_PURGING_DURATION_FACTOR,
                "How strongly buff purging shortens beneficial effects.");
        BUOYANCY_LIFT = getFloat("more", "BUOYANCY_LIFT", BUOYANCY_LIFT,
                "Upward velocity per extra buoyancy score while airborne.");
        CRYSTALSYNTHESIS_RANGE = getInt("more", "CRYSTALSYNTHESIS_RANGE", CRYSTALSYNTHESIS_RANGE,
                "Range for linking to an end crystal.");
        CRYSTALSYNTHESIS_FREQUENCY = getInt("more", "CRYSTALSYNTHESIS_FREQUENCY", CRYSTALSYNTHESIS_FREQUENCY,
                "How often the end-crystal link is updated, in ticks.");
        FIREPROOF_DEFENSE = getFloat("more", "FIREPROOF_DEFENSE", FIREPROOF_DEFENSE,
                "Fire damage reduction from 4 fire-resistant score.");
        IMPACT_DEFENSE = getFloat("more", "IMPACT_DEFENSE", IMPACT_DEFENSE,
                "Fall/impact damage reduction from 4 impact-resistant score.");
        IRON_REPAIR_PERCENT = getFloat("more", "IRON_REPAIR_PERCENT", IRON_REPAIR_PERCENT,
                "Max-health fraction healed by iron repair.");
        LAUNCHING_POWER = getFloat("more", "LAUNCHING_POWER", LAUNCHING_POWER,
                "Upward velocity applied to hit targets per launching score.");
        LEAPING_POWER = getFloat("more", "LEAPING_POWER", LEAPING_POWER,
                "Jump velocity multiplier per extra leaping score.");
        LIGHTWIEGHT_FACTOR = getFloat("more", "LIGHTWIEGHT_FACTOR", LIGHTWIEGHT_FACTOR,
                "Fall-speed scaling per lightweight score.");
        MAX_TELEPORT_ATTEMPTS = getInt("more", "MAX_TELEPORT_ATTEMPTS", MAX_TELEPORT_ATTEMPTS,
                "Random teleport attempts for dodging or hydrophobia.");
        PHOTOSYNTHESIS_FREQUENCY = getInt("more", "PHOTOSYNTHESIS_FREQUENCY", PHOTOSYNTHESIS_FREQUENCY,
                "Ticks 8 photosynthetic organs in sunlight need to restore 1 hunger.");
        RUMINATION_TIME = getInt("more", "RUMINATION_TIME", RUMINATION_TIME,
                "Ticks needed to chew one grass unit.");
        RUMINATION_GRASS_PER_SQUARE = getInt("more", "RUMINATION_GRASS_PER_SQUARE", RUMINATION_GRASS_PER_SQUARE,
                "Grass units obtained from one grazed block.");
        RUMINATION_SQUARES_PER_STOMACH = getInt("more", "RUMINATION_SQUARES_PER_STOMACH", RUMINATION_SQUARES_PER_STOMACH,
                "Grass squares one rumen can store.");
        SHULKER_BULLET_TARGETING_RANGE = getInt("more", "SHULKER_BULLET_TARGETING_RANGE", SHULKER_BULLET_TARGETING_RANGE,
                "Search range for shulker-bullet targets.");
        SWIMSPEED_FACTOR = getFloat("more", "SWIMSPEED_FACTOR", SWIMSPEED_FACTOR,
                "Swim speed contributed by 8 swim-speed score.");
        WITHERED_DURATION_FACTOR = getFloat("more", "WITHERED_DURATION_FACTOR", WITHERED_DURATION_FACTOR,
                "How strongly withered score shortens wither duration.");

        ARROW_DODGE_COOLDOWN = getInt("cooldown", "ARROW_DODGE_COOLDOWN", ARROW_DODGE_COOLDOWN,
                "Projectile-dodge cooldown in ticks.");
        DRAGON_BOMB_COOLDOWN = getInt("cooldown", "DRAGON_BOMB_COOLDOWN", DRAGON_BOMB_COOLDOWN,
                "Dragon-bomb cooldown in ticks.");
        DRAGON_BREATH_COOLDOWN = getInt("cooldown", "DRAGON_BREATH_COOLDOWN", DRAGON_BREATH_COOLDOWN,
                "Dragon-breath cooldown in ticks.");
        EXPLOSION_COOLDOWN = getInt("cooldown", "EXPLOSION_COOLDOWN", EXPLOSION_COOLDOWN,
                "Creepy explosion cooldown in ticks.");
        FORCEFUL_SPIT_COOLDOWN = getInt("cooldown", "FORCEFUL_SPIT_COOLDOWN", FORCEFUL_SPIT_COOLDOWN,
                "Forceful-spit cooldown in ticks.");
        GHASTLY_COOLDOWN = getInt("cooldown", "GHASTLY_COOLDOWN", GHASTLY_COOLDOWN,
                "Ghast fireball cooldown in ticks.");
        IRON_REPAIR_COOLDOWN = getInt("cooldown", "IRON_REPAIR_COOLDOWN", IRON_REPAIR_COOLDOWN,
                "Iron-repair cooldown in ticks.");
        PYROMANCY_COOLDOWN = getInt("cooldown", "PYROMANCY_COOLDOWN", PYROMANCY_COOLDOWN,
                "Pyromancy cooldown in ticks.");
        SHULKER_BULLET_COOLDOWN = getInt("cooldown", "SHULKER_BULLET_COOLDOWN", SHULKER_BULLET_COOLDOWN,
                "Shulker-bullet cooldown in ticks.");
        SILK_COOLDOWN = getInt("cooldown", "SILK_COOLDOWN", SILK_COOLDOWN,
                "Silk-production cooldown in ticks.");
        VENOM_COOLDOWN = getInt("cooldown", "VENOM_COOLDOWN", VENOM_COOLDOWN,
                "Venom-on-hit cooldown in ticks.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static String getDefaultChestCavityId() {
        return DEFAULT_CHEST_CAVITY;
    }

    private static int getInt(String category, String name, int defaultValue, String comment) {
        return configuration.getInt(name, category, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment);
    }

    private static float getFloat(String category, String name, float defaultValue, String comment) {
        return configuration.getFloat(name, category, defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE, comment);
    }
}
