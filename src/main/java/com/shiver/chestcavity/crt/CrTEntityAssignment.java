package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 向 ZenScript 暴露实体到胸腔类型的分配接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.EntityAssignment")
public final class CrTEntityAssignment {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTEntityAssignment() {
    }

    /**
     * 为实体注册一个胸腔类型分配。
     *
     * @param entityId 实体注册名。
     * @param typeId 胸腔类型标识。
     */
    @ZenMethod
    public static void register(String entityId, String typeId) {
        ChestCavityApis.ENTITY_ASSIGNMENTS.register(CrTUtil.id(entityId), typeId);
    }

    /**
     * 注销实体的胸腔类型分配。
     *
     * @param entityId 实体注册名。
     */
    @ZenMethod
    public static void unregister(String entityId) {
        ChestCavityApis.ENTITY_ASSIGNMENTS.unregister(CrTUtil.id(entityId));
    }

    /**
     * 查询实体当前被分配到的胸腔类型。
     *
     * @param entityId 实体注册名。
     * @return 胸腔类型标识。
     */
    @ZenMethod
    public static String getAssignedType(String entityId) {
        return ChestCavityApis.ENTITY_ASSIGNMENTS.getAssignedType(CrTUtil.id(entityId));
    }
}
