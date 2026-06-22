package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;

/**
 * 暴露给 ZenScript 的器官装备事件。
 */
@ZenRegister
@ZenClass("mods.chestcavity.event.OrganEquippedEvent")
public final class CrTOrganEquippedEvent extends CrTOrganChangeEvent {

    /**
     * 创建一条器官装备事件。
     *
     * @param entity 发生变更的实体。
     * @param slot 变更槽位。
     * @param organ 装备的器官物品。
     * @param pseudoOrgan 是否为伪器官。
     */
    CrTOrganEquippedEvent(EntityLivingBase entity, int slot, ItemStack organ, boolean pseudoOrgan) {
        super(entity, slot, organ, pseudoOrgan);
    }
}
