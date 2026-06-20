package com.shiver.chestcavity.content;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.chest.types.CompiledChestCavityType;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.ChestLayouts;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CompiledContent {

    private final Map<ResourceLocation, OrganData> organs;
    private final Map<String, ChestCavityType> types;
    private final Map<ResourceLocation, String> entityAssignments;
    private final Map<ResourceLocation, ChestLayoutDef> layouts;
    private final ChestCavityType fallbackType;

    private CompiledContent(Map<ResourceLocation, OrganData> organs,
                            Map<String, ChestCavityType> types,
                            Map<ResourceLocation, String> entityAssignments,
                            Map<ResourceLocation, ChestLayoutDef> layouts,
                            ChestCavityType fallbackType) {
        this.organs = Collections.unmodifiableMap(organs);
        this.types = Collections.unmodifiableMap(types);
        this.entityAssignments = Collections.unmodifiableMap(entityAssignments);
        this.layouts = Collections.unmodifiableMap(layouts);
        this.fallbackType = fallbackType;
    }

    public static CompiledContent compile(ContentManifest manifest, String fallbackId) {
        ContentManifest source = manifest == null ? new ContentManifest() : manifest;

        Map<ResourceLocation, ChestLayoutDef> layouts = new LinkedHashMap<>();
        layouts.put(ChestLayouts.DEFAULT_ID, ChestLayouts.DEFAULT);
        layouts.putAll(source.getLayouts());

        Map<ResourceLocation, OrganData> organs = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, OrganDef> entry : source.getOrgans().entrySet()) {
            organs.put(entry.getKey(), entry.getValue().getData());
        }

        Map<String, ChestCavityType> types = new LinkedHashMap<>();
        for (Map.Entry<String, BodyTypeDef> entry : source.getBodyTypes().entrySet()) {
            types.put(entry.getKey(), new CompiledChestCavityType(entry.getValue(), organs));
        }

        ChestCavityType fallbackType = types.get(fallbackId);
        if (fallbackType == null) {
            BodyTypeDef fallbackDef = BodyTypeDefs.createFallback(fallbackId);
            fallbackType = new CompiledChestCavityType(fallbackDef, organs);
            types.put(fallbackId, fallbackType);
        }

        Map<ResourceLocation, String> assignments = new LinkedHashMap<>(source.getEntityAssignments());
        return new CompiledContent(organs, types, assignments, layouts, fallbackType);
    }

    public OrganData getOrgan(ResourceLocation itemId) {
        OrganData data = itemId == null ? null : organs.get(itemId);
        return data == null ? null : copyData(data);
    }

    public Map<ResourceLocation, OrganData> getOrgans() {
        Map<ResourceLocation, OrganData> copy = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, OrganData> entry : organs.entrySet()) {
            copy.put(entry.getKey(), copyData(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public ChestCavityType getType(String id) {
        ChestCavityType type = id == null ? null : types.get(id);
        return type == null ? fallbackType : type;
    }

    public ChestCavityType getFallbackType() {
        return fallbackType;
    }

    public Map<String, ChestCavityType> getTypes() {
        return types;
    }

    public String getAssignedTypeId(ResourceLocation entityId) {
        return entityId == null ? null : entityAssignments.get(entityId);
    }

    public Map<ResourceLocation, String> getEntityAssignments() {
        return entityAssignments;
    }

    public ChestLayoutDef getLayout(ResourceLocation id) {
        ChestLayoutDef layout = id == null ? null : layouts.get(id);
        return layout == null ? ChestLayouts.DEFAULT : layout;
    }

    public Map<ResourceLocation, ChestLayoutDef> getLayouts() {
        return layouts;
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
