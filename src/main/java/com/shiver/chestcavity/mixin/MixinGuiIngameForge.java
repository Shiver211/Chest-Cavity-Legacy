package com.shiver.chestcavity.mixin;

import com.shiver.chestcavity.organ.OrganFormulas;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge {

    @Redirect(
            method = "renderAir(II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"
            )
    )
    private boolean chestcavity$showAirWhenNotFull(EntityPlayer player, Material material) {
        return player.isInsideOfMaterial(material)
                || (material == Material.WATER && player.getAir() < OrganFormulas.MAX_AIR);
    }
}
