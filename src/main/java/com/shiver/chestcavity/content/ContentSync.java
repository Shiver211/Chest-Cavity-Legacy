package com.shiver.chestcavity.content;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ContentSync {

    private static final int SYNC_VERSION = 1;
    private static final String VERSION_TAG = "Version";
    private static final String ORGANS_TAG = "Organs";
    private static final String ITEM_ID_TAG = "ItemId";
    private static final String DATA_TAG = "Data";

    private ContentSync() {
    }

    public static NBTTagCompound writeOrgansToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList organs = new NBTTagList();
        for (Map.Entry<ResourceLocation, OrganData> entry : ContentRegistry.getCompiled().getOrgans().entrySet()) {
            NBTTagCompound organTag = new NBTTagCompound();
            organTag.setString(ITEM_ID_TAG, entry.getKey().toString());
            organTag.setTag(DATA_TAG, entry.getValue().toNbt());
            organs.appendTag(organTag);
        }
        root.setInteger(VERSION_TAG, SYNC_VERSION);
        root.setTag(ORGANS_TAG, organs);
        return root;
    }

    public static void readOrgansFromNbt(NBTTagCompound root) {
        if (root == null || !root.hasKey(ORGANS_TAG, Constants.NBT.TAG_LIST)) {
            return;
        }

        Map<ResourceLocation, OrganDef> syncedOrgans = new LinkedHashMap<>();
        NBTTagList organs = root.getTagList(ORGANS_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < organs.tagCount(); i++) {
            NBTTagCompound organTag = organs.getCompoundTagAt(i);
            if (!organTag.hasKey(ITEM_ID_TAG, Constants.NBT.TAG_STRING)
                    || !organTag.hasKey(DATA_TAG, Constants.NBT.TAG_COMPOUND)) {
                continue;
            }

            try {
                ResourceLocation itemId = new ResourceLocation(organTag.getString(ITEM_ID_TAG));
                syncedOrgans.put(itemId, new OrganDef(itemId, OrganData.fromNbt(organTag.getCompoundTag(DATA_TAG))));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to read synced organ entry {}.", i, e);
            }
        }
        ContentRegistry.publishSyncedOrgans(syncedOrgans);
    }
}
