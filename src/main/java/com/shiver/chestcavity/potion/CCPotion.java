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
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        renderIcon(mc, x + 6, y + 7, 1.0F);
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        renderIcon(mc, x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private void renderIcon(Minecraft mc, int x, int y, float alpha) {
        ResourceLocation id = getRegistryName();
        if (id == null) {
            return;
        }

        mc.getTextureManager().bindTexture(new ResourceLocation(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png"));
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
