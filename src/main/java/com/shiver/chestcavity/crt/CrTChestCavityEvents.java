package com.shiver.chestcavity.crt;

import crafttweaker.util.EventList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public final class CrTChestCavityEvents {

    static final EventList<CrTAbilityActivatedEvent> ABILITY_ACTIVATED = new EventList<>();
    static final EventList<CrTOrganEquippedEvent> ORGAN_EQUIPPED = new EventList<>();
    static final EventList<CrTOrganUnequippedEvent> ORGAN_UNEQUIPPED = new EventList<>();

    private CrTChestCavityEvents() {
    }

    public static void publishAbilityActivated(EntityLivingBase entity, String abilityId, float score) {
        if (ABILITY_ACTIVATED.hasHandlers()) {
            ABILITY_ACTIVATED.publish(new CrTAbilityActivatedEvent(entity, abilityId, score));
        }
    }

    public static void publishOrganEquipped(EntityLivingBase entity, int slot, ItemStack stack, boolean pseudoOrgan) {
        if (ORGAN_EQUIPPED.hasHandlers()) {
            ORGAN_EQUIPPED.publish(new CrTOrganEquippedEvent(entity, slot, stack, pseudoOrgan));
        }
    }

    public static void publishOrganUnequipped(EntityLivingBase entity, int slot, ItemStack stack, boolean pseudoOrgan) {
        if (ORGAN_UNEQUIPPED.hasHandlers()) {
            ORGAN_UNEQUIPPED.publish(new CrTOrganUnequippedEvent(entity, slot, stack, pseudoOrgan));
        }
    }
}
