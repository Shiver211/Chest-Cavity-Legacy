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

/**
 * 集中定义并注册模组中的全部药水效果。
 */
public final class CCPotions {

    private static final List<Potion> POTIONS = new ArrayList<Potion>();

    public static final Potion ORGAN_REJECTION = register("organ_rejection", new OrganRejection());
    public static final Potion FURNACE_POWER = register("furnace_power", new FurnacePower());
    public static final Potion RUMINATING = register("ruminating", new Ruminating());
    public static final Potion ARROW_DODGE_COOLDOWN = register("arrow_dodge_cooldown", new CCPotion(false, 0x000000));
    public static final Potion DRAGON_BOMB_COOLDOWN = register("dragon_bomb_cooldown", new CCPotion(false, 0x000000));
    public static final Potion EXPLOSION_COOLDOWN = register("explosion_cooldown", new CCPotion(false, 0x000000));
    public static final Potion FORCEFUL_SPIT_COOLDOWN = register("forceful_spit_cooldown", new CCPotion(false, 0x000000));
    public static final Potion GHASTLY_COOLDOWN = register("ghastly_cooldown", new CCPotion(false, 0x000000));
    public static final Potion IRON_REPAIR_COOLDOWN = register("iron_repair_cooldown", new CCPotion(false, 0x000000));
    public static final Potion PYROMANCY_COOLDOWN = register("pyromancy_cooldown", new CCPotion(false, 0x000000));
    public static final Potion SHULKER_BULLET_COOLDOWN = register("shulker_bullet_cooldown", new CCPotion(false, 0x000000));
    public static final Potion SILK_COOLDOWN = register("silk_cooldown", new CCPotion(false, 0x000000));
    public static final Potion VENOM_COOLDOWN = register("venom_cooldown", new CCPotion(false, 0x000000));
    public static final Potion WATER_VULNERABILITY = register("water_vulnerability", new WaterVulnerability());

    /**
     * 工具类，不允许外部实例化。
     */
    private CCPotions() {
    }

    /**
     * 把全部药水效果注册到 Forge 注册表中。
     *
     * @param registry 药水注册表。
     */
    public static void register(IForgeRegistry<Potion> registry) {
        registry.registerAll(POTIONS.toArray(new Potion[POTIONS.size()]));
    }

    /**
     * 设置药水的注册信息并加入待注册列表。
     *
     * @param name 注册名。
     * @param potion 药水对象。
     * @return 原药水对象，便于常量初始化。
     */
    private static Potion register(String name, Potion potion) {
        potion.setRegistryName(Tags.MOD_ID, name);
        potion.setPotionName("effect." + Tags.MOD_ID + "." + name);
        POTIONS.add(potion);
        return potion;
    }
}
