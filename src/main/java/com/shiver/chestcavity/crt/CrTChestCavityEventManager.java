package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.event.IEventHandle;
import crafttweaker.api.event.IEventManager;
import crafttweaker.util.IEventHandler;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 把胸腔相关事件挂接到 CraftTweaker 事件总线上。
 */
@ZenRegister
@ZenExpansion("crafttweaker.events.IEventManager")
public final class CrTChestCavityEventManager {

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTChestCavityEventManager() {
    }

    /**
     * 监听主动能力触发事件。
     *
     * @param manager CraftTweaker 事件管理器。
     * @param handler 事件处理器。
     * @return 事件句柄。
     */
    @ZenMethod
    public static IEventHandle onAbilityActivated(IEventManager manager, IEventHandler<CrTAbilityActivatedEvent> handler) {
        return CrTChestCavityEvents.ABILITY_ACTIVATED.add(handler);
    }

    /**
     * 监听器官装备事件。
     *
     * @param manager CraftTweaker 事件管理器。
     * @param handler 事件处理器。
     * @return 事件句柄。
     */
    @ZenMethod
    public static IEventHandle onOrganEquipped(IEventManager manager, IEventHandler<CrTOrganEquippedEvent> handler) {
        return CrTChestCavityEvents.ORGAN_EQUIPPED.add(handler);
    }

    /**
     * 监听器官卸下事件。
     *
     * @param manager CraftTweaker 事件管理器。
     * @param handler 事件处理器。
     * @return 事件句柄。
     */
    @ZenMethod
    public static IEventHandle onOrganUnequipped(IEventManager manager, IEventHandler<CrTOrganUnequippedEvent> handler) {
        return CrTChestCavityEvents.ORGAN_UNEQUIPPED.add(handler);
    }
}
