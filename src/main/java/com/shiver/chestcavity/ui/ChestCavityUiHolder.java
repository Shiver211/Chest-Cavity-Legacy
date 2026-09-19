package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 负责构建胸腔界面的 ModularUI 面板内容。
 */
public class ChestCavityUiHolder implements IGuiHolder<ChestCavityGuiData> {

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

        int slotGridWidth = columns * 18;
        int panelWidth = Math.max(176, 14 + slotGridWidth);
        int panelHeight = 114 + rows * 18;
        int startX = Math.max(8, (panelWidth - slotGridWidth) / 2);
        int startY = 18;

        ModularPanel panel = ModularPanel.defaultPanel(ChestCavityUiBridge.PANEL_ID, panelWidth, panelHeight)
                .child(IKey.lang("container.chestcavity.chest_cavity").asWidget().pos(8, 6));

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
            panel.child(ItemSlot.create(false).slot(modularSlot).pos(x, y));
        }

        return panel.bindPlayerInventory(7);
    }
}
