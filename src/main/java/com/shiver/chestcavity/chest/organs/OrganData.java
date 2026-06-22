package com.shiver.chestcavity.chest.organs;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述一个器官物品对应的器官属性数据，以及其在内存中的注册表。
 */
public class OrganData {

    public static final String ORGAN_TAG = "ChestCavityOrganData";
    private static final String PSEUDO_TAG = "PseudoOrgan";
    private static final String SCORES_TAG = "OrganScores";
    private static final Map<ResourceLocation, OrganData> ORGAN_DATA = new LinkedHashMap<>();

    private boolean pseudoOrgan;
    private Map<String, Float> organScores = new LinkedHashMap<>();

    /**
     * 清空当前已加载的器官数据注册表。
     */
    public static void clearRegistry() {
        ORGAN_DATA.clear();
    }

    /**
     * 向注册表中登记一种器官物品的数据定义。
     *
     * @param itemId 物品注册名。
     * @param data 对应的器官数据。
     */
    public static void register(ResourceLocation itemId, OrganData data) {
        if (itemId != null && data != null) {
            ORGAN_DATA.put(itemId, data);
        }
    }

    /**
     * 从注册表中移除一种器官物品的数据定义。
     *
     * @param itemId 物品注册名。
     */
    public static void unregister(ResourceLocation itemId) {
        if (itemId != null) {
            ORGAN_DATA.remove(itemId);
        }
    }

    /**
     * 按物品注册名查询器官数据。
     *
     * @param itemId 物品注册名。
     * @return 对应的器官数据；如果不存在则返回 `null`。
     */
    public static OrganData get(ResourceLocation itemId) {
        return ORGAN_DATA.get(itemId);
    }

    /**
     * 根据物品堆在注册表中查找对应的器官数据。
     *
     * @param stack 要查询的物品堆。
     * @return 对应的器官数据；如果物品无效则返回 `null`。
     */
    public static OrganData fromRegistry(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        return get(stack.getItem().getRegistryName());
    }

    /**
     * 返回只读的器官数据注册表视图。
     *
     * @return 器官数据注册表。
     */
    public static Map<ResourceLocation, OrganData> getRegistry() {
        return Collections.unmodifiableMap(ORGAN_DATA);
    }

    /**
     * 判断该数据是否描述伪器官。
     *
     * @return `true` 表示这是伪器官数据。
     */
    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    /**
     * 设置该数据是否描述伪器官。
     *
     * @param pseudoOrgan 是否为伪器官。
     */
    public void setPseudoOrgan(boolean pseudoOrgan) {
        this.pseudoOrgan = pseudoOrgan;
    }

    /**
     * 返回当前器官分数字典。
     *
     * @return 器官分数字典。
     */
    public Map<String, Float> getOrganScores() {
        return organScores;
    }

    /**
     * 用新的分数字典整体替换当前器官分数。
     *
     * @param organScores 新的器官分数字典。
     */
    public void setOrganScores(Map<String, Float> organScores) {
        this.organScores = new LinkedHashMap<>();
        if (organScores != null) {
            this.organScores.putAll(organScores);
        }
    }

    /**
     * 返回只读的器官分数字典视图。
     *
     * @return 器官分数字典视图。
     */
    public Map<String, Float> getOrganScoresView() {
        return Collections.unmodifiableMap(organScores);
    }

    /**
     * 将当前器官数据写入物品堆的 NBT。
     *
     * @param stack 要写入数据的物品堆。
     */
    public void writeToStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        root.setTag(ORGAN_TAG, toNbt());
    }

    /**
     * 将当前器官数据转换为 NBT。
     *
     * @return 序列化后的 NBT 数据。
     */
    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(PSEUDO_TAG, pseudoOrgan);
        NBTTagCompound scoresTag = new NBTTagCompound();
        for (Map.Entry<String, Float> entry : organScores.entrySet()) {
            scoresTag.setFloat(entry.getKey().toString(), entry.getValue());
        }
        tag.setTag(SCORES_TAG, scoresTag);
        return tag;
    }

    /**
     * 从物品堆的 NBT 中读取器官数据。
     *
     * @param stack 要读取的物品堆。
     * @return 读取出的器官数据；如果物品不含数据则返回 `null`。
     */
    public static OrganData fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(ORGAN_TAG, 10)) {
            return null;
        }
        return fromNbt(root.getCompoundTag(ORGAN_TAG));
    }

    /**
     * 从 NBT 中创建一个器官数据实例。
     *
     * @param tag 序列化后的器官数据。
     * @return 新创建的器官数据实例。
     */
    public static OrganData fromNbt(NBTTagCompound tag) {
        OrganData data = new OrganData();
        data.pseudoOrgan = tag.getBoolean(PSEUDO_TAG);
        NBTTagCompound scoresTag = tag.getCompoundTag(SCORES_TAG);
        for (String key : scoresTag.getKeySet()) {
            data.organScores.put(key, scoresTag.getFloat(key));
        }
        return data;
    }
}
