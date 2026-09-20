package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 负责构建胸腔界面的 ModularUI 面板内容。
 * 支持标准 9x3 手绘整图与任意尺寸的切片自适应界面。
 */
public class ChestCavityUiHolder implements IGuiHolder<ChestCavityGuiData> {

    public static final ResourceLocation TEXTURE_9X3 = new ResourceLocation(Tags.MOD_ID, "textures/gui/chest_cavity.png");
    public static final ResourceLocation TEXTURE_PANEL = new ResourceLocation(Tags.MOD_ID, "textures/gui/chest_cavity_panel.png");
    public static final ResourceLocation TEXTURE_DIVIDER = new ResourceLocation(Tags.MOD_ID, "textures/gui/chest_cavity_divider.png");

    /**
     * 标准 9x3 胸腔完整手绘背景贴图。
     */
    private static final UITexture MASTER_TEXTURE_9X3 = new UITexture(
            TEXTURE_9X3,
            0.0F,
            0.0F,
            181.0F / 256.0F,
            171.0F / 256.0F,
            null,
            true
    ) {
        @Override
        public void draw(float x, float y, float width, float height) {
            super.draw(x, y, 181.0F, 171.0F);
        }
    };

    /**
     * 自适应底板（四角保持 1:1，四边拉伸，内部平铺暗红底色）。
     */
    private static final AdaptableUITexture ADAPTABLE_PANEL = (AdaptableUITexture) UITexture.builder()
            .location(TEXTURE_PANEL)
            .imageSize(256, 256)
            .xy(0, 0, 176, 166)
            .adaptable(7, 17, 7, 7)
            .build();

    /**
     * 中间自适应分割条。
     */
    private static final UITexture DIVIDER_TEXTURE = UITexture.builder()
            .location(TEXTURE_DIVIDER)
            .tiled(162, 12)
            .fullImage()
            .build();

    /**
     * 玩家背包与快捷栏完整贴图（来自原图 162x76，包含 3 行背包 + 4px 间隙 + 1 行快捷栏）。
     * 避免在任意尺寸自适应时对 36 个槽位重复平铺单格背景。
     */
    public static final UITexture PLAYER_INVENTORY_TEXTURE = UITexture.builder()
            .location(TEXTURE_9X3)
            .imageSize(256, 256)
            .xy(7, 83, 162, 76)
            .build();

    /**
     * 原图 9x3 区域基准坐标。
     */
    public static final int ORGAN_TEXTURE_ORIGIN_X = 7;
    public static final int ORGAN_TEXTURE_ORIGIN_Y = 17;

    /**
     * 原图 9x3 槽位单格贴图切片（9 列 x 3 行，共 27 种手绘无缝单格底纹）。
     */
    private static final UITexture[][] ORGAN_SLOT_TEXTURES = new UITexture[9][3];

    /**
     * 常用行列数（1~9 列，1~3 行）从原图居中裁剪的连续网格贴图缓存。
     */
    private static final UITexture[][] ORGAN_GRID_TEXTURES = new UITexture[10][4];

