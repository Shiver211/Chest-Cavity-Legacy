package com.shiver.chestcavity.client.abilitywheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * 负责能力轮盘本体、扇区和文字的渲染。
 */
final class AbilityWheelRenderer {

    /**
     * 绘制“没有可用能力”时的空轮盘界面。
     *
     * @param minecraft 客户端实例。
     * @param centerX 轮盘中心 X。
     * @param centerY 轮盘中心 Y。
     * @param alphaScale 当前透明度缩放。
     */
    void drawEmpty(Minecraft minecraft, int centerX, int centerY, double alphaScale) {
        beginShapeRendering();
        drawCircle(centerX, centerY, AbilityWheelConstants.RADIUS + 5, 10, 10, 12, (int) (140 * alphaScale));
        drawCircle(centerX, centerY, AbilityWheelConstants.INNER_RADIUS + 18, 50, 50, 50, (int) (180 * alphaScale));
        finishShapeRendering();
        drawCenteredStringWithAlpha(minecraft.fontRenderer,
                I18n.format("gui.chestcavity.no_abilities"), centerX, centerY - 4, 0xFFFFFF, alphaScale);
    }

    /**
     * 绘制包含能力扇区、指针与名称的完整轮盘。
     *
     * @param minecraft 客户端实例。
     * @param centerX 轮盘中心 X。
     * @param centerY 轮盘中心 Y。
     * @param abilities 当前可用能力列表。
     * @param state 轮盘状态。
     * @param alphaScale 当前透明度缩放。
     */
    void drawAbilities(Minecraft minecraft, int centerX, int centerY,
                       List<String> abilities, AbilityWheelState state, double alphaScale) {
        beginShapeRendering();
        drawCircle(centerX, centerY, AbilityWheelConstants.RADIUS + 5, 10, 10, 12, (int) (140 * alphaScale));
        drawAbilitySegments(centerX, centerY, abilities, state, alphaScale);
        drawCircle(centerX, centerY, AbilityWheelConstants.INNER_RADIUS, 15, 15, 18, (int) (220 * alphaScale));
        drawCircle(centerX + (int) state.getCurrentPointerX(), centerY + (int) state.getCurrentPointerY(),
                4, 255, 215, 0, (int) (255 * alphaScale));
        finishShapeRendering();

        drawAbilityLabels(minecraft.fontRenderer, centerX, centerY, abilities, state, alphaScale);
        drawCenteredStringWithAlpha(minecraft.fontRenderer, AbilityWheelText.getAbilityName(state.getSelectedAbility()),
                centerX, centerY - 4, 0xFFD700, alphaScale);
    }

