package com.shiver.chestcavity.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 胸腔模组药水效果的基础实现，负责自定义图标渲染。
 */
public class CCPotion extends Potion {

    /**
     * 创建一个基础胸腔药水效果。
     *
     * @param badEffect 是否为负面效果。
     * @param color 药水颜色。
     */
    public CCPotion(boolean badEffect, int color) {
        super(badEffect, color);
    }

    /**
     * 禁用原版状态图标，改用自定义材质渲染。
     *
     * @return 始终返回 `false`。
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasStatusIcon() {
        return false;
    }

    /**
     * 在背包药水效果栏中渲染自定义图标。
     *
     * @param x 绘制起点 X。
     * @param y 绘制起点 Y。
     * @param effect 当前药水效果。
     * @param mc 客户端实例。
     */
    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        renderIcon(mc, x + 6, y + 7, 1.0F);
    }

    /**
     * 在 HUD 药水效果栏中渲染自定义图标。
     *
     * @param x 绘制起点 X。
     * @param y 绘制起点 Y。
     * @param effect 当前药水效果。
     * @param mc 客户端实例。
     * @param alpha 当前透明度。
     */
    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        renderIcon(mc, x + 3, y + 3, alpha);
    }

    /**
     * 使用注册名对应的贴图绘制药水图标。
     *
     * @param mc 客户端实例。
     * @param x 绘制起点 X。
     * @param y 绘制起点 Y。
     * @param alpha 图标透明度。
     */
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
