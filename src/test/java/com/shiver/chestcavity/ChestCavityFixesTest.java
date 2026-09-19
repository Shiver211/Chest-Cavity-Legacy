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
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.crt.CrTChestCavityType;
import crafttweaker.api.item.IItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

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
    void testAbilityActivationCancellation() {
        String testAbility = "test_cancel_ability";
        boolean canceled = CrTChestCavityEvents.publishAbilityActivated(null, testAbility, 1.0F);
        assertFalse(canceled, "Default event with no handlers should not be canceled");

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

        java.util.UUID compatId = playerUuid;
        newCavity.setCompatibilityId(compatId);
        assertEquals(playerUuid, newCavity.getCompatibilityId(), "Player's original UUID must be retained");
    }

    @Test
    void testChestCavityCapabilityEnsureRegistered() {
        assertTrue(ChestCavityCapability.ensureRegistered());
        assertTrue(ChestCavityCapability.ensureRegistered());
    }

    @Test
    void testP0P1P2FixesVerification() {
        if (ForgeRegistries.ITEMS.getValue(new ResourceLocation("chestcavity", "animal_stomach")) == null) {
            CCItems.register(ForgeRegistries.ITEMS);
        }
        CCItems.registerOreDictionary();
        DataLoaders.reload();

        // 1. P0 - Snow Golem scores and charcoal metadata
        com.shiver.chestcavity.chest.types.ChestCavityType snowGolem = DataLoaders.getType("snow_golem");
        assertNotNull(snowGolem);
        assertEquals(Items.COAL, snowGolem.getDefaultChestCavity().getStack(4).getItem());
        assertEquals(1, snowGolem.getDefaultChestCavity().getStack(4).getMetadata(), "Snow golem slot 4 should be charcoal (meta 1)");
        assertEquals(1, snowGolem.getDefaultChestCavity().getStack(13).getMetadata(), "Snow golem slot 13 should be charcoal (meta 1)");
        assertEquals(1, snowGolem.getDefaultChestCavity().getStack(22).getMetadata(), "Snow golem slot 22 should be charcoal (meta 1)");

        Map<String, Float> snowScores = snowGolem.getDefaultOrganScores();
        assertTrue(snowScores.getOrDefault(CCOrganScores.STRENGTH, 0.0F) > 7.9F, "Snow golem strength should be ~8");
        assertTrue(snowScores.getOrDefault(CCOrganScores.SPEED, 0.0F) > 7.9F, "Snow golem speed should be ~8");
        assertEquals(3.0F / 64.0F, snowScores.getOrDefault(CCOrganScores.HEALTH, 0.0F), 0.0001F, "Snow golem health should be 3/64");
        assertEquals(3.0F / 64.0F, snowScores.getOrDefault(CCOrganScores.DEFENSE, 0.0F), 0.0001F, "Snow golem defense should be 3/64");
        assertEquals(3.0F / 64.0F, snowScores.getOrDefault(CCOrganScores.NERVES, 0.0F), 0.0001F, "Snow golem nerves should be 3/64");

        // ExceptionalOrgan matching directly
        ItemStack charcoalStack = new ItemStack(Items.COAL, 1, 1);
        ItemStack coalStack = new ItemStack(Items.COAL, 1, 0);
        assertNotNull(snowGolem.catchExceptionalOrgan(charcoalStack), "Charcoal must match exceptional organ");
        assertNull(snowGolem.catchExceptionalOrgan(coalStack), "Regular coal must not match charcoal exceptional organ");

        ItemStack snowBlockStack = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SNOW));
        assertNotNull(snowGolem.catchExceptionalOrgan(snowBlockStack), "Snow block must match exceptional organ");

        ItemStack snowballStack = new ItemStack(Items.SNOWBALL);
        assertNotNull(snowGolem.catchExceptionalOrgan(snowballStack), "Snowball must match exceptional organ");

        // ExceptionalOrgan explicit metadata constructor and getters test
        GeneratedChestCavityType.ExceptionalOrgan metaMatchOrgan = new GeneratedChestCavityType.ExceptionalOrgan(
                Items.DYE, 4, null, Collections.singletonMap(CCOrganScores.LUCK, 1.0F)); // Lapis lazuli (meta 4)
        assertEquals(Items.DYE, metaMatchOrgan.getItem());
        assertEquals(4, metaMatchOrgan.getMetadata());
        assertNull(metaMatchOrgan.getOreName());
        assertEquals(1.0F, metaMatchOrgan.getScores().get(CCOrganScores.LUCK));

        GeneratedChestCavityType testType = new GeneratedChestCavityType();
        testType.addExceptionalOrgan(Items.DYE, 4, null, Collections.singletonMap(CCOrganScores.LUCK, 1.0F));
        assertNotNull(testType.catchExceptionalOrgan(new ItemStack(Items.DYE, 1, 4)), "Meta 4 should match");
        assertNull(testType.catchExceptionalOrgan(new ItemStack(Items.DYE, 1, 0)), "Meta 0 should not match");

        // Edge case: rule has BOTH item (with specific metadata) and oreName.
        // If stack matches item but has WRONG metadata, it MUST NOT fall through to match oreName!
        GeneratedChestCavityType edgeType = new GeneratedChestCavityType();
        // Item is coal with meta 1 (charcoal), ore is "coal" (which includes both coal 0 and charcoal 1)
        edgeType.addExceptionalOrgan(Items.COAL, 1, "coal", Collections.singletonMap(CCOrganScores.HEALTH, 1.0F));
        assertNotNull(edgeType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 1)), "Meta 1 should match specific item");
        assertNull(edgeType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 0)),
                "Meta 0 should NOT match even if oreName 'coal' matches, because item matched with wrong metadata");

        // ChestCavityTypeApi metadata overload tests
        String apiTypeId = "test_meta_api_type";
        ChestCavityApis.TYPES.register(apiTypeId);
        ChestCavityApis.TYPES.addExceptionalOrgan(apiTypeId, Items.COAL, 1, Collections.singletonMap(CCOrganScores.HEALTH, 5.0F));
        com.shiver.chestcavity.chest.types.ChestCavityType registeredApiType = DataLoaders.getType(apiTypeId);
        assertNotNull(registeredApiType);
        assertNotNull(registeredApiType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 1)), "API meta 1 must match");
        assertNull(registeredApiType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 0)), "API meta 0 must NOT match");

        // ChestCavityTypeApi ItemStack overload tests
        String apiStackTypeId = "test_meta_stack_api_type";
        ChestCavityApis.TYPES.register(apiStackTypeId);
        ChestCavityApis.TYPES.addExceptionalOrgan(apiStackTypeId, new ItemStack(Items.COAL, 1, 1), Collections.singletonMap(CCOrganScores.DEFENSE, 7.0F));
        com.shiver.chestcavity.chest.types.ChestCavityType registeredStackApiType = DataLoaders.getType(apiStackTypeId);
        assertNotNull(registeredStackApiType);
        assertNotNull(registeredStackApiType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 1)), "ItemStack API meta 1 must match");
        assertNull(registeredStackApiType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 0)), "ItemStack API meta 0 must NOT match");

        // CraftTweaker CrTChestCavityType metadata preservation test
        String crtTypeId = "test_crt_meta_type";
        ChestCavityApis.TYPES.register(crtTypeId);
        IItemStack mockCrtItem = (IItemStack) java.lang.reflect.Proxy.newProxyInstance(
                IItemStack.class.getClassLoader(),
                new Class<?>[]{IItemStack.class},
                (proxy, method, args) -> {
                    if ("isEmpty".equals(method.getName())) return false;
                    if ("getInternal".equals(method.getName())) return new ItemStack(Items.COAL, 1, 1);
                    return null;
                }
        );
        CrTChestCavityType.addExceptionalOrgan(crtTypeId, mockCrtItem, Collections.singletonMap(CCOrganScores.NERVES, 9.0F));
        com.shiver.chestcavity.chest.types.ChestCavityType registeredCrTType = DataLoaders.getType(crtTypeId);
        assertNotNull(registeredCrTType);
        assertNotNull(registeredCrTType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 1)), "CrT meta 1 must match");
        assertNull(registeredCrTType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 0)), "CrT meta 0 must NOT match");

        // DataLoaders JSON loading test: legacy name aliasing (charcoal, snow_block) and damage fallback
        String jsonTypeContent = "{\n"
                + "  \"defaultChestCavity\": [\n"
                + "    {\"item\": \"minecraft:charcoal\", \"position\": 0, \"count\": 1},\n"
                + "    {\"item\": \"minecraft:dye\", \"meta\": \"invalid\", \"damage\": 4, \"position\": 1, \"count\": 1}\n"
                + "  ],\n"
                + "  \"exceptionalOrgans\": [\n"
                + "    {\n"
                + "      \"ingredient\": {\"item\": \"minecraft:charcoal\"},\n"
                + "      \"value\": [{\"id\": \"strength\", \"value\": \"10\"}]\n"
                + "    },\n"
                + "    {\n"
                + "      \"ingredient\": {\"item\": \"minecraft:snow_block\"},\n"
                + "      \"value\": [{\"id\": \"speed\", \"value\": \"20\"}]\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        DataLoaders.loadJsonDirect("types/test_alias_type.json", new StringReader(jsonTypeContent));
        com.shiver.chestcavity.chest.types.ChestCavityType aliasType = DataLoaders.getType("test_alias_type");
        assertNotNull(aliasType, "Dynamically loaded aliasType should exist");
        assertEquals(Items.COAL, aliasType.getDefaultChestCavity().getStack(0).getItem());
        assertEquals(1, aliasType.getDefaultChestCavity().getStack(0).getMetadata(), "minecraft:charcoal should map to meta 1");
        assertEquals(Items.DYE, aliasType.getDefaultChestCavity().getStack(1).getItem());
        assertEquals(4, aliasType.getDefaultChestCavity().getStack(1).getMetadata(), "Invalid meta should fall back to valid damage");

        assertNotNull(aliasType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 1)), "Charcoal exceptional organ should match coal meta 1");
        assertNull(aliasType.catchExceptionalOrgan(new ItemStack(Items.COAL, 1, 0)), "Charcoal exceptional organ should NOT match coal meta 0");
        ItemStack snowStack = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SNOW));
        assertNotNull(aliasType.catchExceptionalOrgan(snowStack), "snow_block exceptional organ should match Blocks.SNOW");

        // 2. P0 - Animal Stomach no grazing & Herbivore rumen retains grazing
        OrganData rawStomachData = OrganData.getRegistry().get(new ResourceLocation("chestcavity", "animal_stomach"));
        assertNotNull(rawStomachData, "Animal stomach data should be loaded in OrganData registry");
        assertEquals(0.75F, rawStomachData.getOrganScores().getOrDefault(CCOrganScores.DIGESTION, 0.0F), 0.0001F);
        assertNull(rawStomachData.getOrganScores().get(CCOrganScores.GRAZING), "Animal stomach must not have grazing");

        OrganData rawRumenData = OrganData.getRegistry().get(new ResourceLocation("chestcavity", "herbivore_rumen"));
        assertNotNull(rawRumenData, "Herbivore rumen data should be loaded in OrganData registry");
        assertEquals(1.0F, rawRumenData.getOrganScores().getOrDefault(CCOrganScores.GRAZING, 0.0F), 0.0001F,
                "Herbivore rumen must retain grazing score of 1.0");

        // 3. P1 - Horse entity assignment
        ResourceLocation horseId = new ResourceLocation("minecraft", "horse");
        assertEquals("horse", DataLoaders.getAssignedTypeId(horseId), "Horse must be assigned to horse type");
        assertEquals("herbivore", DataLoaders.getAssignedTypeId(new ResourceLocation("minecraft", "donkey")), "Donkey must be assigned to herbivore");
        assertEquals("herbivore", DataLoaders.getAssignedTypeId(new ResourceLocation("minecraft", "mule")), "Mule must be assigned to herbivore");

        // 5. P1 - Slime ball balance
        ItemStack slimeBallStack = new ItemStack(Items.SLIME_BALL);
        OrganData fallbackResolved = OrganDataResolver.resolve(DataLoaders.getFallbackType(), slimeBallStack);
        assertNull(fallbackResolved, "Slime ball must not be an organ for fallback/non-slime types");

        com.shiver.chestcavity.chest.types.ChestCavityType slimeType = DataLoaders.getType("slime");
        assertNotNull(slimeType);
        OrganData slimeResolved = OrganDataResolver.resolve(slimeType, slimeBallStack);
        assertNotNull(slimeResolved, "Slime ball should be an exceptional organ for slime type");
        assertEquals(32.0F, slimeResolved.getOrganScores().get(CCOrganScores.HEALTH));
        assertEquals(64.0F, slimeResolved.getOrganScores().get(CCOrganScores.STRENGTH));
        assertEquals(64.0F, slimeResolved.getOrganScores().get(CCOrganScores.SPEED));
        assertEquals(64.0F, slimeResolved.getOrganScores().get(CCOrganScores.DEFENSE));
        assertEquals(1.0F, slimeResolved.getOrganScores().get(CCOrganScores.BUOYANT));
        assertEquals(1.0F, slimeResolved.getOrganScores().get(CCOrganScores.LEAPING));

        Map<String, Float> slimeDefaultScores = slimeType.getDefaultOrganScores();
        assertEquals(1.0F, slimeDefaultScores.get(CCOrganScores.HEALTH), 0.0001F); // 0.5 base + 32/64
        assertEquals(1.0F, slimeDefaultScores.get(CCOrganScores.STRENGTH), 0.0001F);
        assertEquals(1.0F, slimeDefaultScores.get(CCOrganScores.SPEED), 0.0001F);
        assertEquals(1.0F, slimeDefaultScores.get(CCOrganScores.DEFENSE), 0.0001F);
        assertEquals(1.0F / 64.0F, slimeDefaultScores.get(CCOrganScores.BUOYANT), 0.0001F);
        assertEquals(1.0F / 64.0F, slimeDefaultScores.get(CCOrganScores.LEAPING), 0.0001F);

        // 6. P2 - Gunpowder creepy score removed
        OrganData gunpowderData = OrganData.getRegistry().get(new ResourceLocation("minecraft", "gunpowder"));
        assertNotNull(gunpowderData);
        assertEquals(192.0F, gunpowderData.getOrganScores().get(CCOrganScores.EXPLOSIVE));
        assertNull(gunpowderData.getOrganScores().get(CCOrganScores.CREEPY), "Gunpowder must not have creepy score");

        com.shiver.chestcavity.chest.types.ChestCavityType creeperType = DataLoaders.getType("creeper");
        assertNotNull(creeperType);
        assertEquals(1.0F, creeperType.getDefaultOrganScores().getOrDefault(CCOrganScores.CREEPY, 0.0F), 0.0001F,
                "Creeper default creepy score must be exactly 1.0F, not drifting to 1.046875F");
    }
}
