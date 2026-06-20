package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class ChestCavityUiHolder implements IGuiHolder<ChestCavityGuiData> {

    @Override
    public ModularPanel buildUI(ChestCavityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.canInteractWith(player -> ChestCavityUi.canKeepOpen(player, data));
        syncManager.onServerTick(() -> {
            if (!ChestCavityUi.canKeepOpen(data.getPlayer(), data)) {
                data.getPlayer().closeScreen();
            }
        });

        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(data.getTarget());
        ChestLayoutDef layout = ChestCavityUi.getLayout(chestCavity);
        IItemHandlerModifiable handler = chestCavity == null
                ? new ItemStackHandler(layout.getSlotCount())
                : chestCavity.getOrganInventory();

        ModularPanel panel = ModularPanel.defaultPanel(ChestCavityUi.PANEL_ID, layout.getPanelWidth(), layout.getPanelHeight())
                .child(IKey.lang("container.chestcavity.chest_cavity").asWidget().pos(layout.getTitleX(), layout.getTitleY()));

        for (int slot = 0; slot < layout.getSlotCount(); slot++) {
            boolean forbidden = chestCavity != null && ChestCavityTypeUtil.isSlotForbidden(chestCavity, slot);
            ModularSlot modularSlot = new ModularSlot(handler, slot)
                    .canPut(!forbidden)
                    .canTake(!forbidden)
                    .canDragInto(!forbidden);
            if (forbidden) {
                modularSlot.setEnabled(false);
            }
            panel.child(ItemSlot.create(false).slot(modularSlot).pos(layout.getSlotX(slot), layout.getSlotY(slot)));
        }

        return panel.bindPlayerInventory();
    }
}
