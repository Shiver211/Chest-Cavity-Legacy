package com.shiver.chestcavity.enchantment;

import com.shiver.chestcavity.registry.CCEnchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * 胸腔模组使用的基础附魔实现，可按构造参数配置等级与限制。
 */
public class ChestCavityEnchantment extends Enchantment {

    private final int minEnchantability;
    private final int maxEnchantability;
    private final int maxLevel;
    private final boolean treasure;
    private final boolean curse;
    private final boolean allowedOnBooks;

    /**
     * 创建一个允许附魔书出现的胸腔附魔。
     *
     * @param rarity 稀有度。
     * @param type 附魔适用类型。
     * @param slots 可装备槽位。
     * @param minEnchantability 最小附魔能力系数。
     * @param maxEnchantability 最大附魔能力系数。
     * @param maxLevel 最大等级。
     * @param treasure 是否为宝藏附魔。
     * @param curse 是否为诅咒附魔。
     */
    public ChestCavityEnchantment(Rarity rarity, EnumEnchantmentType type, EntityEquipmentSlot[] slots, int minEnchantability, int maxEnchantability, int maxLevel, boolean treasure, boolean curse) {
        this(rarity, type, slots, minEnchantability, maxEnchantability, maxLevel, treasure, curse, true);
    }

    /**
     * 创建一个胸腔附魔，并完整指定其全部限制属性。
     *
     * @param rarity 稀有度。
     * @param type 附魔适用类型。
     * @param slots 可装备槽位。
     * @param minEnchantability 最小附魔能力系数。
     * @param maxEnchantability 最大附魔能力系数。
     * @param maxLevel 最大等级。
     * @param treasure 是否为宝藏附魔。
     * @param curse 是否为诅咒附魔。
     * @param allowedOnBooks 是否允许出现在附魔书上。
     */
    public ChestCavityEnchantment(Rarity rarity, EnumEnchantmentType type, EntityEquipmentSlot[] slots, int minEnchantability, int maxEnchantability, int maxLevel, boolean treasure, boolean curse, boolean allowedOnBooks) {
        super(rarity, type, slots);
        this.minEnchantability = minEnchantability;
        this.maxEnchantability = maxEnchantability;
        this.maxLevel = maxLevel;
        this.treasure = treasure;
        this.curse = curse;
        this.allowedOnBooks = allowedOnBooks;
    }

    /**
     * 返回指定等级对应的最小附魔能力需求。
     *
     * @param enchantmentLevel 附魔等级。
     * @return 最小附魔能力。
     */
    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return minEnchantability * enchantmentLevel;
    }

    /**
     * 返回指定等级对应的最大附魔能力需求。
     *
     * @param enchantmentLevel 附魔等级。
     * @return 最大附魔能力。
     */
    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return maxEnchantability * enchantmentLevel;
    }

    /**
     * 返回该附魔允许的最大等级。
     *
     * @return 最大等级。
     */
    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * 判断该附魔是否属于宝藏附魔。
     *
     * @return `true` 表示为宝藏附魔。
     */
    @Override
    public boolean isTreasureEnchantment() {
        return treasure;
    }

    /**
     * 判断该附魔是否属于诅咒。
     *
     * @return `true` 表示为诅咒附魔。
     */
    @Override
    public boolean isCurse() {
        return curse;
    }

    /**
     * 判断该附魔是否允许出现在附魔书上。
     *
     * @return `true` 表示允许出现在附魔书上。
     */
    @Override
    public boolean isAllowedOnBooks() {
        return allowedOnBooks;
    }

    /**
     * 判断该附魔能否与另一附魔同时存在。
     *
     * @param enchantment 另一附魔。
     * @return `true` 表示二者可共存。
     */
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
