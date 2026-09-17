package com.shiver.chestcavity.organ;

/**
 * Pure organ-score formulas used by runtime controllers and unit tests.
 */
public final class OrganFormulas {

    public static final int MAX_AIR = 300;
    public static final int LAND_AIR_GAIN = 4;
    public static final float BUOYANT_EXHALE_AIR_COST = 4.5F;

    private OrganFormulas() {
    }

    public static int digestedHunger(float digestion, int food) {
        if (digestion == 1.0F) {
            return food;
        }
        if (digestion < 0.0F) {
            return 1;
        }
        return Math.max((int) (food * digestion), 1);
    }

    public static int nauseaTicks(float digestion, int food) {
        if (digestion >= 0.0F) {
            return 0;
        }
        return (int) (-food * digestion * 400.0F);
    }

    public static float digestedSaturation(float nutrition, float saturation) {
        if (nutrition < 0.0F) {
            return 0.0F;
        }
        return saturation * nutrition / 4.0F;
    }

    public static int hungerTicks(float nutrition, float saturation) {
        if (nutrition >= 0.0F) {
            return 0;
        }
        return (int) (-saturation * nutrition * 800.0F);
    }

    public static MetabolismTick applySpleenMetabolism(int foodTimer, float metabolismDiff, float remainder) {
        if (metabolismDiff == 0.0F) {
            return new MetabolismTick(foodTimer, remainder);
        }

        float nextRemainder = remainder;
        int nextTimer = foodTimer;
        if (metabolismDiff > 0.0F) {
            nextRemainder += metabolismDiff;
            nextTimer += (int) nextRemainder;
        } else {
            nextRemainder += 1.0F - 1.0F / ((-metabolismDiff) + 1.0F);
            nextTimer -= (int) nextRemainder;
        }
        nextRemainder = nextRemainder % 1.0F;
        return new MetabolismTick(nextTimer, nextRemainder);
    }

    public static BreathTick applyBreathInWater(int oldAir, int vanillaNewAir, float capacity, float waterBreath,
                                                boolean sprinting, float remainder, int maxAir) {
        float airLoss = 1.0F;
        float effectiveWaterBreath = sprinting ? waterBreath / 4.0F : waterBreath;
        if (effectiveWaterBreath > 0.0F) {
            airLoss += -2.0F * effectiveWaterBreath;
        }

        if (airLoss > 0.0F) {
            if (oldAir == vanillaNewAir) {
                airLoss = 0.0F;
            } else {
                airLoss *= (oldAir - vanillaNewAir);
                if (airLoss > 0.0F) {
                    float lungRatio = 20.0F;
                    if (capacity != 0.0F) {
                        lungRatio = Math.min(2.0F / capacity, 20.0F);
                    }
                    airLoss = (airLoss * lungRatio) + remainder;
                }
            }
        }

        float nextRemainder = airLoss % 1.0F;
        int airResult = Math.min(oldAir - ((int) airLoss), maxAir);
        boolean drown = false;
        if (airResult <= -20) {
            airResult = 0;
            nextRemainder = 0.0F;
            drown = true;
        }
        return new BreathTick(airResult, nextRemainder, drown);
    }

    public static BreathTick applyBreathOnLand(int oldAir, int airGain, float recovery, float capacity, float waterBreath,
                                               boolean sprinting, boolean wet, boolean waterBreathing,
                                               boolean respirationCancels, float remainder, int maxAir) {
        float airLoss = waterBreathing ? 0.0F : 1.0F;
        float breath = sprinting ? recovery / 4.0F : recovery;
        if (wet) {
            breath += waterBreath / 4.0F;
        }
        if (breath > 0.0F) {
            airLoss += (-airGain * breath / 2.0F);
        }

        if (airLoss > 0.0F) {
            if (respirationCancels) {
                airLoss = 0.0F;
            } else {
                float breathRatio = 20.0F;
                if (capacity != 0.0F) {
                    breathRatio = Math.min(2.0F / capacity, 20.0F);
                }
                airLoss = (airLoss * breathRatio) + remainder;
            }
        } else if (oldAir == maxAir) {
            return new BreathTick(oldAir, remainder, false);
        }

        float nextRemainder = airLoss % 1.0F;
        int airResult = Math.min(oldAir - ((int) airLoss) - airGain, maxAir);
        boolean drown = false;
        if (airResult <= -20) {
            airResult = 0;
            nextRemainder = 0.0F;
            drown = true;
        }
        return new BreathTick(airResult, nextRemainder, drown);
    }

    public static AirLoss buoyantExhale(float breathRecovery, float remainder) {
        float breathLoss = breathRecovery * BUOYANT_EXHALE_AIR_COST - remainder;
        float nextRemainder = 1.0F - (breathLoss % 1.0F);
        return new AirLoss((int) breathLoss, nextRemainder);
    }

    public static final class MetabolismTick {
        public final int foodTimer;
        public final float remainder;

        public MetabolismTick(int foodTimer, float remainder) {
            this.foodTimer = foodTimer;
            this.remainder = remainder;
        }
    }

    public static final class BreathTick {
        public final int air;
        public final float remainder;
        public final boolean drown;

        public BreathTick(int air, float remainder, boolean drown) {
            this.air = air;
            this.remainder = remainder;
            this.drown = drown;
        }
    }

    public static final class AirLoss {
        public final int wholeAir;
        public final float remainder;

        public AirLoss(int wholeAir, float remainder) {
            this.wholeAir = wholeAir;
            this.remainder = remainder;
        }
    }
}
