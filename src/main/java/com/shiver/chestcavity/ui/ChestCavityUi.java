package com.shiver.chestcavity.ui;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public final class ChestCavityUi {

    public static final String FACTORY_ID = "chestcavity:chest_cavity";
    public static final String PANEL_ID = "chest_cavity";
    public static final double MAX_INTERACT_DISTANCE_SQ = 64.0D;

    private ChestCavityUi() {
    }

    public static ChestLayoutDef getLayout(ChestCavityData chestCavity) {
        return ChestCavityTypeUtil.getChestLayout(chestCavity);
    }

    public static boolean canKeepOpen(EntityPlayer player, ChestCavityGuiData data) {
        EntityLivingBase target = data.getTarget();
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(target);
        return player != null
                && target != null
                && target.isEntityAlive()
                && player.getDistanceSq(target) <= MAX_INTERACT_DISTANCE_SQ
                && ChestCavityTypeUtil.hasAssignedChestCavityType(chestCavity);
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
}
