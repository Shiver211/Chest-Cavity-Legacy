package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.organ.OrganFormulas;
import com.shiver.chestcavity.organ.OrganTickController;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Redirect(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setAir(I)V", ordinal = 0), require = 1)
    private void chestcavity$applyLandBreath(EntityLivingBase entity, int air) {
        if (air == OrganFormulas.MAX_AIR && !entity.isInsideOfMaterial(Material.WATER)) {
            entity.setAir(OrganTickController.applyLandAirRestore(entity, entity.getAir()));
            return;
        }
        entity.setAir(air);
    }

    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    private void chestcavity$applyWaterBreath(int air, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(OrganTickController.applyBreathInWater((EntityLivingBase) (Object) this, air, cir.getReturnValueI()));
    }
}
