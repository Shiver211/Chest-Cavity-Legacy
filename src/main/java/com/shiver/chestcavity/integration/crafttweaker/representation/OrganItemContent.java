package com.shiver.chestcavity.integration.crafttweaker.representation;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.integration.crafttweaker.runtime.IOrganDefinitionProvider;
import com.shiver.chestcavity.integration.crafttweaker.runtime.OrganDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.OrganRegistry;
import com.teamacronymcoders.contenttweaker.modules.vanilla.items.ItemContent;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;

@ZenClass(CtConstants.CT_NAMESPACE + "contenttweaker.OrganItemContent")
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
            OrganRegistry.register(new OrganDefinition(
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
