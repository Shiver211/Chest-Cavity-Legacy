package com.shiver.chestcavity.content;

import com.shiver.chestcavity.layout.ChestLayoutDef;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContentManifest {

    private final Map<ResourceLocation, OrganDef> organs = new LinkedHashMap<>();
    private final Map<String, BodyTypeDef> bodyTypes = new LinkedHashMap<>();
    private final Map<ResourceLocation, String> entityAssignments = new LinkedHashMap<>();
    private final Map<ResourceLocation, ChestLayoutDef> layouts = new LinkedHashMap<>();

    public ContentManifest() {
    }

    public ContentManifest(ContentManifest source) {
        mergeFrom(source);
    }

    public void mergeFrom(ContentManifest source) {
        if (source == null) {
            return;
        }
        for (OrganDef organ : source.getOrgans().values()) {
            registerOrgan(organ);
        }
        for (BodyTypeDef type : source.getBodyTypes().values()) {
            registerBodyType(type);
        }
        for (Map.Entry<ResourceLocation, String> entry : source.getEntityAssignments().entrySet()) {
            registerEntityAssignment(entry.getKey(), entry.getValue());
        }
        for (ChestLayoutDef layout : source.getLayouts().values()) {
            registerLayout(layout);
        }
    }

    public void registerOrgan(OrganDef organ) {
        if (organ != null) {
            organs.put(organ.getItemId(), organ.copy());
        }
    }

    public void replaceOrgans(Map<ResourceLocation, OrganDef> organs) {
        this.organs.clear();
        if (organs != null) {
            for (OrganDef organ : organs.values()) {
                registerOrgan(organ);
            }
        }
    }

    public void removeOrgan(ResourceLocation itemId) {
        if (itemId != null) {
            organs.remove(itemId);
        }
    }

    public OrganDef getOrgan(ResourceLocation itemId) {
        OrganDef organ = itemId == null ? null : organs.get(itemId);
        return organ == null ? null : organ.copy();
    }

    public Map<ResourceLocation, OrganDef> getOrgans() {
        Map<ResourceLocation, OrganDef> copy = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, OrganDef> entry : organs.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(copy);
    }

    public BodyTypeDef getOrCreateBodyType(String typeId) {
        if (typeId == null || typeId.isEmpty()) {
            return null;
        }
        BodyTypeDef type = bodyTypes.get(typeId);
        if (type == null) {
            type = new BodyTypeDef(typeId);
            bodyTypes.put(typeId, type);
        }
        return type;
    }

    public void registerBodyType(BodyTypeDef type) {
        if (type != null) {
            bodyTypes.put(type.getId(), type.copy());
        }
    }

    public void removeBodyType(String typeId) {
        if (typeId != null) {
            bodyTypes.remove(typeId);
        }
    }

    public BodyTypeDef getBodyType(String typeId) {
        BodyTypeDef type = typeId == null ? null : bodyTypes.get(typeId);
        return type == null ? null : type.copy();
    }

    public Map<String, BodyTypeDef> getBodyTypes() {
        Map<String, BodyTypeDef> copy = new LinkedHashMap<>();
        for (Map.Entry<String, BodyTypeDef> entry : bodyTypes.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(copy);
    }

    public void registerEntityAssignment(ResourceLocation entityId, String typeId) {
        if (entityId != null && typeId != null) {
            entityAssignments.put(entityId, typeId);
        }
    }

    public void removeEntityAssignment(ResourceLocation entityId) {
        if (entityId != null) {
            entityAssignments.remove(entityId);
        }
    }

    public String getEntityAssignment(ResourceLocation entityId) {
        return entityId == null ? null : entityAssignments.get(entityId);
    }

    public Map<ResourceLocation, String> getEntityAssignments() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entityAssignments));
    }

    public void registerLayout(ChestLayoutDef layout) {
        if (layout != null) {
            layouts.put(layout.getId(), layout);
        }
    }

    public ChestLayoutDef getLayout(ResourceLocation id) {
        return id == null ? null : layouts.get(id);
    }

    public Map<ResourceLocation, ChestLayoutDef> getLayouts() {
        return Collections.unmodifiableMap(layouts);
    }

    public ContentManifest copy() {
        return new ContentManifest(this);
    }
}
