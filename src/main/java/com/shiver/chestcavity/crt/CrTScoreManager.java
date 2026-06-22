package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 向 ZenScript 暴露分数显示名注册接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.ScoreManager")
public final class CrTScoreManager {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTScoreManager() {
    }

    /**
     * 为一个分数标识注册显示名称。
     *
     * @param scoreId 分数标识。
     * @param displayName 显示名称。
     */
    @ZenMethod
    public static void addScore(String scoreId, String displayName) {
        ChestCavityApis.SCORES.addScore(scoreId, displayName);
    }
}
