package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class OrganRejection extends CCPotion {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource("cc_organ_rejection").setDamageBypassesArmor();

    public OrganRejection() {
        super(true, 0xC8FF00);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration <= 1;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!entityLivingBaseIn.world.isRemote) {
            entityLivingBaseIn.attackEntityFrom(DAMAGE_SOURCE, CCConfig.ORGAN_REJECTION_DAMAGE);
        }
    }
}
