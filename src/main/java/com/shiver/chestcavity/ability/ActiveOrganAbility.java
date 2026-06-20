package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.capability.ChestCavityData;
import net.minecraft.entity.player.EntityPlayerMP;

public interface ActiveOrganAbility {

    boolean activate(EntityPlayerMP player, ChestCavityData chestCavity);
}
