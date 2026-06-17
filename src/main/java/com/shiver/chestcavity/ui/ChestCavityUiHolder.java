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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class ChestCavityUiHolder implements IGuiHolder<ChestCavityGuiData> {

    @Override
    public ModularPanel buildUI(ChestCavityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.canInteractWith(player -> ChestCavityUiBridge.canKeepOpen(player, data));
        syncManager.onServerTick(() -> {
            if (!ChestCavityUiBridge.canKeepOpen(data.getPlayer(), data)) {
                data.getPlayer().closeScreen();
            }
        });

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(data.getTarget());
        IItemHandlerModifiable handler = chestCavity == null
                ? new ItemStackHandler(ChestCavityUiBridge.CHEST_CAVITY_SLOTS)
                : chestCavity.getOrganInventory();

        ModularPanel panel = ModularPanel.defaultPanel(ChestCavityUiBridge.PANEL_ID, 176, 168)
                .child(IKey.lang("container.chestcavity.chest_cavity").asWidget().pos(8, 6));

        for (int slot = 0; slot < ChestCavityUiBridge.CHEST_CAVITY_SLOTS; slot++) {
            int x = 8 + (slot % ChestCavityUiBridge.SLOTS_PER_ROW) * 18;
            int y = 18 + (slot / ChestCavityUiBridge.SLOTS_PER_ROW) * 18;
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

        return panel.bindPlayerInventory();
    }
}
