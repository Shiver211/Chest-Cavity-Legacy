package com.shiver.chestcavity.mixin;

import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotionEffect.class)
public interface PotionEffectAccessor {

    @Accessor("duration")
    void chestcavity$setDuration(int duration);

    @Accessor("amplifier")
    void chestcavity$setAmplifier(int amplifier);
}
