package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OrganDataApi {

    OrganDataApi() {
    }

    public void register(ResourceLocation itemId, Map<String, Float> scores) {
        final ResourceLocation targetItemId = itemId;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetItemId, targetScores, false));
    }

    public void registerPseudo(ResourceLocation itemId, Map<String, Float> scores) {
        final ResourceLocation targetItemId = itemId;
        final Map<String, Float> targetScores = copyScores(scores);
        DataLoaders.applyRuntimeOverride(() -> registerNow(targetItemId, targetScores, true));
    }

    public void addScore(ResourceLocation itemId, String scoreId, float value) {
        final ResourceLocation targetItemId = itemId;
        final String targetScoreId = scoreId;
        final float targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> addScoreNow(targetItemId, targetScoreId, targetValue));
    }

    public void removeScore(ResourceLocation itemId, String scoreId) {
        final ResourceLocation targetItemId = itemId;
        final String targetScoreId = scoreId;
        DataLoaders.applyRuntimeOverride(() -> removeScoreNow(targetItemId, targetScoreId));
    }

    public void remove(ResourceLocation itemId) {
        final ResourceLocation targetItemId = itemId;
        DataLoaders.applyRuntimeOverride(() -> OrganData.unregister(targetItemId));
    }

    public void setPseudo(ResourceLocation itemId, boolean value) {
        final ResourceLocation targetItemId = itemId;
        final boolean targetValue = value;
        DataLoaders.applyRuntimeOverride(() -> setPseudoNow(targetItemId, targetValue));
    }

    public OrganDataView get(ResourceLocation itemId) {
        OrganData data = OrganData.get(itemId);
        return data == null ? null : new OrganDataView(data);
    }

    private void registerNow(ResourceLocation itemId, Map<String, Float> scores, boolean pseudo) {
        if (itemId == null) {
            return;
        }
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudo);
        data.setOrganScores(copyScores(scores));
        OrganData.register(itemId, data);
    }

    private void addScoreNow(ResourceLocation itemId, String scoreId, float value) {
        if (itemId == null || scoreId == null) {
            return;
        }
        OrganData data = getOrCreate(itemId);
        data.getOrganScores().put(scoreId, value);
    }

    private void removeScoreNow(ResourceLocation itemId, String scoreId) {
        OrganData data = OrganData.get(itemId);
        if (data != null && scoreId != null) {
            data.getOrganScores().remove(scoreId);
        }
    }

    private void setPseudoNow(ResourceLocation itemId, boolean value) {
        if (itemId != null) {
            getOrCreate(itemId).setPseudoOrgan(value);
        }
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
        Map<String, Float> copy = new LinkedHashMap<>();
        if (scores != null) {
            copy.putAll(scores);
        }
        return copy;
    }
}
