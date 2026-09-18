package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.organ.OrganFoodController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodStats.class)
public abstract class MixinFoodStats {

    @Shadow
    private int foodTimer;

    @Shadow
    private float foodExhaustionLevel;

    @Unique
    private EntityPlayer chestcavity$player;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void chestcavity$applyMetabolism(EntityPlayer player, CallbackInfo ci) {
        this.chestcavity$player = player;
        this.foodTimer = OrganFoodController.applySpleenMetabolism(player, this.foodTimer);
    }

    @Inject(method = "addStats(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void chestcavity$applyEatenFood(ItemFood foodItem, ItemStack stack, CallbackInfo ci) {
        if (this.chestcavity$player != null
                && OrganFoodController.handleEatenFood((FoodStats) (Object) this, this.chestcavity$player, foodItem, stack)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), argsOnly = true)
    private float chestcavity$applyEndurance(float exhaustion) {
        if (this.foodExhaustionLevel != this.foodExhaustionLevel) {
            this.foodExhaustionLevel = 0.0F;
        }
        return OrganFoodController.applyEnduranceExhaustion(this.chestcavity$player, exhaustion);
    }
}
