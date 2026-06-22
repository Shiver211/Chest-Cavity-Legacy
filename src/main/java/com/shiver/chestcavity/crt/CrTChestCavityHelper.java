package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.api.ChestCavityView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntity;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 向 ZenScript 暴露从实体读取胸腔数据的辅助方法。
 */
@ZenRegister
@ZenClass("mods.chestcavity.ChestCavityHelper")
public final class CrTChestCavityHelper {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTChestCavityHelper() {
    }

    /**
     * 从实体获取胸腔脚本视图。
     *
     * @param entity 目标实体。
     * @return 胸腔脚本视图；如果实体不支持则返回 `null`。
     */
    @ZenMethod
    public static CrTChestCavity get(IEntity entity) {
        ChestCavityView view = ChestCavityApis.CHEST_CAVITIES.get(CrTUtil.internalEntity(entity));
        return view == null ? null : new CrTChestCavity(view);
    }
}
