package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.organ.OrganCombatController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @ModifyVariable(
            method = "damageEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F"
            ),
            argsOnly = true,
            ordinal = 0
    )
    private float chestcavity$applyDefenseAfterArmor(float damageAmount, DamageSource damageSrc) {
        return OrganCombatController.applyDefense(
                ChestCavityHelper.getOrNull((EntityLivingBase) (Object) this), damageSrc, damageAmount);
    }
}
