package com.shiver.chestcavity.chest.organs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shiver.chestcavity.ChestCavityLegacy;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OrganSerializer {

    public OrganEntry read(ResourceLocation id, JsonObject json) {
        if (!json.has("itemID") || !json.has("organScores")) {
            ChestCavityLegacy.LOGGER.warn("Skipping organ {} because itemID or organScores is missing.", id);
            return null;
        }

        OrganData data = new OrganData();
        if (json.has("pseudoOrgan")) {
            data.setPseudoOrgan(json.get("pseudoOrgan").getAsBoolean());
        }
        data.setOrganScores(readOrganScores(id, json.get("organScores")));
        return new OrganEntry(new ResourceLocation(json.get("itemID").getAsString()), data);
    }

    private Map<ResourceLocation, Float> readOrganScores(ResourceLocation id, JsonElement element) {
        Map<ResourceLocation, Float> scores = new LinkedHashMap<>();
        if (element == null || !element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping organ scores in {} because they are not an array.", id);
            return scores;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                if (!entry.isJsonObject()) {
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                if (!object.has("id") || !object.has("value")) {
                    ChestCavityLegacy.LOGGER.warn("Skipping organ score entry in {} because id or value is missing.", id);
                    continue;
                }
                scores.put(new ResourceLocation(object.get("id").getAsString()), object.get("value").getAsFloat());
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse organ score entry in {}.", id, e);
            }
        }
        return scores;
    }

    public static final class OrganEntry {
        public final ResourceLocation itemId;
        public final OrganData data;

        private OrganEntry(ResourceLocation itemId, OrganData data) {
            this.itemId = itemId;
            this.data = data;
        }
    }
}
