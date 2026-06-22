package com.shiver.chestcavity.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 维护器官分数标识到显示名称的映射关系。
 */
public final class ScoreApi {

    private final Map<String, String> displayNames = new LinkedHashMap<String, String>();

    /**
     * 仅允许通过统一 API 单例创建。
     */
    ScoreApi() {
    }

    /**
     * 注册一个分数标识对应的显示名称。
     *
     * @param scoreId 分数标识。
     * @param displayName 显示名称。
     */
    public void addScore(String scoreId, String displayName) {
        if (scoreId != null && displayName != null) {
            displayNames.put(scoreId, displayName);
        }
    }

    /**
     * 返回指定分数标识的显示名称。
     *
     * @param scoreId 分数标识。
     * @return 显示名称；如果不存在则返回 `null`。
     */
    public String getDisplayName(String scoreId) {
        return scoreId == null ? null : displayNames.get(scoreId);
    }

    /**
     * 返回全部显示名称映射的只读视图。
     *
     * @return 分数显示名映射。
     */
    public Map<String, String> getDisplayNames() {
        return Collections.unmodifiableMap(displayNames);
    }
}