    static {
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 3; r++) {
                ORGAN_SLOT_TEXTURES[c][r] = UITexture.builder()
                        .location(TEXTURE_9X3)
                        .imageSize(256, 256)
                        .xy(ORGAN_TEXTURE_ORIGIN_X + c * 18, ORGAN_TEXTURE_ORIGIN_Y + r * 18, 18, 18)
                        .build();
            }
        }

        for (int c = 1; c <= 9; c++) {
            for (int r = 1; r <= 3; r++) {
                int cropX = calculateOrganCropX(c);
                int cropY = calculateOrganCropY();
                ORGAN_GRID_TEXTURES[c][r] = UITexture.builder()
                        .location(TEXTURE_9X3)
                        .imageSize(256, 256)
                        .xy(cropX, cropY, c * 18, r * 18)
                        .build();
            }
        }
    }

    /**
     * 计算指定列数在原图 9x3 区域中居中裁剪的起始 X 像素坐标。
     *
     * @param columns 网格列数。
     * @return 原图裁剪起始 X 坐标。
     */
    public static int calculateOrganCropX(int columns) {
        if (columns >= 9) {
            return ORGAN_TEXTURE_ORIGIN_X;
        }
        int colOffset = (9 - columns) / 2;
        return ORGAN_TEXTURE_ORIGIN_X + colOffset * 18;
    }

    /**
     * 计算在原图 9x3 区域中裁剪的起始 Y 像素坐标。
     *
     * @return 原图裁剪起始 Y 坐标（固定为 17）。
     */
    public static int calculateOrganCropY() {
        return ORGAN_TEXTURE_ORIGIN_Y;
    }

    /**
     * 获取指定行列尺寸的器官整体拼接背景贴图（适用于 columns <= 9 且 rows <= 3）。
     *
     * @param columns 网格列数。
     * @param rows 网格行数。
     * @return 居中裁剪的连续器官底图。
     */
    public static UITexture getOrganGridTexture(int columns, int rows) {
        int c = Math.max(1, Math.min(columns, 9));
        int r = Math.max(1, Math.min(rows, 3));
        return ORGAN_GRID_TEXTURES[c][r];
    }

    /**
     * 获取指定单格坐标在 9x3 手绘模板中对应的单槽贴图。
     *
     * @param col 列索引。
     * @param row 行索引。
     * @return 对应的单格贴图。
     */
    public static UITexture getOrganSlotTexture(int col, int row) {
        int c = Math.floorMod(col, 9);
        int r = Math.floorMod(row, 3);
        return ORGAN_SLOT_TEXTURES[c][r];
    }

    /**
     * 获取适用于任意行列尺寸的器官网格背景 Drawable。
     * 当尺寸在 9x3 以内时直接返回单张连续裁剪贴图；超出时采用无缝循环平铺。
     *
     * @param columns 网格列数。
     * @param rows 网格行数。
     * @return 可绘制的器官网格背景。
     */
    public static IDrawable getOrganGridDrawable(int columns, int rows) {
        if (columns <= 9 && rows <= 3) {
            return getOrganGridTexture(columns, rows);
        }
        return new TiledOrganGridDrawable(columns, rows);
    }

    public static boolean isStandardLayout(int columns, int rows) {
        return columns == ChestCavityUiBridge.DEFAULT_SLOTS_PER_ROW && rows == ChestCavityUiBridge.DEFAULT_ROWS;
    }

    public static int calculatePanelWidth(int columns) {
        return Math.max(176, 14 + columns * 18);
    }

    public static int calculatePanelHeight(int rows) {
        return 112 + rows * 18;
    }

    public static int calculateStartX(int panelWidth, int columns) {
        return (panelWidth - columns * 18) / 2;
    }

    public static int calculateStartY() {
        return 17;
    }

    public static int calculateDividerY(int rows) {
        return 17 + rows * 18;
    }

    public static int calculateDividerWidth(int columns) {
        return Math.max(columns * 18, 162);
    }

    /**
     * 根据目标实体的胸腔数据构建交互界面。
     *
     * @param data 界面同步数据。
     * @param syncManager 面板同步管理器。
     * @param settings 界面设置。
     * @return 构建好的胸腔界面面板。
     */
    @Override
    public ModularPanel buildUI(ChestCavityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.canInteractWith(player -> ChestCavityUiBridge.canKeepOpen(player, data));
        syncManager.onServerTick(() -> {
            if (!ChestCavityUiBridge.canKeepOpen(data.getPlayer(), data)) {
                data.getPlayer().closeScreen();
            }
        });

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(data.getTarget());
        ChestCavityType type = chestCavity != null ? ChestCavityHelper.getChestCavityType(chestCavity) : null;
        int columns = data.getColumns() > 0 ? data.getColumns() : (type != null ? type.getColumns() : ChestCavityUiBridge.DEFAULT_SLOTS_PER_ROW);
        int rows = data.getRows() > 0 ? data.getRows() : (type != null ? type.getRows() : ChestCavityUiBridge.DEFAULT_ROWS);
        int slotCount = columns * rows;

        IItemHandlerModifiable handler = chestCavity == null
                ? new ItemStackHandler(slotCount)
                : chestCavity.getOrganInventory();

        boolean standardLayout = isStandardLayout(columns, rows);

        int slotGridWidth = columns * 18;
        int slotGridHeight = rows * 18;
        int panelWidth = calculatePanelWidth(columns);
        int panelHeight = calculatePanelHeight(rows);
        int startX = calculateStartX(panelWidth, columns);
        int startY = calculateStartY();

        ModularPanel panel = ModularPanel.defaultPanel(ChestCavityUiBridge.PANEL_ID, panelWidth, panelHeight)
                .disableThemeBackground(true);

        if (standardLayout) {
            panel.background(MASTER_TEXTURE_9X3);
        } else {
            panel.background(ADAPTABLE_PANEL);

            // 自适应器官网格背景（根据格子尺寸从原图居中裁剪或平铺拼接纹理）
            IDrawable organBackground = getOrganGridDrawable(columns, rows);
            panel.child(organBackground.asWidget().pos(startX, startY).size(slotGridWidth, slotGridHeight));

            // 自适应中间分割条
            int dividerX = Math.min(startX, (panelWidth - 162) / 2);
            int dividerWidth = calculateDividerWidth(columns);
            int dividerY = calculateDividerY(rows);
            panel.child(DIVIDER_TEXTURE.asWidget().pos(dividerX, dividerY).size(dividerWidth, 12));
        }

        // 标题文字
        panel.child(IKey.lang("container.chestcavity.chest_cavity")
                .color(0xE0D8D0)
                .shadow(true)
                .asWidget()
                .pos(8, 6));

        // 胸腔器官槽位
        for (int slot = 0; slot < slotCount; slot++) {
            int col = slot % columns;
            int row = slot / columns;
            int x = startX + col * 18;
            int y = startY + row * 18;
            boolean forbidden = chestCavity != null && ChestCavityHelper.isSlotForbidden(chestCavity, slot);
            ModularSlot modularSlot = new ModularSlot(handler, slot)
                    .canPut(!forbidden)
                    .canTake(!forbidden)
                    .canDragInto(!forbidden);
            if (forbidden) {
                modularSlot.setEnabled(false);
            }
            ItemSlot itemSlot = ItemSlot.create(false)
                    .slot(modularSlot)
                    .pos(x, y)
                    .disableThemeBackground(true);
            panel.child(itemSlot);
        }

        // 玩家背包与快捷栏槽位
        SlotGroupWidget playerInv = SlotGroupWidget.playerInventory(7, true, (index, slot) -> {
            slot.disableThemeBackground(true);
            return slot;
        });
        playerInv.disableThemeBackground(true);
        if (!standardLayout) {
            playerInv.background(PLAYER_INVENTORY_TEXTURE);
        }
        panel.child(playerInv);

        return panel;
    }

    /**
     * 当器官槽位尺寸超出原图 9x3 时，通过 27 格无缝贴图循环平铺渲染，
     */
    public static class TiledOrganGridDrawable implements IDrawable {
        private final int columns;
        private final int rows;

        public TiledOrganGridDrawable(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }

        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme theme) {
            for (int col = 0; col < columns; col++) {
                for (int row = 0; row < rows; row++) {
                    ORGAN_SLOT_TEXTURES[col % 9][row % 3].draw(context, x + col * 18, y + row * 18, 18, 18, theme);
                }
            }
        }

        @Override
        public int getDefaultWidth() {
            return columns * 18;
        }

        @Override
        public int getDefaultHeight() {
            return rows * 18;
        }
    }
}
