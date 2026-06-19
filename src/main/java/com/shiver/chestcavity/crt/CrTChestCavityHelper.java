package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.api.ChestCavityView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntity;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.chestcavity.ChestCavityHelper")
public final class CrTChestCavityHelper {

    private CrTChestCavityHelper() {
    }

    @ZenMethod
    public static CrTChestCavity get(IEntity entity) {
        ChestCavityView view = ChestCavityApis.CHEST_CAVITIES.get(CrTUtil.internalEntity(entity));
        return view == null ? null : new CrTChestCavity(view);
    }
}
