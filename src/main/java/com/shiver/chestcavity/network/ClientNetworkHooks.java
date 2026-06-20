package com.shiver.chestcavity.network;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.content.ContentSync;
import com.shiver.chestcavity.ui.BodyUiSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientNetworkHooks {

    private ClientNetworkHooks() {
    }

    public static void handleChestCavitySync(MessageChestCavitySync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(() -> {
            if (minecraft.world == null) {
                return;
            }

            Entity entity = minecraft.world.getEntityByID(message.getEntityId());
            ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
            if (chestCavity != null) {
                chestCavity.deserializeNBT(BodyUiSnapshot.getState(message.getData()));
            }
        });
    }

    public static void handleOrganDataSync(MessageOrganDataSync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        NBTTagCompound organData = message.getOrganData().copy();
        minecraft.addScheduledTask(() -> ContentSync.readOrgansFromNbt(organData));
    }
}
