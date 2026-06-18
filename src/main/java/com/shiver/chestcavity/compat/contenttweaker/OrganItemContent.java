package com.shiver.chestcavity.compat.contenttweaker;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.script.model.ScriptOrganDefinition;
import com.shiver.chestcavity.script.registry.ScriptOrganRegistry;
import com.teamacronymcoders.contenttweaker.modules.vanilla.items.ItemContent;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

import com.shiver.chestcavity.compat.crafttweaker.ChestCavityCtConstants;

@ZenClass(ChestCavityCtConstants.CT_NAMESPACE + "contenttweaker.OrganItemContent")
@ZenRegister
public class OrganItemContent extends ItemContent implements IOrganDefinitionProvider {

    private final OrganItemRepresentation representation;
    private final OrganData organData;

    public OrganItemContent(OrganItemRepresentation representation) {
        super(representation);
        this.representation = representation;
        this.organData = representation.buildOrganData();
    }

    @Override
    public void setFields() {
        super.setFields();
        if (getRegistryName() != null) {
            ScriptOrganRegistry.register(new ScriptOrganDefinition(
                    getRegistryName(),
                    organData.isPseudoOrgan(),
                    organData.getOrganScoresView()));
        }
    }

    @Override
    public OrganData getOrganData() {
        return organData;
    }

    public OrganItemRepresentation getRepresentation() {
        return representation;
    }
}
