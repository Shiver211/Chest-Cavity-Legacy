package com.shiver.chestcavity.mixin;

import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityCreeper.class)
public interface EntityCreeperAccessor {

    @Accessor("timeSinceIgnited")
    void chestcavity$setTimeSinceIgnited(int value);
}
