package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import net.minecraft.item.ItemStack;

/**
 * 负责按胸腔类型和物品内容解析器官数据。
 */
final class OrganDataResolver {

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganDataResolver() {
    }

    /**
     * 优先按胸腔类型中的特殊规则，再按物品自身 NBT 解析器官数据。
     *
     * @param type 当前胸腔类型。
     * @param stack 要解析的物品堆。
     * @return 解析出的器官数据；如果无法解析则返回 `null`。
     */
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
