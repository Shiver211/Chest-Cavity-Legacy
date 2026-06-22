package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

/**
 * 负责胸腔界面的 ModularUI 工厂注册与数据编解码。
 */
public final class ChestCavityGuiFactory extends AbstractUIFactory<ChestCavityGuiData> {

    public static final ChestCavityGuiFactory INSTANCE = new ChestCavityGuiFactory();

    private static boolean registered;

    /**
     * 创建胸腔界面工厂并绑定固定工厂标识。
     */
    private ChestCavityGuiFactory() {
        super(ChestCavityUiBridge.FACTORY_ID);
    }

    /**
     * 把界面工厂注册到 ModularUI。
     */
    public static void register() {
        if (!registered) {
            GuiManager.registerFactory(INSTANCE);
            registered = true;
        }
    }

    /**
     * 为指定玩家打开胸腔界面。
     *
     * @param player 接收界面的玩家。
     * @param data 界面同步数据。
     */
    public static void open(EntityPlayerMP player, ChestCavityGuiData data) {
        register();
        GuiManager.open(INSTANCE, data, player);
    }

    /**
     * 创建实际负责构建界面的持有者对象。
     *
     * @param data 界面同步数据。
     * @return 胸腔界面持有者。
     */
    @Override
    public IGuiHolder<ChestCavityGuiData> getGuiHolder(ChestCavityGuiData data) {
        return new ChestCavityUiHolder();
    }

    /**
     * 把胸腔界面同步数据写入网络缓冲区。
     *
     * @param data 界面同步数据。
     * @param buffer 网络缓冲区。
     */
    @Override
    public void writeGuiData(ChestCavityGuiData data, PacketBuffer buffer) {
        buffer.writeInt(data.getTargetEntityId());
    }

    /**
     * 从网络缓冲区读取胸腔界面同步数据。
     *
     * @param player 打开界面的玩家。
     * @param buffer 网络缓冲区。
     * @return 读取出的界面同步数据。
     */
    @Override
    public ChestCavityGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new ChestCavityGuiData(player, buffer.readInt());
    }

    /**
     * 判断玩家在当前时刻是否还能继续与该界面交互。
     *
     * @param player 交互玩家。
     * @param data 界面同步数据。
     * @return `true` 表示允许继续交互。
     */
    @Override
    public boolean canInteractWith(EntityPlayer player, ChestCavityGuiData data) {
        return ChestCavityUiBridge.canKeepOpen(player, data);
    }
}
