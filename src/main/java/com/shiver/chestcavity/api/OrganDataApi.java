package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.content.ContentRegistry;
import com.shiver.chestcavity.content.OrganDef;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OrganDataApi {

    OrganDataApi() {
    }

    public void register(ResourceLocation itemId, Map<String, Float> scores) {
        register(itemId, scores, false);
    }

    public void registerPseudo(ResourceLocation itemId, Map<String, Float> scores) {
        register(itemId, scores, true);
    }

    public void addScore(ResourceLocation itemId, String scoreId, float value) {
        if (itemId == null || scoreId == null) {
            return;
        }
        ContentRegistry.applyScriptOperation(manifest -> {
            OrganData data = dataFromManifest(manifest, itemId);
            data.getOrganScores().put(scoreId, value);
            manifest.registerOrgan(new OrganDef(itemId, data));
        });
    }

    public void removeScore(ResourceLocation itemId, String scoreId) {
        if (itemId == null || scoreId == null) {
            return;
        }
        ContentRegistry.applyScriptOperation(manifest -> {
            OrganData data = dataFromManifest(manifest, itemId);
            data.getOrganScores().remove(scoreId);
            manifest.registerOrgan(new OrganDef(itemId, data));
        });
    }

    public void remove(ResourceLocation itemId) {
        ContentRegistry.removeScriptOrgan(itemId);
    }

    public void setPseudo(ResourceLocation itemId, boolean value) {
        if (itemId == null) {
            return;
        }
        ContentRegistry.applyScriptOperation(manifest -> {
            OrganData data = dataFromManifest(manifest, itemId);
            data.setPseudoOrgan(value);
            manifest.registerOrgan(new OrganDef(itemId, data));
        });
    }

    public OrganDataView get(ResourceLocation itemId) {
        OrganData data = OrganData.get(itemId);
        return data == null ? null : new OrganDataView(data);
    }

    private void register(ResourceLocation itemId, Map<String, Float> scores, boolean pseudo) {
        if (itemId == null) {
            return;
        }
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudo);
        data.setOrganScores(copyScores(scores));
        ContentRegistry.registerScriptOrgan(new OrganDef(itemId, data));
    }

    private OrganData dataFromManifest(com.shiver.chestcavity.content.ContentManifest manifest, ResourceLocation itemId) {
        OrganDef def = manifest.getOrgan(itemId);
        return def == null ? new OrganData() : def.getData();
    }

    private Map<String, Float> copyScores(Map<String, Float> scores) {
        Map<String, Float> copy = new LinkedHashMap<>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
