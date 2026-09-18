package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.organ.OrganCombatController;
import com.shiver.chestcavity.organ.OrganFormulas;
import com.shiver.chestcavity.organ.OrganMovementController;
import com.shiver.chestcavity.organ.OrganTickController;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @ModifyConstant(method = "onEntityUpdate", constant = @Constant(intValue = OrganFormulas.MAX_AIR), require = 1)
    private int chestcavity$applyLandBreath(int air) {
        EntityLivingBase entity = (EntityLivingBase) (Object) this;
        if (entity.isInsideOfMaterial(Material.WATER)) {
            return air;
        }
        return OrganTickController.applyLandAirRestore(entity, entity.getAir());
    }

    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    private void chestcavity$applyWaterBreath(int air, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(OrganTickController.applyBreathInWater((EntityLivingBase) (Object) this, air, cir.getReturnValueI()));
    }

    @Inject(method = "applyArmorCalculations", at = @At("RETURN"), cancellable = true)
    private void chestcavity$applyDefense(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(OrganCombatController.applyDefense(
                ChestCavityHelper.getOrNull((EntityLivingBase) (Object) this), source, cir.getReturnValueF()));
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08D))
    private double chestcavity$applyLightweightGravity(double gravity) {
        return OrganMovementController.applyLightweightToGravity((EntityLivingBase) (Object) this, gravity);
    }
}
