package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.organ.OrganCombatController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    /**
     * Wrap the post-armor damage. {@code ModifyVariable} at the potion-calculation
     * INVOKE is too late: {@code fload} has already copied {@code damageAmount} onto
     * the operand stack, so only the local is changed and the invoke return overwrites it.
     */
    @Redirect(
            method = "damageEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ISpecialArmor$ArmorProperties;applyArmor(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/NonNullList;Lnet/minecraft/util/DamageSource;D)F",
                    remap = false
            )
    )
    private float chestcavity$applyDefenseAfterArmor(EntityLivingBase entity, NonNullList<ItemStack> inventory,
                                                    DamageSource source, double damage) {
        float afterArmor = ISpecialArmor.ArmorProperties.applyArmor(entity, inventory, source, damage);
        return OrganCombatController.applyDefense(ChestCavityHelper.getOrNull(entity), source, afterArmor);
    }
}
