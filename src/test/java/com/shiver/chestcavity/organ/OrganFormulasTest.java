package com.shiver.chestcavity.organ;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganFormulasTest {

    @Test
    void digestedHungerKeepsVanillaWhenDigestionIsOne() {
        assertEquals(8, OrganFormulas.digestedHunger(1.0F, 8));
    }

    @Test
    void digestedHungerNeverDropsBelowOne() {
        assertEquals(1, OrganFormulas.digestedHunger(0.1F, 2));
        assertEquals(1, OrganFormulas.digestedHunger(-1.0F, 8));
        assertEquals(400, OrganFormulas.nauseaTicks(-0.125F, 8));
    }

    @Test
    void digestedSaturationScalesByQuarter() {
        assertEquals(0.5F, OrganFormulas.digestedSaturation(2.0F, 1.0F));
        assertEquals(0.0F, OrganFormulas.digestedSaturation(-1.0F, 1.0F));
        assertEquals(800, OrganFormulas.hungerTicks(-1.0F, 1.0F));
    }

    @Test
    void spleenMetabolismSpeedsUpPositiveScore() {
        OrganFormulas.MetabolismTick first = OrganFormulas.applySpleenMetabolism(0, 1.5F, 0.0F);
        assertEquals(1, first.foodTimer);
        assertEquals(0.5F, first.remainder, 0.0001F);

        OrganFormulas.MetabolismTick second = OrganFormulas.applySpleenMetabolism(first.foodTimer, 1.5F, first.remainder);
        assertEquals(3, second.foodTimer);
        assertEquals(0.0F, second.remainder, 0.0001F);
    }

    @Test
    void spleenMetabolismSlowsNegativeScore() {
        OrganFormulas.MetabolismTick tick = OrganFormulas.applySpleenMetabolism(10, -1.0F, 0.0F);
        assertEquals(10, tick.foodTimer);
        assertEquals(0.5F, tick.remainder, 0.0001F);
    }

    @Test
    void waterBreathRestoresAirWhenGillsArePresent() {
        OrganFormulas.BreathTick tick = OrganFormulas.applyBreathInWater(10, 9, 1.0F, 1.0F, false, 0.0F, 300);
        assertEquals(11, tick.air);
        assertFalse(tick.drown);
    }

    @Test
    void missingLungsCauseLandSuffocation() {
        OrganFormulas.BreathTick tick = OrganFormulas.applyBreathOnLand(
                20, 4, 0.0F, 0.0F, 0.0F, false, false, false, false, 0.0F, 300);
        assertTrue(tick.air < 20);
        assertFalse(tick.drown);
    }

    @Test
    void buoyantExhaleSpendsAirFromRecovery() {
        OrganFormulas.AirLoss loss = OrganFormulas.buoyantExhale(1.0F, 0.0F);
        assertEquals(4, loss.wholeAir);
        assertEquals(0.5F, loss.remainder, 0.0001F);
    }
}
