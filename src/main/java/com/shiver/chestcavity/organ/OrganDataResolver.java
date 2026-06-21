package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import net.minecraft.item.ItemStack;

final class OrganDataResolver {

    private OrganDataResolver() {
    }

    static OrganData resolve(ChestCavityType type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        OrganData data = type == null ? null : type.catchExceptionalOrgan(stack);
        if (data == null) {
            data = OrganData.fromStack(stack);
        }
        return data;
    }
}
