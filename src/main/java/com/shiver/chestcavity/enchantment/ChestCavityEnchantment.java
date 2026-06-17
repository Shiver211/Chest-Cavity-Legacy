package com.shiver.chestcavity.enchantment;

import com.shiver.chestcavity.registry.CCEnchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ChestCavityEnchantment extends Enchantment {

    private final int minEnchantability;
    private final int maxEnchantability;
    private final int maxLevel;
    private final boolean treasure;
    private final boolean curse;

    public ChestCavityEnchantment(Rarity rarity, EnumEnchantmentType type, EntityEquipmentSlot[] slots, int minEnchantability, int maxEnchantability, int maxLevel, boolean treasure, boolean curse) {
        super(rarity, type, slots);
        this.minEnchantability = minEnchantability;
        this.maxEnchantability = maxEnchantability;
        this.maxLevel = maxLevel;
        this.treasure = treasure;
        this.curse = curse;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return minEnchantability * enchantmentLevel;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return maxEnchantability * enchantmentLevel;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return treasure;
    }

    @Override
    public boolean isCurse() {
        return curse;
    }

    @Override
    public boolean canApplyTogether(Enchantment enchantment) {
        if (!super.canApplyTogether(enchantment)) {
            return false;
        }
        if (this == CCEnchantments.SURGICAL) {
            return enchantment != CCEnchantments.TOMOPHOBIA;
        }
        if (this == CCEnchantments.TOMOPHOBIA) {
            return enchantment != CCEnchantments.SURGICAL && enchantment != CCEnchantments.MALPRACTICE;
        }
        if (this == CCEnchantments.MALPRACTICE) {
            return enchantment != CCEnchantments.TOMOPHOBIA;
        }
        return true;
    }
}
