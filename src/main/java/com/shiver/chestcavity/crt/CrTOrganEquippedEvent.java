package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.chestcavity.event.OrganEquippedEvent")
public final class CrTOrganEquippedEvent extends CrTOrganChangeEvent {

    CrTOrganEquippedEvent(EntityLivingBase entity, int slot, ItemStack organ, boolean pseudoOrgan) {
        super(entity, slot, organ, pseudoOrgan);
    }
}
