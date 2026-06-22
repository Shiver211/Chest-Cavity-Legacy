package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;

import java.util.Map;

/**
 * 对外暴露器官数据的只读视图。
 */
public final class OrganDataView {

    private final OrganData data;

    /**
     * 使用内部器官数据创建一个只读视图。
     *
     * @param data 被包装的器官数据。
     */
    OrganDataView(OrganData data) {
        this.data = data;
    }

    /**
     * 判断该器官定义是否为伪器官。
     *
     * @return `true` 表示这是伪器官。
     */
    public boolean isPseudoOrgan() {
        return data.isPseudoOrgan();
    }

    /**
     * 返回器官分数字典的只读视图。
     *
     * @return 器官分数字典。
     */
    public Map<String, Float> getOrganScores() {
        return data.getOrganScoresView();
    }
}
