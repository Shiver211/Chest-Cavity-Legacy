package com.shiver.chestcavity.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CCPotion extends Potion {

    public CCPotion(boolean badEffect, int color) {
        super(badEffect, color);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        renderIcon(x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        renderIcon(x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private void renderIcon(int x, int y, float alpha) {
        ResourceLocation id = getRegistryName();
        if (id == null) return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(
                new ResourceLocation(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png")
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
