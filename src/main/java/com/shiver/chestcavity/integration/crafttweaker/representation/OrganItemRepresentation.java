package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.integration.crafttweaker.callback.OrganCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtUtil;
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

@ZenClass(CtConstants.CT_NAMESPACE + "contenttweaker.OrganItemRepresentation")
@ZenRegister
public class OrganItemRepresentation extends ItemRepresentation {

    @ZenProperty
    public boolean pseudoOrgan;

    @ZenProperty
    public OrganCallbacks.CanInsert canInsert;

    @ZenProperty
    public OrganCallbacks.CanRemove canRemove;

    @ZenProperty
    public OrganCallbacks.OnInserted onInserted;

    @ZenProperty
    public OrganCallbacks.OnRemoved onRemoved;

    @ZenProperty
    public OrganCallbacks.OnTick onTick;

    private final Map<ResourceLocation, Float> organScores = new LinkedHashMap<ResourceLocation, Float>();

    public OrganItemRepresentation(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }

    @ZenMethod
    public void addScore(String id, float value) {
        organScores.put(CtUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void bindAbility(String id, float value) {
        organScores.put(CtUtil.requireId(id, "ability"), value);
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
