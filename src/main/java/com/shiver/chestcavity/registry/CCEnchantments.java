package com.shiver.chestcavity.registry;

import java.util.ArrayList;
import java.util.List;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.enchantment.ChestCavityEnchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 集中定义并注册模组使用的附魔。
 */
public final class CCEnchantments {

    private static final List<Enchantment> ENCHANTMENTS = new ArrayList<Enchantment>();
    private static final EntityEquipmentSlot[] MAIN_HAND = new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND};

    public static final Enchantment O_NEGATIVE = register("o_negative", new ChestCavityEnchantment(Enchantment.Rarity.RARE, EnumEnchantmentType.ALL, EntityEquipmentSlot.values(), 50, 100, 2, true, false, false));
    public static final Enchantment SURGICAL = register("surgical", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 15, 65, 3, false, false));
    public static final Enchantment MALPRACTICE = register("malpractice", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 25, 50, 1, true, true));
    public static final Enchantment TOMOPHOBIA = register("tomophobia", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 25, 50, 1, true, false));

    /**
     * 工具类，不允许外部实例化。
     */
    private CCEnchantments() {
    }

    /**
     * 把全部附魔注册到 Forge 注册表中。
     *
     * @param registry 附魔注册表。
     */
    public static void register(IForgeRegistry<Enchantment> registry) {
        registry.registerAll(ENCHANTMENTS.toArray(new Enchantment[ENCHANTMENTS.size()]));
    }

    /**
     * 设置附魔的注册信息并加入待注册列表。
     *
     * @param name 注册名。
     * @param enchantment 附魔对象。
     * @return 原附魔对象，便于常量初始化。
     */
    private static Enchantment register(String name, Enchantment enchantment) {
        enchantment.setRegistryName(Tags.MOD_ID, name);
        enchantment.setName(Tags.MOD_ID + "." + name);
        ENCHANTMENTS.add(enchantment);
        return enchantment;
    }
}
