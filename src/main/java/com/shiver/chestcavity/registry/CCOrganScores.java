package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;
import net.minecraft.util.ResourceLocation;

public final class CCOrganScores {

    public static final ResourceLocation HEALTH = id("health");
    public static final ResourceLocation LUCK = id("luck");
    public static final ResourceLocation STRENGTH = id("strength");
    public static final ResourceLocation SPEED = id("speed");
    public static final ResourceLocation DEFENSE = id("defense");
    public static final ResourceLocation NERVES = id("nerves");
    public static final ResourceLocation MINING_SPEED = id("mining_speed");
    public static final ResourceLocation EASE_OF_ACCESS = id("ease_of_access");
    public static final ResourceLocation INCOMPATIBILITY = id("incompatibility");
    public static final ResourceLocation DIGESTION = id("digestion");
    public static final ResourceLocation NUTRITION = id("nutrition");
    public static final ResourceLocation FILTRATION = id("filtration");
    public static final ResourceLocation DETOXIFICATION = id("detoxification");
    public static final ResourceLocation METABOLISM = id("metabolism");
    public static final ResourceLocation ENDURANCE = id("endurance");
    public static final ResourceLocation ROT_DIGESTION = id("rot_digestion");
    public static final ResourceLocation ROTGUT = id("rotgut");
    public static final ResourceLocation CARNIVOROUS_DIGESTION = id("carnivorous_digestion");
    public static final ResourceLocation CARNIVOROUS_NUTRITION = id("carnivorous_nutrition");
    public static final ResourceLocation HERBIVOROUS_DIGESTION = id("herbivorous_digestion");
    public static final ResourceLocation HERBIVOROUS_NUTRITION = id("herbivorous_nutrition");
    public static final ResourceLocation FURNACE_POWERED = id("furnace_powered");
    public static final ResourceLocation GRAZING = id("grazing");
    public static final ResourceLocation IRON_REPAIR = id("iron_repair");
    public static final ResourceLocation BREATH = id("breath");
    public static final ResourceLocation BREATH_CAPACITY = id("breath_capacity");
    public static final ResourceLocation WATER_BREATH = id("water_breath");
    public static final ResourceLocation BUOYANT = id("buoyant");
    public static final ResourceLocation CREEPY = id("creepy");
    public static final ResourceLocation EXPLOSIVE = id("explosive");
    public static final ResourceLocation FIRE_RESISTANT = id("fire_resistant");
    public static final ResourceLocation HYDROALLERGENIC = id("hydroallergenic");
    public static final ResourceLocation HYDROPHOBIA = id("hydrophobia");
    public static final ResourceLocation IMPACT_RESISTANT = id("impact_resistant");
    public static final ResourceLocation KNOCKBACK_RESISTANT = id("knockback_resistant");
    public static final ResourceLocation LEAPING = id("leaping");
    public static final ResourceLocation LIGHTWEIGHT = id("lightweight");
    public static final ResourceLocation SWIM_SPEED = id("swim_speed");
    public static final ResourceLocation GLOWING = id("glowing");
    public static final ResourceLocation PYROMANCY = id("pyromancy");
    public static final ResourceLocation LAUNCHING = id("launching");
    public static final ResourceLocation VENOMOUS = id("venomous");
    public static final ResourceLocation ARROW_DODGING = id("arrow_dodging");
    public static final ResourceLocation BUFF_PURGING = id("buff_purging");
    public static final ResourceLocation WITHERED = id("withered");
    public static final ResourceLocation DRAGON_BOMBS = id("dragon_bombs");
    public static final ResourceLocation FORCEFUL_SPIT = id("forceful_spit");
    public static final ResourceLocation GHASTLY = id("ghastly");
    public static final ResourceLocation SHULKER_BULLETS = id("shulker_bullets");
    public static final ResourceLocation SILK = id("silk");
    public static final ResourceLocation CRYSTALSYNTHESIS = id("crystalsynthesis");
    public static final ResourceLocation PHOTOSYNTHESIS = id("photosynthesis");
    public static final ResourceLocation DESTRUCTIVE_COLLISIONS = id("destructive_collisions");

    private CCOrganScores() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
