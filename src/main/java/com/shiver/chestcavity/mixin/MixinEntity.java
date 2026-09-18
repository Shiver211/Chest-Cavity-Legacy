package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.organ.OrganMovementController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @ModifyVariable(method = "updateFallState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double chestcavity$scaleFallDistance(double y) {
        if (y >= 0.0D || !((Object) this instanceof EntityLivingBase)) {
            return y;
        }
        return OrganMovementController.applyFallDistance((EntityLivingBase) (Object) this, y);
    }
}
