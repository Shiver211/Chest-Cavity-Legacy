package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class Ruminating extends CCPotion {

    public Ruminating() {
        super(false, 0x91B84A);
        setBeneficial();
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % Math.max(1, CCConfig.RUMINATION_TIME) == 1;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!entityLivingBaseIn.world.isRemote && entityLivingBaseIn instanceof EntityPlayer) {
            ((EntityPlayer) entityLivingBaseIn).getFoodStats().addStats(1, 0.1F);
        }
    }
}
