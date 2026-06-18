package com.shiver.chestcavity.client;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.script.model.ScriptAbilityDefinition;
import com.shiver.chestcavity.script.registry.ScriptAbilityRegistry;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class CCAbilityWheel {

    private static final ResourceLocation[] DEFAULT_ABILITIES = {
            CCOrganScores.BUOYANT,
            CCOrganScores.FURNACE_POWERED,
            CCOrganScores.IRON_REPAIR,
            CCOrganScores.GRAZING,
            CCOrganScores.SILK,
            CCOrganScores.CREEPY,
            CCOrganScores.DRAGON_BOMBS,
            CCOrganScores.FORCEFUL_SPIT,
            CCOrganScores.PYROMANCY,
            CCOrganScores.GHASTLY,
            CCOrganScores.SHULKER_BULLETS
    };

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int RADIUS = 82;
    private static final int INNER_RADIUS = 24;
    private static final int SEGMENT_STEPS = 16;

    private static ResourceLocation selectedAbility;
    private static ResourceLocation hoveredAbility;
    private static boolean wheelOpenLastFrame;
    private static boolean wasMouseDown;
    private static double pointerX;
    private static double pointerY = -RADIUS;

    private static long lastFrameTime = 0;
    private static final java.util.Map<ResourceLocation, Double> hoverProgress = new java.util.HashMap<ResourceLocation, Double>();
    private static double wheelAlphaProgress = 0.0;
    private static double currentPointerX = 0;
    private static double currentPointerY = -RADIUS;

    public static ResourceLocation getSelectedAbility() {
        return selectedAbility;
    }

    private static final java.util.List<ResourceLocation> SCORE_ORDER = new java.util.ArrayList<ResourceLocation>();
    static {
        for (java.lang.reflect.Field field : com.shiver.chestcavity.registry.CCOrganScores.class.getDeclaredFields()) {
            if (field.getType() == ResourceLocation.class) {
                try {
                    SCORE_ORDER.add((ResourceLocation) field.get(null));
                } catch (Exception e) {}
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.showDebugInfo) {
            return;
        }

        if (!CCKeyBindings.abilityWheel.isKeyDown()) {
            if (wheelOpenLastFrame) {
                wheelOpenLastFrame = false;
                if (minecraft.currentScreen == null) {
                    minecraft.setIngameFocus();
                }
            }
            wheelAlphaProgress = 0.0;
            return;
        }

        ScaledResolution resolution = event.getResolution();
        int centerX = resolution.getScaledWidth() / 2;
        int centerY = resolution.getScaledHeight() / 2;
        List<ResourceLocation> availableAbilities = getAvailableAbilities(minecraft);

        if (!wheelOpenLastFrame) {
            if (minecraft.currentScreen == null) {
                minecraft.mouseHelper.ungrabMouseCursor();
                minecraft.inGameHasFocus = false;
            }
            wheelOpenLastFrame = true;
            wheelAlphaProgress = 0.0;
            lastFrameTime = Minecraft.getSystemTime();
            resetPointer(availableAbilities);
        } else {
            updatePointerAbsolute(minecraft, resolution, centerX, centerY);
        }

        updateAnimations(availableAbilities);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        double alphaScale = Math.min(1.0, wheelAlphaProgress);

        drawCircle(centerX, centerY, RADIUS + 5, 10, 10, 12, (int)(140 * alphaScale));

        if (availableAbilities.isEmpty()) {
            selectedAbility = null;
            drawCircle(centerX, centerY, INNER_RADIUS + 18, 50, 50, 50, (int)(180 * alphaScale));
            finishShapeRendering();
            drawCenteredStringWithAlpha(minecraft.fontRenderer, I18n.format("gui.chestcavity.no_abilities"), centerX, centerY - 4, 0xFFFFFF, alphaScale);
            drawScoreSummary(minecraft.fontRenderer, centerX, centerY, minecraft, resolution, alphaScale);
            return;
        }

        hoveredAbility = getAbilityAtPointer(availableAbilities);
        
        boolean isMouseDown = Mouse.isButtonDown(0);
        if (isMouseDown && !wasMouseDown) {
            if (hoveredAbility != null) {
                selectedAbility = hoveredAbility;
            }
        }
        wasMouseDown = isMouseDown;

        drawAbilitySegments(centerX, centerY, availableAbilities, alphaScale);
        
        drawCircle(centerX, centerY, INNER_RADIUS, 15, 15, 18, (int)(220 * alphaScale));
        
        drawCircle(centerX + (int) currentPointerX, centerY + (int) currentPointerY, 4, 255, 215, 0, (int)(255 * alphaScale));
        
        finishShapeRendering();
        
        drawAbilityLabels(minecraft.fontRenderer, centerX, centerY, availableAbilities, alphaScale);
        
        drawCenteredStringWithAlpha(minecraft.fontRenderer, getAbilityName(selectedAbility), centerX, centerY - 4, 0xFFD700, alphaScale);
        
        drawScoreSummary(minecraft.fontRenderer, centerX, centerY, minecraft, resolution, alphaScale);
    }

    private static void updateAnimations(List<ResourceLocation> abilities) {
        long currentTime = Minecraft.getSystemTime();
        if (lastFrameTime == 0) lastFrameTime = currentTime;
        double delta = (currentTime - lastFrameTime) / 1000.0;
        lastFrameTime = currentTime;
        
        if (delta > 0.1) delta = 0.1;

        wheelAlphaProgress = Math.min(1.0, wheelAlphaProgress + delta * 8.0);

        for (ResourceLocation ability : abilities) {
            double current = hoverProgress.containsKey(ability) ? hoverProgress.get(ability) : 0.0;
            if (ability.equals(hoveredAbility)) {
                current = Math.min(1.0, current + delta * 12.0);
            } else {
                current = Math.max(0.0, current - delta * 12.0);
            }
            hoverProgress.put(ability, current);
        }

        double lerpFactor = Math.min(1.0, delta * 20.0);
        currentPointerX += (pointerX - currentPointerX) * lerpFactor;
        currentPointerY += (pointerY - currentPointerY) * lerpFactor;
    }

    private static List<ResourceLocation> getAvailableAbilities(Minecraft minecraft) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(minecraft.player);
        List<ResourceLocation> result = new ArrayList<ResourceLocation>();
        if (chestCavity == null) {
            return result;
        }

        for (ResourceLocation ability : ActiveOrganAbilities.getRegisteredAbilityIds()) {
            if (chestCavity.getOrganScore(ability) > 0.0F) {
                result.add(ability);
            }
        }
        result.sort(new java.util.Comparator<ResourceLocation>() {
            @Override
            public int compare(ResourceLocation a, ResourceLocation b) {
                return Integer.compare(getAbilitySortOrder(a), getAbilitySortOrder(b));
            }
        });
        if (!result.contains(selectedAbility) && !result.isEmpty()) {
            selectedAbility = result.get(0);
        }
        return result;
    }

    private static void resetPointer(List<ResourceLocation> abilities) {
        int index = selectedAbility == null ? 0 : abilities.indexOf(selectedAbility);
        if (index < 0) {
            index = 0;
        }

        double angle = -Math.PI / 2.0D + index * TWO_PI / abilities.size();
        pointerX = Math.cos(angle) * (RADIUS - 20);
        pointerY = Math.sin(angle) * (RADIUS - 20);
        currentPointerX = pointerX;
        currentPointerY = pointerY;
    }

    private static void updatePointerAbsolute(Minecraft minecraft, ScaledResolution resolution, int centerX, int centerY) {
        int mouseX = Mouse.getX() * resolution.getScaledWidth() / minecraft.displayWidth;
        int mouseY = resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight() / minecraft.displayHeight - 1;
        
        pointerX = mouseX - centerX;
        pointerY = mouseY - centerY;

        double length = Math.sqrt(pointerX * pointerX + pointerY * pointerY);
        if (length < INNER_RADIUS) {
            if (length < 1.0D) {
                pointerX = 0.0D;
                pointerY = -INNER_RADIUS;
            } else {
                pointerX = pointerX / length * INNER_RADIUS;
                pointerY = pointerY / length * INNER_RADIUS;
            }
            return;
        }
        if (length > RADIUS) {
            pointerX = pointerX / length * RADIUS;
            pointerY = pointerY / length * RADIUS;
        }
    }

    private static ResourceLocation getAbilityAtPointer(List<ResourceLocation> abilities) {
        if (pointerX * pointerX + pointerY * pointerY < INNER_RADIUS * INNER_RADIUS) {
            return hoveredAbility == null ? (selectedAbility == null ? abilities.get(0) : selectedAbility) : hoveredAbility;
        }

        double step = TWO_PI / abilities.size();
        double normalized = normalizeAngle(Math.atan2(pointerY, pointerX) + Math.PI / 2.0D + step / 2.0D);
        return abilities.get((int) (normalized / step) % abilities.size());
    }

    private static void drawAbilitySegments(int centerX, int centerY, List<ResourceLocation> abilities, double alphaScale) {
        double step = TWO_PI / abilities.size();
        double gap = Math.toRadians(2.0);
        
        if (abilities.size() <= 1) {
            gap = 0;
        }

        for (int i = 0; i < abilities.size(); i++) {
            ResourceLocation ability = abilities.get(i);
            double mid = -Math.PI / 2.0D + i * step;
            double start = mid - step / 2.0D + gap / 2.0D;
            double end = mid + step / 2.0D - gap / 2.0D;
            
            double progress = hoverProgress.containsKey(ability) ? hoverProgress.get(ability) : 0.0;
            
            int currentOuterRadius = RADIUS + (int)(progress * 6);
            
            int baseR = 30, baseG = 30, baseB = 35;
            int selR = 255, selG = 215, selB = 0;
            
            int r = (int)(baseR + (selR - baseR) * progress);
            int g = (int)(baseG + (selG - baseG) * progress);
            int b = (int)(baseB + (selB - baseB) * progress);
            
            int innerR = (int)(r * 0.7);
            int innerG = (int)(g * 0.7);
            int innerB = (int)(b * 0.7);
            
            int baseAlpha = 140;
            int selAlpha = 210;
            int alpha = (int)((baseAlpha + (selAlpha - baseAlpha) * progress) * alphaScale);
            int innerAlpha = (int)(alpha * 0.8);

            drawWedgeWithGradient(centerX, centerY, INNER_RADIUS + 2, currentOuterRadius, start, end, 
                                  innerR, innerG, innerB, innerAlpha, 
                                  r, g, b, alpha);
        }
    }

    private static void drawAbilityLabels(FontRenderer fontRenderer, int centerX, int centerY, List<ResourceLocation> abilities, double alphaScale) {
        double step = TWO_PI / abilities.size();
        for (int i = 0; i < abilities.size(); i++) {
            ResourceLocation ability = abilities.get(i);
            double angle = -Math.PI / 2.0D + i * step;
            
            double progress = hoverProgress.containsKey(ability) ? hoverProgress.get(ability) : 0.0;
            int radiusOffset = (int)(progress * 6);
            
            int x = centerX + (int) (Math.cos(angle) * (RADIUS - 24 + radiusOffset));
            int y = centerY + (int) (Math.sin(angle) * (RADIUS - 24 + radiusOffset));
            
            int r = (int)(255);
            int g = (int)(255 - 40 * progress);
            int b = (int)(255 - 255 * progress);
            int color = (r << 16) | (g << 8) | b;
            
            drawCenteredStringWithAlpha(fontRenderer, getAbilityName(ability), x, y - 4, color, alphaScale);
        }
    }

    private static void drawCircle(int centerX, int centerY, int radius, int red, int green, int blue, int alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(centerX, centerY, 0).color(red, green, blue, alpha).endVertex();
        for (int i = 0; i <= 64; i++) {
            double angle = TWO_PI * i / 64.0D;
            buffer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, 0)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    private static void drawWedgeWithGradient(int centerX, int centerY, int innerRadius, int outerRadius,
                                              double start, double end, 
                                              int inR, int inG, int inB, int inA,
                                              int outR, int outG, int outB, int outA) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < SEGMENT_STEPS; i++) {
            double angle1 = start + (end - start) * i / SEGMENT_STEPS;
            double angle2 = start + (end - start) * (i + 1) / SEGMENT_STEPS;
            
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

    private static void finishShapeRendering() {
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawCenteredStringWithAlpha(FontRenderer fontRenderer, String text, int x, int y, int color, double alphaScale) {
        int alpha = (int)(255 * alphaScale);
        if (alpha <= 4) return;
        int finalColor = (color & 0x00FFFFFF) | (alpha << 24);
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2.0F, y, finalColor);
    }

    private static void drawStringWithAlpha(FontRenderer fontRenderer, String text, int x, int y, int color, double alphaScale) {
        int alpha = (int)(255 * alphaScale);
        if (alpha <= 4) return;
        int finalColor = (color & 0x00FFFFFF) | (alpha << 24);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        fontRenderer.drawStringWithShadow(text, x, y, finalColor);
    }

    private static void drawScoreSummary(FontRenderer fontRenderer, int centerX, int centerY, Minecraft minecraft, ScaledResolution resolution, double alphaScale) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(minecraft.player);
        if (chestCavity == null) return;
        
        java.util.Map<ResourceLocation, Float> rawScores = chestCavity.getOrganScores();
        java.util.List<java.util.Map.Entry<ResourceLocation, Float>> validScores = new java.util.ArrayList<java.util.Map.Entry<ResourceLocation, Float>>();
        for (java.util.Map.Entry<ResourceLocation, Float> entry : rawScores.entrySet()) {
            if (entry.getValue() > 0.0F) {
                validScores.add(entry);
            }
        }
        
        if (validScores.isEmpty()) return;
        
        validScores.sort(new java.util.Comparator<java.util.Map.Entry<ResourceLocation, Float>>() {
            @Override
            public int compare(java.util.Map.Entry<ResourceLocation, Float> a, java.util.Map.Entry<ResourceLocation, Float> b) {
                java.util.List<ResourceLocation> activeList = java.util.Arrays.asList(DEFAULT_ABILITIES);
                boolean activeA = activeList.contains(a.getKey());
                boolean activeB = activeList.contains(b.getKey());
                if (activeA != activeB) {
                    return activeA ? 1 : -1;
                }
                
                int indexA = SCORE_ORDER.indexOf(a.getKey());
                int indexB = SCORE_ORDER.indexOf(b.getKey());
                if (indexA == -1) indexA = 999;
                if (indexB == -1) indexB = 999;
                if (indexA != indexB) {
                    return Integer.compare(indexA, indexB);
                }
                
                return getScoreName(a.getKey()).compareTo(getScoreName(b.getKey()));
            }
        });
        
        int alpha = (int)(255 * alphaScale);
        if (alpha <= 4) return;
        int bgAlpha = (int)(140 * alphaScale);
        
        String title = "分数总结";
        int padding = 10;
        int lineSpacing = 12;
        
        int maxNameWidth = 0;
        int maxValWidth = 0;
        for (java.util.Map.Entry<ResourceLocation, Float> entry : validScores) {
            int w1 = fontRenderer.getStringWidth(getScoreName(entry.getKey()));
            int w2 = fontRenderer.getStringWidth(String.format("%.1f", entry.getValue()));
            if (w1 > maxNameWidth) maxNameWidth = w1;
            if (w2 > maxValWidth) maxValWidth = w2;
        }
        
        int columnWidth = maxNameWidth + maxValWidth + 15;
        int minColumnWidth = fontRenderer.getStringWidth(title) + 20;
        if (columnWidth < minColumnWidth) columnWidth = minColumnWidth;
        
        float scale = 1.0f;
        int panelWidth = 0;
        int panelHeight = 0;
        int startX = 0;
        int startY = 0;
        int titleSpace = 20;
        int maxRowsPerCol = 1;
        
        for (float s = 1.0f; s >= 0.5f; s -= 0.1f) {
            scale = s;
            int scaledScreenW = (int)(resolution.getScaledWidth() / s);
            int scaledScreenH = (int)(resolution.getScaledHeight() / s);
            int scaledCenterX = (int)(centerX / s);
            int scaledCenterY = (int)(centerY / s);
            int scaledRadius = (int)(RADIUS / s);
            
            int maxPanelHeight = scaledScreenH - (int)(40 / s);
            maxRowsPerCol = (maxPanelHeight - titleSpace - padding * 2) / lineSpacing;
            if (maxRowsPerCol < 1) maxRowsPerCol = 1;
            
            int numCols = (int) Math.ceil((double)validScores.size() / maxRowsPerCol);
            int rowsInFirstCol = Math.min(validScores.size(), maxRowsPerCol);
            
            panelWidth = padding * 2 + numCols * columnWidth;
            panelHeight = padding * 2 + titleSpace + rowsInFirstCol * lineSpacing;
            
            startX = scaledCenterX + scaledRadius + (int)(20 / s);
            if (startX + panelWidth <= scaledScreenW - 5) {
                startY = scaledCenterY - panelHeight / 2;
                break;
            }
            
            int leftStartX = scaledCenterX - scaledRadius - (int)(20 / s) - panelWidth;
            if (leftStartX >= 5) {
                startX = leftStartX;
                startY = scaledCenterY - panelHeight / 2;
                break;
            }
            
            if (s <= 0.51f) {
                startX = scaledScreenW - panelWidth - 5;
                if (startX < 5) startX = 5;
                startY = scaledCenterY - panelHeight / 2;
            }
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        
        net.minecraft.client.gui.Gui.drawRect(startX, startY, startX + panelWidth, startY + panelHeight, (bgAlpha << 24) | 0x1A1A1E);
        
        drawCenteredStringWithAlpha(fontRenderer, title, startX + panelWidth / 2, startY + padding, 0xFFD700, alphaScale);
        
        int curX = startX + padding;
        int curY = startY + padding + titleSpace;
        int rowCount = 0;
        
        for (java.util.Map.Entry<ResourceLocation, Float> entry : validScores) {
            String name = getScoreName(entry.getKey());
            String val = String.format("%.1f", entry.getValue());
            
            boolean isNegative = isNegativeScore(entry.getKey());
            int colorName = 0xCCCCCC;
            int colorVal = 0xFFFFFF;
            float floatVal = entry.getValue();
            if (floatVal > 1.05f) {
                colorVal = isNegative ? 0xFF5555 : 0x55FF55;
            } else if (floatVal < 0.95f) {
                colorVal = isNegative ? 0x55FF55 : 0xFF5555;
            }
            
            drawStringWithAlpha(fontRenderer, name, curX, curY, colorName, alphaScale);
            drawStringWithAlpha(fontRenderer, val, curX + columnWidth - fontRenderer.getStringWidth(val) - 10, curY, colorVal, alphaScale);
            
            curY += lineSpacing;
            rowCount++;
            if (rowCount >= maxRowsPerCol) {
                rowCount = 0;
                curX += columnWidth;
                curY = startY + padding + titleSpace;
            }
        }
        
        GlStateManager.popMatrix();
    }

    private static String getAbilityName(ResourceLocation id) {
        if (id == null) {
            return "";
        }
        ScriptAbilityDefinition definition = ScriptAbilityRegistry.get(id);
        if (definition != null) {
            if (definition.getDisplayName() != null && !definition.getDisplayName().isEmpty()) {
                return definition.getDisplayName();
            }
            if (definition.getTranslationKey() != null && I18n.hasKey(definition.getTranslationKey())) {
                return I18n.format(definition.getTranslationKey());
            }
        }
        return I18n.format("key." + id.getNamespace() + "." + id.getPath());
    }

    private static int getAbilitySortOrder(ResourceLocation id) {
        ScriptAbilityDefinition definition = ScriptAbilityRegistry.get(id);
        return definition == null ? 0 : definition.getSortOrder();
    }

    private static String getScoreName(ResourceLocation id) {
        if (id == null) return "";
        String key = "organscore." + id.getNamespace() + "." + id.getPath();
        if (I18n.hasKey(key)) {
            return I18n.format(key, "").trim();
        }
        String abilityKey = "key." + id.getNamespace() + "." + id.getPath();
        if (I18n.hasKey(abilityKey)) {
            return I18n.format(abilityKey);
        }
        return id.getPath();
    }

    private static boolean isNegativeScore(ResourceLocation id) {
        if (id == null) return false;
        String path = id.getPath();
        return path.equals("metabolism") || 
               path.equals("incompatibility") || 
               path.equals("hydroallergenic") || 
               path.equals("hydrophobia") || 
               path.equals("withered");
    }

    private static double normalizeAngle(double angle) {
        angle %= TWO_PI;
        return angle < 0.0D ? angle + TWO_PI : angle;
    }
}
