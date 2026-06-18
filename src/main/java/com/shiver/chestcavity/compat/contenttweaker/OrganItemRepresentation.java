package com.shiver.chestcavity.compat.contenttweaker;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.compat.crafttweaker.ChestCavityCtConstants;
import com.shiver.chestcavity.compat.crafttweaker.CtCompatUtil;
import com.teamacronymcoders.base.registrysystem.ItemRegistry;
import com.teamacronymcoders.contenttweaker.ContentTweaker;
import com.teamacronymcoders.contenttweaker.modules.vanilla.items.ItemRepresentation;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "contenttweaker.OrganItemRepresentation")
@ZenRegister
public class OrganItemRepresentation extends ItemRepresentation {

    @ZenProperty
    public boolean pseudoOrgan;

    private final Map<ResourceLocation, Float> organScores = new LinkedHashMap<>();

    public OrganItemRepresentation(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }

    @ZenMethod
    public void addScore(String id, float value) {
        organScores.put(CtCompatUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void bindAbility(String id, float value) {
        organScores.put(CtCompatUtil.requireId(id, "ability"), value);
    }

    @ZenMethod
    @Override
    public void register() {
        ContentTweaker.instance.getRegistry(ItemRegistry.class, "ITEM").register(new OrganItemContent(this));
    }

    public OrganData buildOrganData() {
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudoOrgan);
        data.setOrganScores(organScores);
        return data;
    }
}
