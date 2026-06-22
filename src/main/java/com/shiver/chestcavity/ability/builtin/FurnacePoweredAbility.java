package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumHand;

/**
 * 通过消耗手持燃料给玩家叠加炉火层数的主动能力。
 */
final class FurnacePoweredAbility implements ActiveOrganAbility {

    static final FurnacePoweredAbility INSTANCE = new FurnacePoweredAbility();

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private FurnacePoweredAbility() {
    }

    /**
     * 消耗玩家手中的燃料，为炉火能力添加一层燃料。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float furnacePoweredScore = chestCavity.getOrganScore(CCOrganScores.FURNACE_POWERED);
        if (furnacePoweredScore <= 0.0F) {
            return false;
        }
        int furnacePowered = Math.max(1, Math.round(furnacePoweredScore));

        if (FurnacePower.getActiveLayerCount(player) >= furnacePowered) {
            return false;
        }

        FuelStack fuel = findFuel(player);
        if (fuel == null) {
            return false;
        }

        if (!FurnacePower.addFuelLayer(player, fuel.burnTime, furnacePowered)) {
            return false;
        }
        consumeFuel(player, fuel);
        return true;
    }

    /**
     * 优先从主手，其次从副手查找可用燃料。
     *
     * @param player 发动能力的玩家。
     * @return 可用燃料信息；如果没有则返回 `null`。
     */
    private static FuelStack findFuel(EntityPlayerMP player) {
        FuelStack mainHand = findFuel(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findFuel(player, EnumHand.OFF_HAND) : mainHand;
    }

    /**
     * 从指定手上查找可充当炉火燃料的物品。
     *
     * @param player 发动能力的玩家。
     * @param hand 要检查的手。
     * @return 可用燃料信息；如果没有则返回 `null`。
     */
    private static FuelStack findFuel(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }
        int burnTime = TileEntityFurnace.getItemBurnTime(stack);
        return burnTime > 0 ? new FuelStack(hand, stack, burnTime) : null;
    }

    /**
     * 从玩家手中真正扣除一份燃料物品。
     *
     * @param player 发动能力的玩家。
     * @param fuel 已确认可用的燃料信息。
     */
    private static void consumeFuel(EntityPlayerMP player, FuelStack fuel) {
        ItemStack stack = fuel.stack;
        if (stack.getCount() == 1 && stack.getItem().hasContainerItem(stack)) {
            player.setHeldItem(fuel.hand, stack.getItem().getContainerItem(stack));
            return;
        }
        stack.shrink(1);
    }

    /**
     * 记录一次燃料消耗所需的上下文信息。
     */
    private static final class FuelStack {
        private final EnumHand hand;
        private final ItemStack stack;
        private final int burnTime;

        /**
         * 创建一份燃料上下文。
         *
         * @param hand 燃料所在的手。
         * @param stack 燃料物品堆。
         * @param burnTime 燃料燃烧时间。
         */
        private FuelStack(EnumHand hand, ItemStack stack, int burnTime) {
            this.hand = hand;
            this.stack = stack;
            this.burnTime = burnTime;
        }
    }
}
