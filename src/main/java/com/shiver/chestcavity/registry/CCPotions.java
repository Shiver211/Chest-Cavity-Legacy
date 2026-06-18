package com.shiver.chestcavity.registry;

import java.util.ArrayList;
import java.util.List;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.potion.CCPotion;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.potion.OrganRejection;
import com.shiver.chestcavity.potion.Ruminating;
import com.shiver.chestcavity.potion.WaterVulnerability;

import net.minecraft.potion.Potion;
import net.minecraftforge.registries.IForgeRegistry;

public final class CCPotions {

    private static final List<Potion> POTIONS = new ArrayList<Potion>();

    public static final Potion ORGAN_REJECTION = register("organ_rejection", new OrganRejection());
    public static final Potion FURNACE_POWER = register("furnace_power", new FurnacePower());
    public static final Potion RUMINATING = register("ruminating", new Ruminating());
    public static final Potion ARROW_DODGE_COOLDOWN = register("arrow_dodge_cooldown", new CCPotion(false, 0x000000));
    public static final Potion DRAGON_BOMB_COOLDOWN = register("dragon_bomb_cooldown", new CCPotion(false, 0x000000));
    public static final Potion DRAGON_BREATH_COOLDOWN = register("dragon_breath_cooldown", new CCPotion(false, 0x000000));
    public static final Potion EXPLOSION_COOLDOWN = register("explosion_cooldown", new CCPotion(false, 0x000000));
    public static final Potion FORCEFUL_SPIT_COOLDOWN = register("forceful_spit_cooldown", new CCPotion(false, 0x000000));
    public static final Potion GHASTLY_COOLDOWN = register("ghastly_cooldown", new CCPotion(false, 0x000000));
    public static final Potion IRON_REPAIR_COOLDOWN = register("iron_repair_cooldown", new CCPotion(false, 0x000000));
    public static final Potion PYROMANCY_COOLDOWN = register("pyromancy_cooldown", new CCPotion(false, 0x000000));
    public static final Potion SHULKER_BULLET_COOLDOWN = register("shulker_bullet_cooldown", new CCPotion(false, 0x000000));
    public static final Potion SILK_COOLDOWN = register("silk_cooldown", new CCPotion(false, 0x000000));
    public static final Potion VENOM_COOLDOWN = register("venom_cooldown", new CCPotion(false, 0x000000));
    public static final Potion WATER_VULNERABILITY = register("water_vulnerability", new WaterVulnerability());

    private CCPotions() {
    }

    public static void register(IForgeRegistry<Potion> registry) {
        registry.registerAll(POTIONS.toArray(new Potion[POTIONS.size()]));
    }

    private static Potion register(String name, Potion potion) {
        potion.setRegistryName(Tags.MOD_ID, name);
        potion.setPotionName("effect." + Tags.MOD_ID + "." + name);
        POTIONS.add(potion);
        return potion;
    }
}
