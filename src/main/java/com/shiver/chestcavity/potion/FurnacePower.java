package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.registry.CCPotions;
import com.shiver.chestcavity.scoreevent.FoodScoreEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entityLivingBaseIn);
        if (chestCavity == null) {
            return;
        }

        int progress = chestCavity.getFurnaceProgress() + 1;
        if (progress >= FOOD_INTERVAL_TICKS) {
            progress = 0;
            EntityPlayer player = (EntityPlayer) entityLivingBaseIn;
            FoodScoreEvents.consumeFurnacePowerFood(player);
        }
        chestCavity.setFurnaceProgress(progress);
    }

    public static int getActiveLayerCount(EntityPlayer player) {
        if (player == null || !player.isPotionActive(CCPotions.FURNACE_POWER)) {
            return 0;
        }
        return readFuelLayers(player, true).size();
    }

    public static boolean addFuelLayer(EntityPlayer player, int burnTime, int maxLayers) {
        if (player == null || player.world.isRemote || burnTime <= 0 || maxLayers <= 0) {
            return false;
        }

        List<Integer> layers = readFuelLayers(player, true);
        if (layers.size() >= maxLayers) {
            return false;
        }

        layers.add(burnTime);
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

        List<Integer> layers = readFuelLayers(player, true);
        List<Integer> remaining = new ArrayList<>();
        for (Integer layer : layers) {
            int duration = layer - 1;
            if (duration > 0) {
                remaining.add(duration);
            }
        }

        if (remaining.isEmpty()) {
            clearFuelLayers(player);
            resetFurnaceProgress(player);
            player.removePotionEffect(CCPotions.FURNACE_POWER);
            return;
        }

        writeFuelLayers(player, remaining);
        syncVisibleEffect(player, remaining);
    }

    private static List<Integer> readFuelLayers(EntityPlayer player, boolean seedFromPotion) {
        NBTTagCompound data = player.getEntityData();
        List<Integer> layers = new ArrayList<>();
        if (data.hasKey(FUEL_LAYERS_KEY, Constants.NBT.TAG_INT_ARRAY)) {
            int[] values = data.getIntArray(FUEL_LAYERS_KEY);
            for (int value : values) {
                if (value > 0) {
                    layers.add(value);
                }
            }
            return layers;
        }

        PotionEffect current = player.getActivePotionEffect(CCPotions.FURNACE_POWER);
        if (seedFromPotion && current != null && current.getDuration() > 0) {
            int layerCount = Math.max(1, current.getAmplifier() + 1);
            for (int i = 0; i < layerCount; i++) {
                layers.add(current.getDuration());
            }
            writeFuelLayers(player, layers);
        }
        return layers;
    }

    private static void writeFuelLayers(EntityPlayer player, List<Integer> layers) {
        int[] values = new int[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            values[i] = layers.get(i);
        }
        player.getEntityData().setIntArray(FUEL_LAYERS_KEY, values);
    }

    private static void clearFuelLayers(EntityPlayer player) {
        player.getEntityData().removeTag(FUEL_LAYERS_KEY);
    }

    private static void syncVisibleEffect(EntityPlayer player, List<Integer> layers) {
        int duration = shortestDuration(layers);
        int amplifier = layers.size() - 1;
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

    private static int shortestDuration(List<Integer> layers) {
        int duration = Integer.MAX_VALUE;
        for (Integer layer : layers) {
            duration = Math.min(duration, layer);
        }
        return Math.max(1, duration);
    }

    private static void resetFurnaceProgress(EntityPlayer player) {
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(player);
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
