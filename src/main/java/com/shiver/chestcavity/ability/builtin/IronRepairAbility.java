package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

/**
 * 消耗铁质材料为玩家恢复生命的主动能力。
 */
final class IronRepairAbility implements ActiveOrganAbility {

    static final IronRepairAbility INSTANCE = new IronRepairAbility();

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private IronRepairAbility() {
    }

    /**
     * 使用手持铁质材料恢复生命，并施加修复冷却。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float ironRepair = chestCavity.getOrganScore(CCOrganScores.IRON_REPAIR)
                - ChestCavityHelper.getChestCavityType(chestCavity).getDefaultOrganScore(CCOrganScores.IRON_REPAIR);
        if (ironRepair <= 0.0F
                || player.isPotionActive(CCPotions.IRON_REPAIR_COOLDOWN)
                || player.getHealth() >= player.getMaxHealth()) {
            return false;
        }

        ItemStack material = findIronRepairMaterial(player);
        if (material == null) {
            return false;
        }

        float healAmount = player.getMaxHealth() * CCConfig.IRON_REPAIR_PERCENT * getIronRepairMaterialValue(material);
        player.heal(healAmount);
        player.addPotionEffect(new PotionEffect(CCPotions.IRON_REPAIR_COOLDOWN,
                Math.max(1, Math.round(CCConfig.IRON_REPAIR_COOLDOWN / ironRepair)), 0, false, false));
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.75F, 1.0F);
        material.shrink(1);
        return true;
    }

    /**
     * 优先从主手，其次从副手查找可用修复材料。
     *
     * @param player 发动能力的玩家。
     * @return 可用材料；如果没有则返回 `null`。
     */
    private static ItemStack findIronRepairMaterial(EntityPlayerMP player) {
        ItemStack mainHand = findIronRepairMaterial(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findIronRepairMaterial(player, EnumHand.OFF_HAND) : mainHand;
    }

    /**
     * 从指定手上查找可用于铁修复的材料。
     *
     * @param player 发动能力的玩家。
     * @param hand 要检查的手。
     * @return 可用材料；如果没有则返回 `null`。
     */
    private static ItemStack findIronRepairMaterial(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }

        return isIronRepairMaterial(stack) ? stack : null;
    }

    /**
     * 判断一个物品是否可作为铁修复材料。
     *
     * @param stack 要检查的物品堆。
     * @return `true` 表示该物品可用于修复。
     */
    private static boolean isIronRepairMaterial(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.IRON_NUGGET || item == Items.IRON_INGOT || item == Item.getItemFromBlock(Blocks.IRON_BLOCK);
    }

    /**
     * 返回一种铁修复材料对应的治疗倍率。
     *
     * @param stack 要检查的物品堆。
     * @return 材料价值倍率。
     */
    private static float getIronRepairMaterialValue(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.IRON_NUGGET) {
            return 1.0F / 9.0F;
        }
        if (item == Item.getItemFromBlock(Blocks.IRON_BLOCK)) {
            return 9.0F;
        }
        return 1.0F;
    }
}
