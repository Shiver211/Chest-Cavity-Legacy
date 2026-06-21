package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumHand;

final class FurnacePoweredAbility implements ActiveOrganAbility {

    static final FurnacePoweredAbility INSTANCE = new FurnacePoweredAbility();

    private FurnacePoweredAbility() {
    }

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

    private static FuelStack findFuel(EntityPlayerMP player) {
        FuelStack mainHand = findFuel(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findFuel(player, EnumHand.OFF_HAND) : mainHand;
    }

    private static FuelStack findFuel(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }
        int burnTime = TileEntityFurnace.getItemBurnTime(stack);
        return burnTime > 0 ? new FuelStack(hand, stack, burnTime) : null;
    }

    private static void consumeFuel(EntityPlayerMP player, FuelStack fuel) {
        ItemStack stack = fuel.stack;
        if (stack.getCount() == 1 && stack.getItem().hasContainerItem(stack)) {
            player.setHeldItem(fuel.hand, stack.getItem().getContainerItem(stack));
            return;
        }
        stack.shrink(1);
    }

    private static final class FuelStack {
        private final EnumHand hand;
        private final ItemStack stack;
        private final int burnTime;

        private FuelStack(EnumHand hand, ItemStack stack, int burnTime) {
            this.hand = hand;
            this.stack = stack;
            this.burnTime = burnTime;
        }
    }
}
