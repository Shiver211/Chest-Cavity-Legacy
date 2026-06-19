package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.chestcavity.ScoreManager")
public final class CrTScoreManager {

    private CrTScoreManager() {
    }

    @ZenMethod
    public static void addScore(String scoreId, String displayName) {
        ChestCavityApis.SCORES.addScore(scoreId, displayName);
    }
}
