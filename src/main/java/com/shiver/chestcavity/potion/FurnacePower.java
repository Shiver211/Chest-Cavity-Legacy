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

public class FurnacePower extends CCPotion {

    private static final String FUEL_LAYERS_KEY = "chestcavity:furnace_power_layers";
    private static final int FOOD_INTERVAL_TICKS = 200;
    private static final Field POTION_EFFECT_DURATION_FIELD = findPotionEffectField("duration", "field_76460_b");
    private static final Field POTION_EFFECT_AMPLIFIER_FIELD = findPotionEffectField("amplifier", "field_76461_c");

    public FurnacePower() {
        super(false, 0xFF8C24);
        setBeneficial();
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

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

    public static int getActiveLayerCount(EntityPlayer player) {
        if (player == null || !player.isPotionActive(CCPotions.FURNACE_POWER)) {
            return 0;
        }
        return readFuelLayers(player, true).length;
    }

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

    private static void writeFuelLayers(EntityPlayer player, int[] layers) {
        player.getEntityData().setIntArray(FUEL_LAYERS_KEY, layers);
    }

    private static void clearFuelLayers(EntityPlayer player) {
        player.getEntityData().removeTag(FUEL_LAYERS_KEY);
    }

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

    private static int shortestDuration(int[] layers) {
        int duration = Integer.MAX_VALUE;
        for (int layer : layers) {
            duration = Math.min(duration, layer);
        }
        return Math.max(1, duration);
    }

    private static void resetFurnaceProgress(EntityPlayer player) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
        if (chestCavity != null) {
            chestCavity.setFurnaceProgress(0);
        }
    }

    private static void setPotionEffectValue(PotionEffect effect, Field field, int value) {
        if (field == null) {
            return;
        }
        try {
            field.setInt(effect, value);
        } catch (IllegalAccessException ignored) {
        }
    }

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
