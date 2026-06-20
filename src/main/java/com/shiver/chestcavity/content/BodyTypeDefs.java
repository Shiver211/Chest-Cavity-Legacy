package com.shiver.chestcavity.content;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BodyTypeDefs {

    private BodyTypeDefs() {
    }

    public static BodyTypeDef createFallback(String id) {
        BodyTypeDef def = new BodyTypeDef(id == null || id.isEmpty() ? "fallback" : id);
        ChestCavityInventory inventory = new ChestCavityInventory();
        for (int i = 0; i < inventory.size(); i++) {
            inventory.setStack(i, new ItemStack(Blocks.DIRT, 64));
        }
        def.setDefaultChestCavity(inventory);
        def.setForbiddenSlots(allSlots(inventory.size()));
        return def;
    }

    private static List<Integer> allSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            slots.add(i);
        }
        return slots;
    }
}
