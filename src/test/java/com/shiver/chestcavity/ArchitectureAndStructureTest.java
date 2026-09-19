package com.shiver.chestcavity;

import com.shiver.chestcavity.ability.builtin.AbilityActivationHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.capability.UnmodifiableNonNullList;
import com.shiver.chestcavity.event.ChestCavityCombatEvents;
import com.shiver.chestcavity.event.ChestCavityDropEvents;
import com.shiver.chestcavity.event.ChestCavityInteractionEvents;
import com.shiver.chestcavity.event.ChestCavityLifecycleEvents;
import com.shiver.chestcavity.event.ChestCavityNetworkEvents;
import com.shiver.chestcavity.event.ForgeEvents;
import com.shiver.chestcavity.network.MessageChestCavitySync;
import com.shiver.chestcavity.network.MessageMovementConfigSync;
import com.shiver.chestcavity.network.MessageOrganDataSync;
import com.shiver.chestcavity.proxy.ClientProxy;
import com.shiver.chestcavity.proxy.CommonProxy;
import com.shiver.chestcavity.proxy.ServerProxy;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureAndStructureTest {

    @BeforeAll
    static void init() {
        if (!Bootstrap.isRegistered()) {
            Bootstrap.register();
        }
    }

    @Test
    void testSidedProxySetupAndFallback() throws Exception {
        assertNotNull(ChestCavityLegacy.PROXY, "ChestCavityLegacy.PROXY should be initialized with default CommonProxy");
        assertTrue(ChestCavityLegacy.PROXY instanceof CommonProxy, "PROXY must be an instance of CommonProxy");

        ClientProxy clientProxy = new ClientProxy();
        assertTrue(clientProxy instanceof CommonProxy, "ClientProxy must extend CommonProxy");

        ServerProxy serverProxy = new ServerProxy();
        assertTrue(serverProxy instanceof CommonProxy, "ServerProxy must extend CommonProxy");

        Field proxyField = ChestCavityLegacy.class.getField("PROXY");
        assertTrue(proxyField.isAnnotationPresent(SidedProxy.class), "PROXY field must be annotated with @SidedProxy");
        SidedProxy annotation = proxyField.getAnnotation(SidedProxy.class);
        assertEquals("com.shiver.chestcavity.proxy.ClientProxy", annotation.clientSide());
        assertEquals("com.shiver.chestcavity.proxy.ServerProxy", annotation.serverSide());

        assertDoesNotThrow(() -> {
            ChestCavityLegacy.PROXY.handleChestCavitySync(new MessageChestCavitySync());
            ChestCavityLegacy.PROXY.handleOrganDataSync(new MessageOrganDataSync());
            ChestCavityLegacy.PROXY.handleMovementConfigSync(new MessageMovementConfigSync(0.05F, 0.05F));
        });
    }

    @Test
    void testModularEventSubscribers() throws Exception {
        assertTrue(ChestCavityCombatEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ChestCavityCombatEvents must be an @EventBusSubscriber");
        assertTrue(ChestCavityInteractionEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ChestCavityInteractionEvents must be an @EventBusSubscriber");
        assertTrue(ChestCavityLifecycleEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ChestCavityLifecycleEvents must be an @EventBusSubscriber");
        assertTrue(ChestCavityDropEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ChestCavityDropEvents must be an @EventBusSubscriber");
        assertTrue(ChestCavityNetworkEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ChestCavityNetworkEvents must be an @EventBusSubscriber");

        assertTrue(ForgeEvents.class.isAnnotationPresent(Deprecated.class),
                "ForgeEvents must be annotated with @Deprecated");
        assertFalse(ForgeEvents.class.isAnnotationPresent(Mod.EventBusSubscriber.class),
                "ForgeEvents must NOT be an @EventBusSubscriber to avoid duplicate execution");

        Method networkClientDisc = ChestCavityNetworkEvents.class.getMethod("clientDisconnected",
                net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent.class);
        assertTrue(networkClientDisc.isAnnotationPresent(SideOnly.class));
        assertEquals(Side.CLIENT, networkClientDisc.getAnnotation(SideOnly.class).value());

        Method forgeClientDisc = ForgeEvents.class.getMethod("clientDisconnected",
                net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent.class);
        assertTrue(forgeClientDisc.isAnnotationPresent(SideOnly.class));
        assertEquals(Side.CLIENT, forgeClientDisc.getAnnotation(SideOnly.class).value());
    }

    @Test
    void testOrgansListUnmodifiableDefensiveView() {
        IChestCavity cavity = new ChestCavityData();
        NonNullList<ItemStack> organs = cavity.getOrgans();
        assertNotNull(organs);
        assertEquals(ChestCavityData.DEFAULT_SLOT_COUNT, organs.size());

        ItemStack apple = new ItemStack(Items.APPLE);

        assertThrows(UnsupportedOperationException.class, () -> organs.set(0, apple),
                "Directly setting on cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, () -> organs.add(apple),
                "Directly adding to cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, () -> organs.remove(0),
                "Directly removing from cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, organs::clear,
                "Directly clearing cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, () -> organs.addAll(Collections.singletonList(apple)),
                "Directly addAll to cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, () -> organs.removeAll(Collections.singletonList(apple)),
                "Directly removeAll from cavity.getOrgans() must be forbidden");
        assertThrows(UnsupportedOperationException.class, () -> organs.retainAll(Collections.emptyList()),
                "Directly retainAll from cavity.getOrgans() must be forbidden");

        assertThrows(UnsupportedOperationException.class, () -> organs.removeIf(stack -> false),
                "removeIf must throw UnsupportedOperationException on unmodifiable list even with false predicate");
        assertThrows(UnsupportedOperationException.class, () -> organs.replaceAll(stack -> stack),
                "replaceAll must throw UnsupportedOperationException on unmodifiable list");
        assertThrows(UnsupportedOperationException.class, () -> organs.sort(null),
                "sort must throw UnsupportedOperationException on unmodifiable list");

        assertThrows(UnsupportedOperationException.class, () -> {
            for (java.util.Iterator<ItemStack> it = organs.iterator(); it.hasNext(); ) {
                it.next();
                it.remove();
            }
        });

        assertThrows(UnsupportedOperationException.class, () -> organs.listIterator().add(apple));

        assertThrows(UnsupportedOperationException.class, () -> organs.subList(0, 1).set(0, apple));

        cavity.setOrgan(2, apple);
        assertEquals(Items.APPLE, cavity.getOrgans().get(2).getItem(),
                "getOrgans() defensive view must reflect updates made via safe accessor setOrgan()");
        assertEquals(Items.APPLE, cavity.getOrgan(2).getItem(),
                "getOrgan(slot) must return the updated organ");

        ItemStack stored = cavity.getOrgan(2);
        assertTrue(ItemStack.areItemStacksEqual(apple, stored));
        assertEquals(2, organs.indexOf(stored));
        assertEquals(2, organs.lastIndexOf(stored));
        assertEquals(organs, cavity.getOrgans());
        assertEquals(organs.hashCode(), cavity.getOrgans().hashCode());

        assertEquals(ItemStack.EMPTY, cavity.getOrgan(-1), "Negative slot must return ItemStack.EMPTY safely");
        assertEquals(ItemStack.EMPTY, cavity.getOrgan(100), "Out of bounds slot must return ItemStack.EMPTY safely");
    }

    @Test
    void testOrganScoresUnmodifiableDefensiveView() {
        IChestCavity cavity = new ChestCavityData();
        Map<String, Float> scores = cavity.getOrganScores();
        Map<String, Float> scoresView = cavity.getOrganScoresView();
        Map<String, Float> oldScores = cavity.getOldOrganScores();
        Map<String, Float> oldScoresView = cavity.getOldOrganScoresView();

        assertThrows(UnsupportedOperationException.class, () -> scores.put(CCOrganScores.HEALTH, 5.0F),
                "Directly mutating getOrganScores() must throw UnsupportedOperationException");
        assertThrows(UnsupportedOperationException.class, () -> scores.remove(CCOrganScores.HEALTH),
                "Directly removing from getOrganScores() must throw UnsupportedOperationException");
        assertThrows(UnsupportedOperationException.class, scores::clear,
                "Directly clearing getOrganScores() must throw UnsupportedOperationException");

        assertThrows(UnsupportedOperationException.class, () -> scoresView.put(CCOrganScores.HEALTH, 5.0F));

        assertThrows(UnsupportedOperationException.class, () -> oldScores.put(CCOrganScores.HEALTH, 5.0F));
        assertThrows(UnsupportedOperationException.class, () -> oldScoresView.put(CCOrganScores.HEALTH, 5.0F));

        cavity.setOrganScore(CCOrganScores.HEALTH, 3.5F);
        assertEquals(3.5F, cavity.getOrganScores().get(CCOrganScores.HEALTH),
                "getOrganScores() must reflect score set via safe accessor");
        assertEquals(3.5F, cavity.getOrganScoresView().get(CCOrganScores.HEALTH),
                "getOrganScoresView() must reflect score set via safe accessor");
        assertEquals(3.5F, cavity.getOrganScore(CCOrganScores.HEALTH));
        assertTrue(cavity.hasScoreChanges(), "Modifying score via safe accessor must mark scoreChanges dirty");

        assertEquals(0.0F, cavity.getOrganScore(null), "Null score id should return 0.0F");
        assertEquals(0.0F, cavity.getOldOrganScore(null), "Null old score id should return 0.0F");

        Map<String, Float> newScores = new HashMap<>();
        newScores.put(CCOrganScores.DIGESTION, 2.0F);
        cavity.replaceOrganScores(newScores);
        assertEquals(2.0F, cavity.getOrganScores().get(CCOrganScores.DIGESTION));
        assertNull(cavity.getOrganScores().get(CCOrganScores.HEALTH));

        assertDoesNotThrow(() -> cavity.replaceOrganScores(null));
        assertTrue(cavity.getOrganScores().isEmpty());

        cavity.setOrganScore(CCOrganScores.LUCK, 1.0F);
        cavity.copyCurrentScoresToOld();
        assertFalse(cavity.hasScoreChanges());
        assertEquals(1.0F, cavity.getOldOrganScores().get(CCOrganScores.LUCK));
        assertEquals(1.0F, cavity.getOldOrganScoresView().get(CCOrganScores.LUCK));
    }

    @Test
    void testSerializationPreservesDefensiveWrapper() {
        ChestCavityData original = new ChestCavityData();
        original.setOrgan(1, new ItemStack(Items.BEEF));
        original.setOrganScore(CCOrganScores.DIGESTION, 1.5F);

        NBTTagCompound tag = original.serializeNBT();

        ChestCavityData restored = new ChestCavityData();
        restored.deserializeNBT(tag);

        assertEquals(Items.BEEF, restored.getOrgan(1).getItem());
        assertEquals(Items.BEEF, restored.getOrgans().get(1).getItem());
        assertEquals(1.5F, restored.getOrganScore(CCOrganScores.DIGESTION));

        assertThrows(UnsupportedOperationException.class, () -> restored.getOrgans().set(1, ItemStack.EMPTY));
        assertThrows(UnsupportedOperationException.class, () -> restored.getOrganScores().put("test", 1.0F));
    }

    @Test
    void testAbilityActivationHelperPublicAndFunctional() {
        assertTrue(java.lang.reflect.Modifier.isPublic(AbilityActivationHelper.class.getModifiers()),
                "AbilityActivationHelper should be a public class");

        assertDoesNotThrow(() -> {
            Method lookMethod = AbilityActivationHelper.class.getMethod("getNormalizedLook", net.minecraft.entity.player.EntityPlayerMP.class);
            assertTrue(java.lang.reflect.Modifier.isPublic(lookMethod.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isStatic(lookMethod.getModifiers()));

            Method targetMethod = AbilityActivationHelper.class.getMethod("findNearestTarget", net.minecraft.entity.player.EntityPlayerMP.class, double.class);
            assertTrue(java.lang.reflect.Modifier.isPublic(targetMethod.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isStatic(targetMethod.getModifiers()));

            Method recoilMethod = AbilityActivationHelper.class.getMethod("applyRecoil", net.minecraft.entity.player.EntityPlayerMP.class, net.minecraft.util.math.Vec3d.class, double.class);
            assertTrue(java.lang.reflect.Modifier.isPublic(recoilMethod.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isStatic(recoilMethod.getModifiers()));
        });

        // Defensive checks with null inputs
        assertNull(AbilityActivationHelper.getNormalizedLook(null));
        assertNull(AbilityActivationHelper.findNearestTarget(null, 10.0D));
        assertDoesNotThrow(() -> AbilityActivationHelper.applyRecoil(null, null, 1.0D));
    }
}
