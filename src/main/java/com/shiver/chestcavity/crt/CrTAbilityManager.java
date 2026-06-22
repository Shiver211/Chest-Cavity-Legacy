package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.util.IEventHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 向 ZenScript 暴露主动能力注册接口。
 */
@ZenRegister
@ZenClass("mods.chestcavity.AbilityManager")
public final class CrTAbilityManager {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTAbilityManager() {
    }

    /**
     * 注册一个无冷却的主动能力占位条目。
     *
     * @param scoreId 能力分数标识。
     * @param displayName 轮盘显示名称。
     */
    @ZenMethod
    public static void registerAbility(String scoreId, String displayName) {
        registerAbility(scoreId, displayName, 0);
    }

    /**
     * 注册一个带固定冷却时间的主动能力占位条目。
     *
     * @param scoreId 能力分数标识。
     * @param displayName 轮盘显示名称。
     * @param cooldownTicks 冷却时长。
     */
    @ZenMethod
    public static void registerAbility(String scoreId, String displayName, int cooldownTicks) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, displayName, cooldownTicks, (player, chestCavity) -> true);
    }

    /**
     * 注册一个由脚本事件回调处理的主动能力。
     *
     * @param scoreId 能力分数标识。
     * @param displayName 轮盘显示名称。
     * @param handler 事件处理器。
     */
    @ZenMethod
    public static void registerAbility(String scoreId, String displayName, IEventHandler<CrTAbilityActivatedEvent> handler) {
        registerAbility(scoreId, displayName, 0, handler);
    }

    /**
     * 注册一个带冷却且由脚本事件回调处理的主动能力。
     *
     * @param scoreId 能力分数标识。
     * @param displayName 轮盘显示名称。
     * @param cooldownTicks 冷却时长。
     * @param handler 事件处理器。
     */
    @ZenMethod
    public static void registerAbility(String scoreId, String displayName, int cooldownTicks, IEventHandler<CrTAbilityActivatedEvent> handler) {
        ChestCavityApis.ABILITIES.registerAbility(scoreId, displayName, cooldownTicks, (player, chestCavity) -> {
            if (handler == null) {
                return true;
            }
            CrTAbilityActivatedEvent event = new CrTAbilityActivatedEvent(player, scoreId, chestCavity.getOrganScore(scoreId));
            handler.handle(event);
            return !event.isCanceled();
        });
    }
}
