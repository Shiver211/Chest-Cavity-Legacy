package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.OrganDataView;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.Map;

/**
 * 暴露给 ZenScript 的器官数据只读视图。
 */
@ZenRegister
@ZenClass("mods.chestcavity.OrganDataView")
public final class CrTOrganDataView {

    private final OrganDataView view;

    /**
     * 使用内部器官数据视图创建脚本包装对象。
     *
     * @param view 内部器官数据视图。
     */
    CrTOrganDataView(OrganDataView view) {
        this.view = view;
    }

    /**
     * 判断该器官定义是否为伪器官。
     *
     * @return `true` 表示为伪器官。
     */
    @ZenGetter("isPseudoOrgan")
    public boolean isPseudoOrgan() {
        return view.isPseudoOrgan();
    }

    /**
     * 返回器官分数字典。
     *
     * @return 器官分数字典。
     */
    @ZenGetter("organScores")
    public Map<String, Float> getOrganScores() {
        return view.getOrganScores();
    }
}
