package com.shiver.chestcavity.chest.organs;

import com.google.gson.JsonObject;
import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.script.registry.ScriptOrganRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;

public final class OrganManager {

    private static final OrganSerializer SERIALIZER = new OrganSerializer();
    private static final int SYNC_VERSION = 1;
    private static final String VERSION_TAG = "Version";
    private static final String ORGANS_TAG = "Organs";
    private static final String ITEM_ID_TAG = "ItemId";
    private static final String DATA_TAG = "Data";

    private OrganManager() {
    }

    public static void clear() {
        OrganData.clearRegistry();
    }

    public static void load(ResourceLocation id, JsonObject json) {
        OrganSerializer.OrganEntry entry = SERIALIZER.read(id, json);
        if (entry == null) {
            return;
        }
        if (ForgeRegistries.ITEMS.getValue(entry.itemId) == null) {
            ChestCavityLegacy.LOGGER.info("Skipping organ {} because item {} is not registered in 1.12.2.", id, entry.itemId);
            return;
        }
        OrganData.register(entry.itemId, entry.data);
    }

    public static OrganData get(ItemStack stack) {
        OrganData data = ScriptOrganRegistry.getOrganData(stack);
        return data == null ? OrganData.fromRegistry(stack) : data;
    }

    public static boolean isTrueOrgan(ItemStack stack) {
        OrganData data = get(stack);
        return data != null && !data.isPseudoOrgan();
    }

    public static NBTTagCompound writeRegistryToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList organs = new NBTTagList();
        for (Map.Entry<ResourceLocation, OrganData> entry : OrganData.getRegistry().entrySet()) {
            NBTTagCompound organTag = new NBTTagCompound();
            organTag.setString(ITEM_ID_TAG, entry.getKey().toString());
            organTag.setTag(DATA_TAG, entry.getValue().toNbt());
            organs.appendTag(organTag);
        }
        root.setInteger(VERSION_TAG, SYNC_VERSION);
        root.setTag(ORGANS_TAG, organs);
        return root;
    }

    public static void readRegistryFromNbt(NBTTagCompound root) {
        clear();
        if (root == null || !root.hasKey(ORGANS_TAG, Constants.NBT.TAG_LIST)) {
            return;
        }

        NBTTagList organs = root.getTagList(ORGANS_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < organs.tagCount(); i++) {
            NBTTagCompound organTag = organs.getCompoundTagAt(i);
            if (!organTag.hasKey(ITEM_ID_TAG, Constants.NBT.TAG_STRING)
                    || !organTag.hasKey(DATA_TAG, Constants.NBT.TAG_COMPOUND)) {
                continue;
            }

            try {
                ResourceLocation itemId = new ResourceLocation(organTag.getString(ITEM_ID_TAG));
                if (ForgeRegistries.ITEMS.getValue(itemId) == null) {
                    ChestCavityLegacy.LOGGER.info("Skipping synced organ because item {} is not registered in 1.12.2.", itemId);
                    continue;
                }
                OrganData.register(itemId, OrganData.fromNbt(organTag.getCompoundTag(DATA_TAG)));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to read synced organ entry {}.", i, e);
            }
        }
    }
}
