package com.shiver.chestcavity.client;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class ScoreSummaryRenderer {

    private static final List<String> ACTIVE_ABILITIES = Arrays.asList(AbilityWheelConstants.ABILITIES);
    private static final List<String> SCORE_ORDER = new ArrayList<String>();

    static {
        for (java.lang.reflect.Field field : CCOrganScores.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                try {
                    SCORE_ORDER.add((String) field.get(null));
                } catch (Exception ignored) {
                }
            }
        }
    }

    void draw(FontRenderer fontRenderer, int centerX, int centerY,
              Minecraft minecraft, ScaledResolution resolution, double alphaScale) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(minecraft.player);
        if (chestCavity == null) {
            return;
        }

        List<Map.Entry<String, Float>> validScores = getValidScores(chestCavity);
        if (validScores.isEmpty()) {
            return;
        }
        validScores.sort(new ScoreEntryComparator());

        int alpha = (int) (255 * alphaScale);
        if (alpha <= 4) {
            return;
        }

        int bgAlpha = (int) (140 * alphaScale);
        String title = I18n.format("gui.chestcavity.score_summary");
        int padding = 10;
        int lineSpacing = 12;

        int maxNameWidth = 0;
        int maxValWidth = 0;
        for (Map.Entry<String, Float> entry : validScores) {
            int nameWidth = fontRenderer.getStringWidth(AbilityWheelText.getScoreName(entry.getKey()));
            int valueWidth = fontRenderer.getStringWidth(String.format("%.1f", entry.getValue()));
            if (nameWidth > maxNameWidth) {
                maxNameWidth = nameWidth;
            }
            if (valueWidth > maxValWidth) {
                maxValWidth = valueWidth;
            }
        }

        int columnWidth = maxNameWidth + maxValWidth + 15;
        int minColumnWidth = fontRenderer.getStringWidth(title) + 20;
        if (columnWidth < minColumnWidth) {
            columnWidth = minColumnWidth;
        }

        PanelLayout layout = fitPanel(resolution, centerX, centerY, validScores.size(), padding, lineSpacing, columnWidth);

        GlStateManager.pushMatrix();
        GlStateManager.scale(layout.scale, layout.scale, 1.0F);

        Gui.drawRect(layout.startX, layout.startY,
                layout.startX + layout.panelWidth, layout.startY + layout.panelHeight,
                (bgAlpha << 24) | 0x1A1A1E);

        AbilityWheelRenderer.drawCenteredStringWithAlpha(fontRenderer, title,
                layout.startX + layout.panelWidth / 2, layout.startY + padding, 0xFFD700, alphaScale);

        int curX = layout.startX + padding;
        int curY = layout.startY + padding + layout.titleSpace;
        int rowCount = 0;

        for (Map.Entry<String, Float> entry : validScores) {
            String name = AbilityWheelText.getScoreName(entry.getKey());
            String value = String.format("%.1f", entry.getValue());

            int colorName = 0xCCCCCC;
            int colorValue = getScoreValueColor(entry.getKey(), entry.getValue());

            AbilityWheelRenderer.drawStringWithAlpha(fontRenderer, name, curX, curY, colorName, alphaScale);
            AbilityWheelRenderer.drawStringWithAlpha(fontRenderer, value,
                    curX + columnWidth - fontRenderer.getStringWidth(value) - 10, curY, colorValue, alphaScale);

            curY += lineSpacing;
            rowCount++;
            if (rowCount >= layout.maxRowsPerCol) {
                rowCount = 0;
                curX += columnWidth;
                curY = layout.startY + padding + layout.titleSpace;
            }
        }

        GlStateManager.popMatrix();
    }

    private static List<Map.Entry<String, Float>> getValidScores(IChestCavity chestCavity) {
        Map<String, Float> rawScores = chestCavity.getOrganScores();
        List<Map.Entry<String, Float>> validScores = new ArrayList<Map.Entry<String, Float>>();
        for (Map.Entry<String, Float> entry : rawScores.entrySet()) {
            if (entry.getValue() > 0.0F) {
                validScores.add(entry);
            }
        }
        return validScores;
    }

    private static int getScoreValueColor(String scoreId, float value) {
        boolean isNegative = AbilityWheelText.isNegativeScore(scoreId);
        if (value > 1.05F) {
            return isNegative ? 0xFF5555 : 0x55FF55;
        }
        if (value < 0.95F) {
            return isNegative ? 0x55FF55 : 0xFF5555;
        }
        return 0xFFFFFF;
    }

    private static PanelLayout fitPanel(ScaledResolution resolution, int centerX, int centerY,
                                        int scoreCount, int padding, int lineSpacing, int columnWidth) {
        int titleSpace = 20;
        PanelLayout layout = new PanelLayout();
        layout.titleSpace = titleSpace;

        for (float scale = 1.0F; scale >= 0.5F; scale -= 0.1F) {
            int scaledScreenW = (int) (resolution.getScaledWidth() / scale);
            int scaledScreenH = (int) (resolution.getScaledHeight() / scale);
            int scaledCenterX = (int) (centerX / scale);
            int scaledCenterY = (int) (centerY / scale);
            int scaledRadius = (int) (AbilityWheelConstants.RADIUS / scale);

            int maxPanelHeight = scaledScreenH - (int) (40 / scale);
            int maxRowsPerCol = (maxPanelHeight - titleSpace - padding * 2) / lineSpacing;
            if (maxRowsPerCol < 1) {
                maxRowsPerCol = 1;
            }

            int numCols = (int) Math.ceil((double) scoreCount / maxRowsPerCol);
            int rowsInFirstCol = Math.min(scoreCount, maxRowsPerCol);
            int panelWidth = padding * 2 + numCols * columnWidth;
            int panelHeight = padding * 2 + titleSpace + rowsInFirstCol * lineSpacing;

            int startX = scaledCenterX + scaledRadius + (int) (20 / scale);
            int startY = scaledCenterY - panelHeight / 2;
            if (startX + panelWidth <= scaledScreenW - 5) {
                return layout.set(scale, panelWidth, panelHeight, startX, startY, maxRowsPerCol);
            }

            int leftStartX = scaledCenterX - scaledRadius - (int) (20 / scale) - panelWidth;
            if (leftStartX >= 5) {
                return layout.set(scale, panelWidth, panelHeight, leftStartX, startY, maxRowsPerCol);
            }

            if (scale <= 0.51F) {
                startX = scaledScreenW - panelWidth - 5;
                if (startX < 5) {
                    startX = 5;
                }
                return layout.set(scale, panelWidth, panelHeight, startX, startY, maxRowsPerCol);
            }
        }

        return layout;
    }

    private static final class ScoreEntryComparator implements Comparator<Map.Entry<String, Float>> {
        @Override
        public int compare(Map.Entry<String, Float> a, Map.Entry<String, Float> b) {
            boolean activeA = ACTIVE_ABILITIES.contains(a.getKey());
            boolean activeB = ACTIVE_ABILITIES.contains(b.getKey());
            if (activeA != activeB) {
                return activeA ? 1 : -1;
            }

            int indexA = SCORE_ORDER.indexOf(a.getKey());
            int indexB = SCORE_ORDER.indexOf(b.getKey());
            if (indexA == -1) {
                indexA = 999;
            }
            if (indexB == -1) {
                indexB = 999;
            }
            if (indexA != indexB) {
                return Integer.compare(indexA, indexB);
            }

            return AbilityWheelText.getScoreName(a.getKey()).compareTo(AbilityWheelText.getScoreName(b.getKey()));
        }
    }

    private static final class PanelLayout {
        private float scale = 1.0F;
        private int panelWidth;
        private int panelHeight;
        private int startX;
        private int startY;
        private int titleSpace;
        private int maxRowsPerCol = 1;

        private PanelLayout set(float scale, int panelWidth, int panelHeight, int startX, int startY, int maxRowsPerCol) {
            this.scale = scale;
            this.panelWidth = panelWidth;
            this.panelHeight = panelHeight;
            this.startX = startX;
            this.startY = startY;
            this.maxRowsPerCol = maxRowsPerCol;
            return this;
        }
    }
}
