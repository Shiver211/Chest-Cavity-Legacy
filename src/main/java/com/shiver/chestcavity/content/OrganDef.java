package com.shiver.chestcavity.content;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.util.ResourceLocation;

public final class OrganDef {

    private final ResourceLocation itemId;
    private final OrganData data;

    public OrganDef(ResourceLocation itemId, OrganData data) {
        if (itemId == null) {
            throw new IllegalArgumentException("Organ item id must not be null");
        }
        this.itemId = itemId;
        this.data = copyData(data);
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public OrganData getData() {
        return copyData(data);
    }

    public OrganDef copy() {
        return new OrganDef(itemId, data);
    }

    private static OrganData copyData(OrganData source) {
        OrganData copy = new OrganData();
        if (source != null) {
            copy.setPseudoOrgan(source.isPseudoOrgan());
            copy.setOrganScores(source.getOrganScores());
        }
        return copy;
    }
}
