package com.shiver.chestcavity.chest.organs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shiver.chestcavity.ChestCavityLegacy;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 负责将器官 JSON 数据转换为运行时使用的器官定义。
 */
public final class OrganSerializer {

    /**
     * 从 JSON 对象中读取一个器官定义。
     *
     * @param id 当前数据文件的资源标识。
     * @param json 解析后的 JSON 对象。
     * @return 解析出的器官条目；如果数据不完整则返回 `null`。
     */
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

    /**
     * 从 JSON 数组中读取器官分数字典。
     *
     * @param id 当前数据文件的资源标识。
     * @param element 记录器官分数的 JSON 元素。
     * @return 解析出的器官分数字典。
     */
    private Map<String, Float> readOrganScores(ResourceLocation id, JsonElement element) {
        Map<String, Float> scores = new LinkedHashMap<>();
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
                scores.put(object.get("id").getAsString(), object.get("value").getAsFloat());
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse organ score entry in {}.", id, e);
            }
        }
        return scores;
    }

    /**
     * 表示一次解析后得到的器官条目。
     */
    public static final class OrganEntry {
        public final ResourceLocation itemId;
        public final OrganData data;

        /**
        * 创建一个器官条目。
        *
        * @param itemId 器官物品的注册名。
        * @param data 对应的器官数据。
        */
        private OrganEntry(ResourceLocation itemId, OrganData data) {
            this.itemId = itemId;
            this.data = data;
        }
    }
}
