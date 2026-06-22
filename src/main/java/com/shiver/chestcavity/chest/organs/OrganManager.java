package com.shiver.chestcavity.chest.organs;

import com.google.gson.JsonObject;
import com.shiver.chestcavity.ChestCavityLegacy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;

/**
 * 负责器官数据的加载、注册以及网络同步序列化。
 */
public final class OrganManager {

    private static final OrganSerializer SERIALIZER = new OrganSerializer();
    private static final int SYNC_VERSION = 1;
    private static final String VERSION_TAG = "Version";
    private static final String ORGANS_TAG = "Organs";
    private static final String ITEM_ID_TAG = "ItemId";
    private static final String DATA_TAG = "Data";

    /**
     * 工具类，不允许外部实例化。
     */
    private OrganManager() {
    }

    /**
     * 清空当前已加载的器官数据。
     */
    public static void clear() {
        OrganData.clearRegistry();
    }

    /**
     * 从 JSON 数据中加载一个器官定义并注册到内存中。
     *
     * @param id 当前数据文件的资源标识。
     * @param json 解析后的 JSON 对象。
     */
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

    /**
     * 根据物品堆查询其器官数据定义。
     *
     * @param stack 要查询的物品堆。
     * @return 对应的器官数据；如果没有则返回 `null`。
     */
    public static OrganData get(ItemStack stack) {
        return OrganData.fromRegistry(stack);
    }

    /**
     * 判断一个物品是否对应真正可掉落的器官，而不是伪器官。
     *
     * @param stack 要检查的物品堆。
     * @return `true` 表示它是一个真实器官。
     */
    public static boolean isTrueOrgan(ItemStack stack) {
        OrganData data = get(stack);
        return data != null && !data.isPseudoOrgan();
    }

    /**
     * 将当前器官注册表写出为可网络同步的 NBT。
     *
     * @return 序列化后的器官注册表。
     */
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

    /**
     * 用 NBT 数据重建当前器官注册表。
     *
     * @param root 序列化后的器官注册表数据。
     */
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
