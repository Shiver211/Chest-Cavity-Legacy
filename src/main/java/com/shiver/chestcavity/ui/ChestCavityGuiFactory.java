package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public final class ChestCavityGuiFactory extends AbstractUIFactory<ChestCavityGuiData> {

    public static final ChestCavityGuiFactory INSTANCE = new ChestCavityGuiFactory();

    private static boolean registered;

    private ChestCavityGuiFactory() {
        super(ChestCavityUi.FACTORY_ID);
    }

    public static void register() {
        if (!registered) {
            GuiManager.registerFactory(INSTANCE);
            registered = true;
        }
    }

    public static void open(EntityPlayerMP player, ChestCavityGuiData data) {
        register();
        GuiManager.open(INSTANCE, data, player);
    }

    @Override
    public IGuiHolder<ChestCavityGuiData> getGuiHolder(ChestCavityGuiData data) {
        return new ChestCavityUiHolder();
    }

    @Override
    public void writeGuiData(ChestCavityGuiData data, PacketBuffer buffer) {
        buffer.writeInt(data.getTargetEntityId());
    }

    @Override
    public ChestCavityGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new ChestCavityGuiData(player, buffer.readInt());
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, ChestCavityGuiData data) {
        return ChestCavityUi.canKeepOpen(player, data);
    }
}