    /**
     * 准备形状渲染所需的 OpenGL 状态。
     */
    private static void beginShapeRendering() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
    }

    /**
     * 绘制能力轮盘的所有扇区，并根据悬停状态应用渐变和高亮。
     *
     * @param centerX 轮盘中心 X。
     * @param centerY 轮盘中心 Y。
     * @param abilities 当前可用能力列表。
     * @param state 轮盘状态。
     * @param alphaScale 当前透明度缩放。
     */
    private static void drawAbilitySegments(int centerX, int centerY,
                                            List<String> abilities, AbilityWheelState state, double alphaScale) {
        double step = AbilityWheelConstants.TWO_PI / abilities.size();
        double gap = Math.toRadians(2.0D);

        if (abilities.size() <= 1) {
            gap = 0.0D;
        }

        for (int i = 0; i < abilities.size(); i++) {
            String ability = abilities.get(i);
            double mid = -Math.PI / 2.0D + i * step;
            double start = mid - step / 2.0D + gap / 2.0D;
            double end = mid + step / 2.0D - gap / 2.0D;

            double progress = state.getHoverProgress(ability);
            int currentOuterRadius = AbilityWheelConstants.RADIUS + (int) (progress * 6);

            int baseR = 30;
            int baseG = 30;
            int baseB = 35;
            int selR = 255;
            int selG = 215;
            int selB = 0;

            int r = (int) (baseR + (selR - baseR) * progress);
            int g = (int) (baseG + (selG - baseG) * progress);
            int b = (int) (baseB + (selB - baseB) * progress);

            int innerR = (int) (r * 0.7D);
            int innerG = (int) (g * 0.7D);
            int innerB = (int) (b * 0.7D);

            int baseAlpha = 140;
            int selAlpha = 210;
            int alpha = (int) ((baseAlpha + (selAlpha - baseAlpha) * progress) * alphaScale);
            int innerAlpha = (int) (alpha * 0.8D);

            drawWedgeWithGradient(centerX, centerY, AbilityWheelConstants.INNER_RADIUS + 2, currentOuterRadius,
                    start, end, innerR, innerG, innerB, innerAlpha, r, g, b, alpha);
        }
    }

    /**
     * 绘制每个能力扇区的名称标签。
     *
     * @param fontRenderer 字体渲染器。
     * @param centerX 轮盘中心 X。
     * @param centerY 轮盘中心 Y。
     * @param abilities 当前可用能力列表。
     * @param state 轮盘状态。
     * @param alphaScale 当前透明度缩放。
     */
    private static void drawAbilityLabels(FontRenderer fontRenderer, int centerX, int centerY,
                                          List<String> abilities, AbilityWheelState state, double alphaScale) {
        double step = AbilityWheelConstants.TWO_PI / abilities.size();
        for (int i = 0; i < abilities.size(); i++) {
            String ability = abilities.get(i);
            double angle = -Math.PI / 2.0D + i * step;

            double progress = state.getHoverProgress(ability);
            int radiusOffset = (int) (progress * 6);

            int x = centerX + (int) (Math.cos(angle) * (AbilityWheelConstants.RADIUS - 24 + radiusOffset));
            int y = centerY + (int) (Math.sin(angle) * (AbilityWheelConstants.RADIUS - 24 + radiusOffset));

            int r = 255;
            int g = (int) (255 - 40 * progress);
            int b = (int) (255 - 255 * progress);
            int color = (r << 16) | (g << 8) | b;

            drawCenteredStringWithAlpha(fontRenderer, AbilityWheelText.getAbilityName(ability), x, y - 4, color, alphaScale);
        }
    }

    /**
     * 绘制一个纯色圆形。
     *
     * @param centerX 圆心 X。
     * @param centerY 圆心 Y。
     * @param radius 半径。
     * @param red 红色分量。
     * @param green 绿色分量。
     * @param blue 蓝色分量。
     * @param alpha 透明度。
     */
    private static void drawCircle(int centerX, int centerY, int radius, int red, int green, int blue, int alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(centerX, centerY, 0).color(red, green, blue, alpha).endVertex();
        for (int i = 0; i <= 128; i++) {
            double angle = AbilityWheelConstants.TWO_PI * i / 128.0D;
            buffer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, 0)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    /**
     * 绘制一个带内外渐变的扇形区域。
     *
     * @param centerX 圆心 X。
     * @param centerY 圆心 Y。
     * @param innerRadius 内半径。
     * @param outerRadius 外半径。
     * @param start 起始角度。
     * @param end 结束角度。
     * @param inR 内圈红色分量。
     * @param inG 内圈绿色分量。
     * @param inB 内圈蓝色分量。
     * @param inA 内圈透明度。
     * @param outR 外圈红色分量。
     * @param outG 外圈绿色分量。
     * @param outB 外圈蓝色分量。
     * @param outA 外圈透明度。
     */
    private static void drawWedgeWithGradient(int centerX, int centerY, int innerRadius, int outerRadius,
                                              double start, double end,
                                              int inR, int inG, int inB, int inA,
                                              int outR, int outG, int outB, int outA) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int steps = Math.max(4, (int) Math.ceil(128.0D * (end - start) / AbilityWheelConstants.TWO_PI));
        for (int i = 0; i < steps; i++) {
            double angle1 = start + (end - start) * i / steps;
            double angle2 = start + (end - start) * (i + 1) / steps;

            buffer.pos(centerX + Math.cos(angle1) * outerRadius, centerY + Math.sin(angle1) * outerRadius, 0)
                    .color(outR, outG, outB, outA)
                    .endVertex();
            buffer.pos(centerX + Math.cos(angle1) * innerRadius, centerY + Math.sin(angle1) * innerRadius, 0)
                    .color(inR, inG, inB, inA)
                    .endVertex();
            buffer.pos(centerX + Math.cos(angle2) * innerRadius, centerY + Math.sin(angle2) * innerRadius, 0)
                    .color(inR, inG, inB, inA)
                    .endVertex();
            buffer.pos(centerX + Math.cos(angle2) * outerRadius, centerY + Math.sin(angle2) * outerRadius, 0)
                    .color(outR, outG, outB, outA)
                    .endVertex();
        }
        tessellator.draw();
    }

    /**
     * 恢复形状渲染前的 OpenGL 状态。
     */
    private static void finishShapeRendering() {
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * 以指定透明度绘制居中文字。
     *
     * @param fontRenderer 字体渲染器。
     * @param text 文本内容。
     * @param x 绘制中心 X。
     * @param y 绘制位置 Y。
     * @param color RGB 颜色值。
     * @param alphaScale 透明度缩放。
     */
    static void drawCenteredStringWithAlpha(FontRenderer fontRenderer, String text,
                                            int x, int y, int color, double alphaScale) {
        int alpha = (int) (255 * alphaScale);
        if (alpha <= 4) {
            return;
        }
        int finalColor = (color & 0x00FFFFFF) | (alpha << 24);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2.0F, y, finalColor);
    }

    /**
     * 以指定透明度绘制普通左对齐文字。
     *
     * @param fontRenderer 字体渲染器。
     * @param text 文本内容。
     * @param x 绘制位置 X。
     * @param y 绘制位置 Y。
     * @param color RGB 颜色值。
     * @param alphaScale 透明度缩放。
     */
    static void drawStringWithAlpha(FontRenderer fontRenderer, String text,
                                    int x, int y, int color, double alphaScale) {
        int alpha = (int) (255 * alphaScale);
        if (alpha <= 4) {
            return;
        }
        int finalColor = (color & 0x00FFFFFF) | (alpha << 24);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        fontRenderer.drawStringWithShadow(text, x, y, finalColor);
    }
}
