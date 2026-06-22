package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 表示炉火能量效果，并维护其分层燃料持续时间。
 */
public class FurnacePower extends CCPotion {

    private static final String FUEL_LAYERS_KEY = "chestcavity:furnace_power_layers";
    private static final int FOOD_INTERVAL_TICKS = 200;
    private static final Field POTION_EFFECT_DURATION_FIELD = findPotionEffectField("duration", "field_76460_b");
    private static final Field POTION_EFFECT_AMPLIFIER_FIELD = findPotionEffectField("amplifier", "field_76461_c");

    /**
     * 创建炉火能量药水效果。
     */
    public FurnacePower() {
        super(false, 0xFF8C24);
        setBeneficial();
    }

    /**
     * 让该效果每个 tick 都执行一次。
     *
     * @param duration 剩余持续时间。
     * @param amplifier 当前层数。
     * @return 始终返回 `true`。
     */
    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    /**
     * 推进炉火能量进度，并在达到阈值时转化为进食效果。
     *
     * @param entityLivingBaseIn 目标实体。
     * @param amplifier 当前层数。
     */
    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn.world.isRemote || !(entityLivingBaseIn instanceof EntityPlayer)) {
            return;
        }

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entityLivingBaseIn);
        if (chestCavity == null) {
            return;
        }

        int progress = chestCavity.getFurnaceProgress() + 1;
        if (progress >= FOOD_INTERVAL_TICKS) {
            progress = 0;
            EntityPlayer player = (EntityPlayer) entityLivingBaseIn;
            ChestCavityHelper.consumeFurnacePowerFood(player);
        }
        chestCavity.setFurnaceProgress(progress);
    }

    /**
     * 返回玩家当前炉火能量的活跃层数。
     *
     * @param player 目标玩家。
     * @return 活跃层数。
     */
    public static int getActiveLayerCount(EntityPlayer player) {
        if (player == null || !player.isPotionActive(CCPotions.FURNACE_POWER)) {
            return 0;
        }
        return readFuelLayers(player, true).length;
    }

    /**
     * 为玩家新增一层燃料持续时间。
     *
     * @param player 目标玩家。
     * @param burnTime 本层持续时间。
     * @param maxLayers 允许的最大层数。
     * @return `true` 表示成功加入一层。
     */
    public static boolean addFuelLayer(EntityPlayer player, int burnTime, int maxLayers) {
        if (player == null || player.world.isRemote || burnTime <= 0 || maxLayers <= 0) {
            return false;
        }

        int[] layers = readFuelLayers(player, true);
        if (layers.length >= maxLayers) {
            return false;
        }

        layers = Arrays.copyOf(layers, layers.length + 1);
        layers[layers.length - 1] = burnTime;
        writeFuelLayers(player, layers);
        syncVisibleEffect(player, layers);
        return true;
    }

    /**
     * 在每个 tick 中推进全部炉火燃料层的剩余时间。
     *
     * @param entity 目标实体。
     */
    public static void tickFuelLayers(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        if (!player.isPotionActive(CCPotions.FURNACE_POWER)) {
            clearFuelLayers(player);
            resetFurnaceProgress(player);
            return;
        }

        int[] layers = readFuelLayers(player, true);
        int remainingCount = 0;
        for (int i = 0; i < layers.length; i++) {
            int duration = layers[i] - 1;
            if (duration > 0) {
                layers[remainingCount++] = duration;
            }
        }

        if (remainingCount == 0) {
            clearFuelLayers(player);
            resetFurnaceProgress(player);
            player.removePotionEffect(CCPotions.FURNACE_POWER);
            return;
        }

        boolean layerCountChanged = remainingCount != layers.length;
        if (layerCountChanged) {
            layers = Arrays.copyOf(layers, remainingCount);
        }
        writeFuelLayers(player, layers);
        PotionEffect current = player.getActivePotionEffect(CCPotions.FURNACE_POWER);
        if (layerCountChanged || current == null || current.getAmplifier() != layers.length - 1) {
            syncVisibleEffect(player, layers);
        }
    }

    /**
     * 读取玩家当前保存的全部燃料层。
     *
     * @param player 目标玩家。
     * @param seedFromPotion 当没有缓存时，是否尝试从药水效果中恢复层数。
     * @return 燃料层持续时间数组。
     */
    private static int[] readFuelLayers(EntityPlayer player, boolean seedFromPotion) {
        NBTTagCompound data = player.getEntityData();
        if (data.hasKey(FUEL_LAYERS_KEY, Constants.NBT.TAG_INT_ARRAY)) {
            int[] values = data.getIntArray(FUEL_LAYERS_KEY);
            int count = 0;
            for (int value : values) {
                if (value > 0) {
                    values[count++] = value;
                }
            }
            return count == values.length ? values : Arrays.copyOf(values, count);
        }

        PotionEffect current = player.getActivePotionEffect(CCPotions.FURNACE_POWER);
        if (seedFromPotion && current != null && current.getDuration() > 0) {
            int layerCount = Math.max(1, current.getAmplifier() + 1);
            int[] layers = new int[layerCount];
            Arrays.fill(layers, current.getDuration());
            writeFuelLayers(player, layers);
            return layers;
        }
        return new int[0];
    }

    /**
     * 把燃料层数组写回玩家实体数据。
     *
     * @param player 目标玩家。
     * @param layers 燃料层数组。
     */
    private static void writeFuelLayers(EntityPlayer player, int[] layers) {
        player.getEntityData().setIntArray(FUEL_LAYERS_KEY, layers);
    }

    /**
     * 清空玩家保存的全部燃料层。
     *
     * @param player 目标玩家。
     */
    private static void clearFuelLayers(EntityPlayer player) {
        player.getEntityData().removeTag(FUEL_LAYERS_KEY);
    }

    /**
     * 把内部燃料层状态同步为可见的药水效果层数与持续时间。
     *
     * @param player 目标玩家。
     * @param layers 当前燃料层数组。
     */
    private static void syncVisibleEffect(EntityPlayer player, int[] layers) {
        int duration = shortestDuration(layers);
        int amplifier = layers.length - 1;
        PotionEffect current = player.getActivePotionEffect(CCPotions.FURNACE_POWER);

        if (current == null || current.getAmplifier() != amplifier) {
            if (current != null) {
                player.removePotionEffect(CCPotions.FURNACE_POWER);
            }
            player.addPotionEffect(new PotionEffect(CCPotions.FURNACE_POWER, duration, amplifier, false, true));
            current = player.getActivePotionEffect(CCPotions.FURNACE_POWER);
        }

        if (current != null) {
            setPotionEffectValue(current, POTION_EFFECT_DURATION_FIELD, duration);
            setPotionEffectValue(current, POTION_EFFECT_AMPLIFIER_FIELD, amplifier);
        }
    }

    /**
     * 返回所有燃料层中最短的剩余持续时间。
     *
     * @param layers 燃料层数组。
     * @return 最短持续时间。
     */
    private static int shortestDuration(int[] layers) {
        int duration = Integer.MAX_VALUE;
        for (int layer : layers) {
            duration = Math.min(duration, layer);
        }
        return Math.max(1, duration);
    }

    /**
     * 把玩家胸腔中的熔炉进度归零。
     *
     * @param player 目标玩家。
     */
    private static void resetFurnaceProgress(EntityPlayer player) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity != null) {
            chestCavity.setFurnaceProgress(0);
        }
    }

    /**
     * 通过反射修改药水效果内部字段值。
     *
     * @param effect 要修改的药水效果。
     * @param field 目标字段。
     * @param value 新的数值。
     */
    private static void setPotionEffectValue(PotionEffect effect, Field field, int value) {
        if (field == null) {
            return;
        }
        try {
            field.setInt(effect, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    /**
     * 通过反射查找药水效果内部字段。
     *
     * @param name MCP 字段名。
     * @param srgName SRG 字段名。
     * @return 查找到的字段；失败时返回 `null`。
     */
    private static Field findPotionEffectField(String name, String srgName) {
        try {
            Field field = ReflectionHelper.findField(PotionEffect.class, name, srgName);
            field.setAccessible(true);
            return field;
        } catch (ReflectionHelper.UnableToFindFieldException ignored) {
            return null;
        }
    }
}
