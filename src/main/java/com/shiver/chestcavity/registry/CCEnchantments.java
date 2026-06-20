package com.shiver.chestcavity.registry;

import java.util.ArrayList;
import java.util.List;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.enchantment.ChestCavityEnchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.registries.IForgeRegistry;

public final class CCEnchantments {

    private static final List<Enchantment> ENCHANTMENTS = new ArrayList<>();
    private static final EntityEquipmentSlot[] MAIN_HAND = new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND};

    public static final Enchantment O_NEGATIVE = register("o_negative", new ChestCavityEnchantment(Enchantment.Rarity.RARE, EnumEnchantmentType.ALL, EntityEquipmentSlot.values(), 50, 100, 2, true, false, false));
    public static final Enchantment SURGICAL = register("surgical", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 15, 65, 3, false, false));
    public static final Enchantment MALPRACTICE = register("malpractice", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 25, 50, 1, true, true));
    public static final Enchantment TOMOPHOBIA = register("tomophobia", new ChestCavityEnchantment(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, MAIN_HAND, 25, 50, 1, true, false));

    private CCEnchantments() {
    }

    public static void register(IForgeRegistry<Enchantment> registry) {
        registry.registerAll(ENCHANTMENTS.toArray(new Enchantment[0]));
    }

    private static Enchantment register(String name, Enchantment enchantment) {
        enchantment.setRegistryName(Tags.MOD_ID, name);
        enchantment.setName(Tags.MOD_ID + "." + name);
        ENCHANTMENTS.add(enchantment);
        return enchantment;
    }
}
