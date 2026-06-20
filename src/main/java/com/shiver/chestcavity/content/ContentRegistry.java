package com.shiver.chestcavity.content;

import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.ChestLayouts;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ContentRegistry {

    private static final List<ManifestOperation> SCRIPT_OPERATIONS = new ArrayList<>();
    private static ContentManifest activeManifest = new ContentManifest();
    private static CompiledContent compiledContent = CompiledContent.compile(activeManifest, DataLoaders.FALLBACK_ID);

    private ContentRegistry() {
    }

    public static synchronized ContentManifest createReloadManifest() {
        return new ContentManifest();
    }

    public static synchronized void applyScriptManifest(ContentManifest manifest) {
        if (manifest != null) {
            for (ManifestOperation operation : SCRIPT_OPERATIONS) {
                operation.apply(manifest);
            }
        }
    }

    public static synchronized void publish(ContentManifest manifest) {
        activeManifest = manifest == null ? new ContentManifest() : manifest.copy();
        compiledContent = CompiledContent.compile(activeManifest, DataLoaders.FALLBACK_ID);
        ChestLayouts.replaceAll(compiledContent.getLayouts());
    }

    public static synchronized CompiledContent getCompiled() {
        return compiledContent;
    }

    public static synchronized ContentManifest getManifest() {
        return activeManifest.copy();
    }

    public static synchronized void applyScriptOperation(ManifestOperation operation) {
        if (operation == null) {
            return;
        }
        SCRIPT_OPERATIONS.add(operation);
        operation.apply(activeManifest);
        publish(activeManifest);
    }

    public static synchronized void registerScriptOrgan(OrganDef organ) {
        applyScriptOperation(manifest -> manifest.registerOrgan(organ));
    }

    public static synchronized void publishSyncedOrgans(java.util.Map<ResourceLocation, OrganDef> organs) {
        activeManifest.replaceOrgans(organs);
        publish(activeManifest);
    }

    public static synchronized void removeScriptOrgan(ResourceLocation itemId) {
        applyScriptOperation(manifest -> manifest.removeOrgan(itemId));
    }

    public static synchronized void removeScriptBodyType(String typeId) {
        applyScriptOperation(manifest -> manifest.removeBodyType(typeId));
    }

    public static synchronized void publishScriptBodyType(BodyTypeDef type) {
        applyScriptOperation(manifest -> manifest.registerBodyType(type));
    }

    public static synchronized void registerScriptEntityAssignment(ResourceLocation entityId, String typeId) {
        applyScriptOperation(manifest -> manifest.registerEntityAssignment(entityId, typeId));
    }

    public static synchronized void removeScriptEntityAssignment(ResourceLocation entityId) {
        applyScriptOperation(manifest -> manifest.removeEntityAssignment(entityId));
    }

    public static synchronized void registerScriptLayout(ChestLayoutDef layout) {
        applyScriptOperation(manifest -> manifest.registerLayout(layout));
    }

    public interface ManifestOperation {
        void apply(ContentManifest manifest);
    }
}
