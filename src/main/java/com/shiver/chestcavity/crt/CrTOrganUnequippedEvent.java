package com.shiver.chestcavity.crt;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.chestcavity.event.OrganUnequippedEvent")
public final class CrTOrganUnequippedEvent extends CrTOrganChangeEvent {

    CrTOrganUnequippedEvent(EntityLivingBase entity, int slot, ItemStack organ, boolean pseudoOrgan) {
        super(entity, slot, organ, pseudoOrgan);
    }
}
