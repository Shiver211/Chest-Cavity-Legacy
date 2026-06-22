package com.shiver.chestcavity.crt;

import crafttweaker.util.EventList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * 保存并分发 CraftTweaker 胸腔相关事件列表。
 */
public final class CrTChestCavityEvents {

    static final EventList<CrTAbilityActivatedEvent> ABILITY_ACTIVATED = new EventList<CrTAbilityActivatedEvent>();
    static final EventList<CrTOrganEquippedEvent> ORGAN_EQUIPPED = new EventList<CrTOrganEquippedEvent>();
    static final EventList<CrTOrganUnequippedEvent> ORGAN_UNEQUIPPED = new EventList<CrTOrganUnequippedEvent>();

    /**
     * 工具类，不允许外部实例化。
     */
    private CrTChestCavityEvents() {
    }

    /**
     * 发布一次主动能力触发事件。
     *
     * @param entity 触发能力的实体。
     * @param abilityId 能力标识。
     * @param score 当前能力分数。
     */
    public static void publishAbilityActivated(EntityLivingBase entity, String abilityId, float score) {
        if (ABILITY_ACTIVATED.hasHandlers()) {
            ABILITY_ACTIVATED.publish(new CrTAbilityActivatedEvent(entity, abilityId, score));
        }
    }

    /**
     * 发布一次器官装备事件。
     *
     * @param entity 装备器官的实体。
     * @param slot 槽位索引。
     * @param stack 装备的器官物品。
     * @param pseudoOrgan 是否为伪器官。
     */
    public static void publishOrganEquipped(EntityLivingBase entity, int slot, ItemStack stack, boolean pseudoOrgan) {
        if (ORGAN_EQUIPPED.hasHandlers()) {
            ORGAN_EQUIPPED.publish(new CrTOrganEquippedEvent(entity, slot, stack, pseudoOrgan));
        }
    }

    /**
     * 发布一次器官卸下事件。
     *
     * @param entity 卸下器官的实体。
     * @param slot 槽位索引。
     * @param stack 卸下的器官物品。
     * @param pseudoOrgan 是否为伪器官。
     */
    public static void publishOrganUnequipped(EntityLivingBase entity, int slot, ItemStack stack, boolean pseudoOrgan) {
        if (ORGAN_UNEQUIPPED.hasHandlers()) {
            ORGAN_UNEQUIPPED.publish(new CrTOrganUnequippedEvent(entity, slot, stack, pseudoOrgan));
        }
    }
}
