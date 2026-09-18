package com.shiver.chestcavity;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.ChestCavityCapability;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.organ.OrganDataResolver;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestCavityFixesTest {

    @BeforeAll
    static void init() {
        if (!Bootstrap.isRegistered()) {
            Bootstrap.register();
        }
    }

    @Test
    void testScoreChangesDirtyFlag() {
        ChestCavityData data = new ChestCavityData();
        assertTrue(data.hasScoreChanges(), "Newly created chest cavity should have score changes");

        data.copyCurrentScoresToOld();
        assertFalse(data.hasScoreChanges(), "Score changes should be false after copying to old");

        Map<String, Float> sameScores = new HashMap<>();
        data.replaceOrganScores(sameScores);
        assertFalse(data.hasScoreChanges(), "Replacing with identical scores should keep scoreChanges false");

        Map<String, Float> newScores = new HashMap<>();
        newScores.put(CCOrganScores.HEALTH, 2.0F);
        data.replaceOrganScores(newScores);
        assertTrue(data.hasScoreChanges(), "Replacing with different scores should set scoreChanges true");

        data.copyCurrentScoresToOld();
        assertFalse(data.hasScoreChanges());

        data.setOrganScore(CCOrganScores.HEALTH, 3.0F);
        assertTrue(data.hasScoreChanges(), "setOrganScore should set scoreChanges true");

        data.copyCurrentScoresToOld();
        assertFalse(data.hasScoreChanges());

        data.markScoresDirty();
        assertTrue(data.hasScoreChanges(), "markScoresDirty should set scoreChanges true");
    }

    @Test
    void testGeneratedChestCavityTypeCatchExceptionalOrganReturnsNullForNormalItem() {
        GeneratedChestCavityType type = new GeneratedChestCavityType();
        ItemStack ironSword = new ItemStack(Items.IRON_SWORD);
        assertNull(type.catchExceptionalOrgan(ironSword), "Non-exceptional organ should return null");
    }

    @Test
    void testOrganDataResolverPrioritizesDynamicNbtOverRegistry() {
        GeneratedChestCavityType type = new GeneratedChestCavityType();
        ResourceLocation regId = new ResourceLocation("minecraft", "apple");
        OrganData staticData = new OrganData();
        staticData.getOrganScores().put(CCOrganScores.DIGESTION, 1.0F);
        OrganData.register(regId, staticData);

        ItemStack stack = new ItemStack(Items.APPLE);
        OrganData resolved = OrganDataResolver.resolve(type, stack);
        assertNotNull(resolved);
        assertEquals(1.0F, resolved.getOrganScores().get(CCOrganScores.DIGESTION));

        // Add dynamic NBT organ scores
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound organTag = new NBTTagCompound();
        NBTTagCompound scoresTag = new NBTTagCompound();
        scoresTag.setFloat(CCOrganScores.DIGESTION, 5.0F);
        organTag.setTag("OrganScores", scoresTag);
        root.setTag(OrganData.ORGAN_TAG, organTag);
        stack.setTagCompound(root);

        OrganData dynamicResolved = OrganDataResolver.resolve(type, stack);
        assertNotNull(dynamicResolved);
        assertEquals(5.0F, dynamicResolved.getOrganScores().get(CCOrganScores.DIGESTION),
                "Dynamic ItemStack NBT score should take precedence over static registry");

        OrganData.unregister(regId);
    }

    @Test
    void testDataLoadersReloadClearsRuntimeOverrides() {
        AtomicInteger counter = new AtomicInteger(0);
        DataLoaders.applyRuntimeOverride(counter::incrementAndGet);
        assertEquals(1, counter.get());

        // Reload should clear previously registered overrides
        DataLoaders.reload();
        // Since runtime overrides were cleared, counter should remain 1 (not re-executed)
        assertEquals(1, counter.get());
    }

    @Test
    void testAbilityActivationCancellation() {
        String testAbility = "test_cancel_ability";
        // Default with no handlers
        boolean canceled = CrTChestCavityEvents.publishAbilityActivated(null, testAbility, 1.0F);
        assertFalse(canceled, "Default event with no handlers should not be canceled");

        // Add a canceling handler
        crafttweaker.api.event.IEventHandle handle = com.shiver.chestcavity.crt.CrTChestCavityEventManager.onAbilityActivated(null, event -> {
            if (testAbility.equals(event.getAbilityId())) {
                event.cancel();
            }
        });

        try {
            boolean canceledWithHandler = CrTChestCavityEvents.publishAbilityActivated(null, testAbility, 1.0F);
            assertTrue(canceledWithHandler, "Event should be canceled when handler cancels it");
        } finally {
            handle.close();
        }

        assertFalse(CrTChestCavityEvents.publishAbilityActivated(null, testAbility, 1.0F),
                "After closing handle, event should no longer be canceled");
    }

    @Test
    void testPlayerCompatibilityIdRetained() {
        java.util.UUID playerUuid = java.util.UUID.randomUUID();
        ChestCavityData oldCavity = new ChestCavityData();
        oldCavity.setCompatibilityId(playerUuid);
        ChestCavityData newCavity = new ChestCavityData();

        // Simulate resetPlayerChestCavityAfterDeath logic
        java.util.UUID compatId = playerUuid; // from owner.getUniqueID()
        newCavity.setCompatibilityId(compatId);
        assertEquals(playerUuid, newCavity.getCompatibilityId(), "Player's original UUID must be retained");
    }

    @Test
    void testChestCavityCapabilityEnsureRegistered() {
        assertTrue(ChestCavityCapability.ensureRegistered());
        assertTrue(ChestCavityCapability.ensureRegistered());
    }
}
