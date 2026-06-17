package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FallbackChestCavityType extends GeneratedChestCavityType {

    public FallbackChestCavityType() {
        ChestCavityInventory inventory = new ChestCavityInventory();
        for (int i = 0; i < inventory.size(); i++) {
            inventory.setStack(i, new ItemStack(Blocks.DIRT, 64));
        }
        setDefaultChestCavity(inventory);
        setForbiddenSlots(allSlots(inventory.size()));
    }

    private static List<Integer> allSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            slots.add(i);
        }
        return slots;
    }
}
