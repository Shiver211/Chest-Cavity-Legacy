package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;
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
        OrganData data = getOrCreate(itemId);
        if (scoreId != null) {
            data.getOrganScores().put(scoreId, value);
        }
    }

    public void removeScore(ResourceLocation itemId, String scoreId) {
        OrganData data = OrganData.get(itemId);
        if (data != null && scoreId != null) {
            data.getOrganScores().remove(scoreId);
        }
    }

    public void remove(ResourceLocation itemId) {
        OrganData.unregister(itemId);
    }

    public void setPseudo(ResourceLocation itemId, boolean value) {
        getOrCreate(itemId).setPseudoOrgan(value);
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
        OrganData.register(itemId, data);
    }

    private OrganData getOrCreate(ResourceLocation itemId) {
        OrganData data = OrganData.get(itemId);
        if (data == null) {
            data = new OrganData();
            OrganData.register(itemId, data);
        }
        return data;
    }

    private Map<String, Float> copyScores(Map<String, Float> scores) {
        Map<String, Float> copy = new LinkedHashMap<String, Float>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
