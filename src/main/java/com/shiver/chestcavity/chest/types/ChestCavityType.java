package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public interface ChestCavityType {

    Map<String, Float> getDefaultOrganScores();

    float getDefaultOrganScore(String id);

    ChestCavityInventory getDefaultChestCavity();

    boolean isSlotForbidden(int index);

    void fillChestCavityInventory(ChestCavityInventory chestCavity);

    void loadBaseOrganScores(Map<String, Float> organScores);

    OrganData catchExceptionalOrgan(ItemStack stack);

    List<ItemStack> getDroppableOrgans();

    boolean isBossChestCavity();

    default int getHeartBleedCap() {
        return isBossChestCavity() ? 5 : Integer.MAX_VALUE;
    }

    boolean isPlayerChestCavity();

    float getDropRateMultiplier();
}
