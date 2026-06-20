package com.shiver.chestcavity.ui;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class ChestCavityUiBridge {

    public static final String FACTORY_ID = "chestcavity:chest_cavity";
    public static final String PANEL_ID = "chest_cavity";
    public static final int CHEST_CAVITY_SLOTS = 27;
    public static final int SLOTS_PER_ROW = 9;
    public static final double MAX_INTERACT_DISTANCE_SQ = 64.0D;

    private static SlotPolicy slotPolicy = SlotPolicy.ALLOW_ALL;

    private ChestCavityUiBridge() {
    }

    public static void setSlotPolicy(SlotPolicy policy) {
        slotPolicy = policy == null ? SlotPolicy.ALLOW_ALL : policy;
    }

    public static SlotPolicy getSlotPolicy() {
        return slotPolicy;
    }

    public static boolean canKeepOpen(EntityPlayer player, ChestCavityGuiData data) {
        EntityLivingBase target = data.getTarget();
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(target);
        return player != null
                && target != null
                && target.isEntityAlive()
                && player.getDistanceSq(target) <= MAX_INTERACT_DISTANCE_SQ
                && ChestCavityHelper.hasAssignedChestCavityType(chestCavity);
    }

    public static boolean open(EntityPlayerMP player, EntityLivingBase target) {
        if (player == null || target == null || player.world.isRemote) {
            return false;
        }

        ChestCavityGuiData data = new ChestCavityGuiData(player, target.getEntityId());
        if (!canKeepOpen(player, data)) {
            return false;
        }

        ChestCavityGuiFactory.open(player, data);
        return true;
    }

    public interface SlotPolicy {
        SlotPolicy ALLOW_ALL = new SlotPolicy() {
            @Override
            public boolean isForbidden(int slot) {
                return false;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true;
            }
        };

        boolean isForbidden(int slot);

        boolean isItemValid(int slot, ItemStack stack);
    }
}
