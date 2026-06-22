package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 当正常胸腔类型无法生成时使用的保底胸腔类型。
 */
public class FallbackChestCavityType extends GeneratedChestCavityType {

    /**
     * 创建一个所有槽位都被泥土填满的保底胸腔类型。
     */
    public FallbackChestCavityType() {
        ChestCavityInventory inventory = new ChestCavityInventory();
        for (int i = 0; i < inventory.size(); i++) {
            inventory.setStack(i, new ItemStack(Blocks.DIRT, 64));
        }
        setDefaultChestCavity(inventory);
        setForbiddenSlots(allSlots(inventory.size()));
    }

    /**
     * 生成包含全部槽位索引的列表。
     *
     * @param size 槽位总数。
     * @return 全部槽位索引列表。
     */
    private static List<Integer> allSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            slots.add(i);
        }
        return slots;
    }
}
